/*
 * Copyright (c) 2004-2005 Auster Solutions do Brasil. All Rights Reserved.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * Created on Apr 22, 2005
 */
package br.com.auster.udd.reader;

import gnu.trove.TIntStack;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import br.com.auster.common.util.I18n;
import br.com.auster.common.xml.DOMUtils;
import br.com.auster.udd.node.UDDElement;
import br.com.auster.udd.node.UDDKeyDefinition;
import br.com.auster.udd.node.UDDNode;

/**
 * TODO class comments
 * 
 * @version $Id: TaggedFileReader.java 53 2007-04-04 19:24:32Z rbarone $
 */
public final class TaggedFileReader extends FlatNIOReader {

  // Some constants
  public static final String NAMESPACE_URI = "http://www.auster.com.br/udd/TaggedFileReader/";

  protected static final String START_KEY_ATTR = "start-key";

  protected static final String END_KEY_ATTR = "end-key";

  protected static final String NAME_ATTR = "name";

  protected static final String LINE_NUMBER_ATTR = "line";

  protected static final String PRINT_LINE_NUMBER_ATTR = "print-line-number";
  
  protected static final String PRINT_KEY_ATTR = "print-key";
  
  protected static final String SKIP_KEY_ATTR = "skip";

  protected static final String KEY_TAG = "key";
  
  protected static final String KEY_DEF_ATTR = "key-definition";

  protected static final String DOCUMENT_TAG = "document";

  protected static final String KEY_NAME_ATTR = "key";

  protected static final String LABEL_ATTR = "label";
  

  /**
	 * This class is used to maintain a list of what components a block may have.
	 */
  private final class RecordBlock {

    // Map<RecordBlock(childs)>
    private final Map blocks = new HashMap();
    
    private final int index;

    private final I18n i18n = I18n.getInstance(RecordBlock.class);

    public final String blockName, keyName, startKey, endKey, label;
    
    public final boolean skipFlag;

    public final UDDElement udd;

    // WeakReference<RecordBlock>
    public final WeakReference parent;
    
    // UDDKeyDefinition name for this block (null if none)
    public final String keyDefinitionName; 
    
    // HashMap<String (child key name), String(UDDKeyDefinition name)>
    public final HashMap keyDefinitions = new HashMap();

    /**
     * Given a root element, creates a record block for it and their children,
     * recursively.
     * 
     * @param root
     *          the root element defining this block.
     */
    public RecordBlock(Element root) throws SAXException {
      this.blockName = "";
      this.startKey = "";
      this.endKey = "";
      this.label = "";
      this.skipFlag = false;
      this.index = 0;
      this.udd = null;
      this.parent = null;
      this.keyName = "ROOT";
      this.keyDefinitionName = null;
      this.createChildren(root);
    }

    /**
     * Given a root element, creates a record block for it and their children,
     * recursively.
     * 
     * @param root
     *          the root element defining this block.
     * @param parent
     *          the parent record for this.
     */
    public RecordBlock(Element root, RecordBlock parent, String startKey, int index) throws SAXException {
      this.parent = new WeakReference(parent);
      this.blockName = DOMUtils.getAttribute(root, NAME_ATTR, true);
      this.startKey = startKey;     
      this.endKey = root.getAttribute(END_KEY_ATTR);
      this.skipFlag = DOMUtils.getBooleanAttribute(root, SKIP_KEY_ATTR);
      this.udd = new UDDElement(root, parent.udd);
      this.label = DOMUtils.getAttribute(root, LABEL_ATTR, false);
      this.index = index;
      
      final String keyDefName = DOMUtils.getAttribute(root, KEY_DEF_ATTR, false);
      if (keyDefName != null && keyDefName.length() > 0) {
      	this.keyName = DOMUtils.getAttribute(root, KEY_NAME_ATTR, true);
      	this.keyDefinitionName = keyDefName;
      } else {
      	this.keyName = this.startKey;
      	this.keyDefinitionName = null;
      }
      
      this.createChildren(root);
    }

    private final void createChildren(Element root) throws SAXException {
      String malformedAttr = null;
      if (this.parent == null && root.hasAttribute(UDDNode.REPLACE_MALFORMED_ATT)) {
        final boolean value = DOMUtils.getBooleanAttribute(root, UDDNode.REPLACE_MALFORMED_ATT);
        malformedAttr = String.valueOf(value);
      }
      final NodeList nodes = DOMUtils.getElements(root, NAMESPACE_URI, KEY_TAG);
      for (int i = 0, size = nodes.getLength(); i < size; i++) {
        final Element element = (Element) nodes.item(i);
        final String startKey = DOMUtils.getAttribute(element, START_KEY_ATTR, true);
        if (this.blocks.containsKey(startKey)) {
          throw new IllegalArgumentException(i18n.getString("keyAlreadyDefined", 
                                                            startKey,
                                                            this.keyName));
        }
        if (malformedAttr != null && !element.hasAttribute(UDDNode.REPLACE_MALFORMED_ATT)) {
          element.setAttribute(UDDNode.REPLACE_MALFORMED_ATT, malformedAttr);
        }
        
        final RecordBlock child = new RecordBlock(element, this, startKey, i);
        if (child.keyDefinitionName != null) {
        	this.keyDefinitions.put(child.keyName, child.keyDefinitionName);
        }
        this.blocks.put(startKey, child);
      }
    }

    /**
     * Gets the child block for the specified key.
     * 
     * @param key
     *          the key.
     * @return a record block that has the key specified. Null if this key is
     *         not mapped.
     */
    public final RecordBlock getChildBlock(String key) {
      return (RecordBlock) this.blocks.get(key);
    }
    
    public final RecordBlock getParent() {
      return (this.parent == null ? null : (RecordBlock) this.parent.get());
    }
  }

  /** *************************************** */
  /* START OF TaggedFileReader IMPLEMENTATION */
  /** *************************************** */

  // The static variables
  private static final Logger log = Logger.getLogger(TaggedFileReader.class);
  private static final String UDD_MISSING_ATTR = "quiet-missing";
  
  // Map<String(name), UDDKeyDefinition> - default key is null
  private Map keyDefinitions;
  
  private RecordBlock uddRoot, currentBlock;

  private String documentName;

  private int currentLine;

  private boolean isPrintLineNumber, isPrintKey;

  private final TIntStack lastEntry = new TIntStack();

  private final I18n i18n = I18n.getInstance(TaggedFileReader.class);

  private boolean quietOnMissing;

  public TaggedFileReader(Element config) throws ParserConfigurationException, SAXException,
      IOException {
    super(config);
    this.quietOnMissing = Boolean.valueOf(DOMUtils.getAttribute(config, UDD_MISSING_ATTR, false))
        .booleanValue();
  }

  /**
   * Parses the UDD configuration for the TaggedFile format.
   */
  protected void parseUDD(Element uddConf, String uddFileName) throws SAXException {
  	// key definitions
  	this.keyDefinitions = new HashMap();
  	final NodeList keyDefs = DOMUtils.getElements(uddConf, UDDKeyDefinition.KEY_DEF_ELT);
  	if (keyDefs.getLength() == 0) {
  		throw new IllegalArgumentException("Key definition error: at least one default " + 
			                                   UDDKeyDefinition.KEY_DEF_ELT + " must be defined.");
  	} else {
	  	for (int i = 0; i < keyDefs.getLength(); i++) {
	  		UDDKeyDefinition keyDef = new UDDKeyDefinition( (Element) keyDefs.item(i) );
	  		final String name = keyDef.getName() == null
														|| keyDef.getName().length() == 0 ? null : keyDef.getName();
	  		if (this.keyDefinitions.containsKey(name)) {
	  			throw new IllegalArgumentException("Key definition error: duplicated " + 
	  			                                   UDDKeyDefinition.KEY_DEF_ELT + " [" + name + "]");
	  		}
	  		this.keyDefinitions.put(name, keyDef);
	  	}
  	}
  	
    this.isPrintLineNumber = DOMUtils.getBooleanAttribute(uddConf, PRINT_LINE_NUMBER_ATTR);
    this.isPrintKey = DOMUtils.getBooleanAttribute(uddConf, PRINT_KEY_ATTR);
    
    this.documentName = DOMUtils.getAttribute(uddConf, NAME_ATTR, true);
    this.uddRoot = new RecordBlock(uddConf);
  }

  /**
   * Process a record found.
   * 
   * @param handler
   *          the content handler used to output the SAX events.
   * @param cb
   *          the buffer that contains the record found.
   */
  protected final void processRecord(ContentHandler handler, CharBuffer cb)
			throws SAXException {
		this.currentLine++;
		final String key = getKey(cb);
		if (key != null) {
			this.process(handler, cb, key);
		}
	}

  /**
   * Gets the tag name from a record
   */
  private final String getKey(CharBuffer cb) throws SAXException {
  	try {
  		String key = ((UDDKeyDefinition)this.keyDefinitions.get(null)).parseKey(cb.duplicate());
  		if (key != null) {
  			final String keyDefName = (String) this.currentBlock.keyDefinitions.get(key);
  			if (keyDefName != null) {
  				final UDDKeyDefinition userKeyDef = (UDDKeyDefinition) this.keyDefinitions.get(keyDefName);
  				if (userKeyDef != null) {
  					key = userKeyDef.parseKey(cb.duplicate());
  				}
  			}
  		}
  		return key;
  	} catch (Exception e) {
      error(e.getMessage());
      throw new SAXException(e);
    }
  }

  /**
   * Process the buffer using the key to find the RecordBlock.
   */
  protected final void process(ContentHandler handler, CharBuffer cb, String key)
      throws SAXException {
    RecordBlock childBlock = this.currentBlock.getChildBlock(key);
    if (childBlock != null) {
      // Checks the key ordering
      final int keyEntry = childBlock.index;
      final int lastEntry = this.lastEntry.pop();
      if (keyEntry < lastEntry) {
      	if (!this.quietOnMissing) {
      		warn(i18n.getString("foundKeyAfter", key,
      		                    this.currentBlock.startKey));
      	}
      }

      // Found a child of the current block
      this.lastEntry.push(keyEntry);
      if (childBlock.skipFlag) {
        return;
      }
      this.currentBlock = childBlock;
      this.lastEntry.push(0);

      try {
        final AttributesImpl atts = this.currentBlock.udd.getAttributes(cb);
        if (this.isPrintKey) {
        	atts.addAttribute("", KEY_NAME_ATTR, KEY_NAME_ATTR, "CDATA", this.currentBlock.keyName);
        }
        if (this.currentBlock.label != null && this.currentBlock.label.length() > 0) {
          atts.addAttribute("", LABEL_ATTR, LABEL_ATTR, "CDATA", this.currentBlock.label);
        }

        if (this.isPrintLineNumber) {
          // Insert the line number as an attribute of the block element
          atts.addAttribute("", LINE_NUMBER_ATTR, LINE_NUMBER_ATTR, "CDATA", Integer
              .toString(this.currentLine));
        }
        handler.startElement("", this.currentBlock.blockName, this.currentBlock.blockName, atts);
        this.currentBlock.udd.getElements(cb, handler);
      } catch (Exception e) {
        error(e.getMessage());
        System.err.println(e.getMessage() + " - Full StackTrace:");
        e.printStackTrace();
        throw new SAXException(e);
      }
    } else if (this.currentBlock.endKey.length() > 0) {
      if (!this.currentBlock.endKey.equals(key)) {
        if (!this.quietOnMissing) {
          warn(i18n.getString("keyNotDefined", key, this.currentBlock.startKey));
        }
      } else {
        // Ends the element
        handler.endElement("", this.currentBlock.blockName, this.currentBlock.blockName);

        // Gets back to the record block parent
        this.currentBlock = this.currentBlock.getParent();
        this.lastEntry.pop();
      }
    } else if (this.currentBlock.endKey.length() == 0) {
      if (this.currentBlock.getParent() == null) {
      	error(i18n.getString("couldNotFindKey", key));
      } else {
      	handler.endElement("", this.currentBlock.blockName, this.currentBlock.blockName);
	      // Gets back to the record block parent
	      this.lastEntry.pop();
	      this.currentBlock = this.currentBlock.getParent();
   	    process(handler, cb, getKey(cb));
      }
    }
  }

  /**
   * Logs the message with the line number.
   */
  private final void debug(String logMsg) {
    TaggedFileReader.log.debug("TaggedFile - (line: " + this.currentLine + ") " + logMsg);
  }

  /**
   * Logs the message with the line number.
   */
  private final void warn(String logMsg) {
    TaggedFileReader.log.warn(i18n.getString("taggedFileLine", new Integer(this.currentLine), logMsg));
  }

  /**
   * Logs the message with the line number.
   */
  private final void error(String logMsg) {
    TaggedFileReader.log.error(i18n.getString("taggedFileLine", new Integer(this.currentLine), logMsg));
  }

  /**
   * Method called at the beginning of the document, after a call to
   * <code>handler.startDocument()</code>.
   * 
   * @param handler
   *          the content handler used to output the SAX events.
   */
  protected final void startDocument(ContentHandler handler) throws SAXException {
    this.currentBlock = this.uddRoot;
    this.currentLine = 0;
    this.lastEntry.clear();
    this.lastEntry.push(0);

    handler.startElement("", this.documentName, this.documentName, new AttributesImpl());

  }

  /**
   * Method called at the end of the document, before a call to
   * <code>handler.endDocument()</code>.
   * 
   * @param handler
   *          the content handler used to output the SAX events.
   */
  protected final void endDocument(ContentHandler handler) throws SAXException {
	// only create an END tag if there is no ENDKEY defined, and the block has a defined name (this last condition excludes 	  
	//    the ROOT element)
	if (((this.currentBlock.endKey == null) || (this.currentBlock.endKey.trim().length() <= 0)) && 
		((this.currentBlock.blockName != null) && (this.currentBlock.blockName.trim().length() > 0))) {
		handler.endElement("", this.currentBlock.blockName, this.currentBlock.blockName);
	}
    handler.endElement("", this.documentName, this.documentName);
  }
}

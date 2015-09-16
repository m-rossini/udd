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
 * Created on Apr 8, 2005
 */
package br.com.auster.udd.node;

import gnu.trove.TIntObjectHashMap;

import java.nio.CharBuffer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import br.com.auster.common.io.NIOBufferUtils;
import br.com.auster.common.util.I18n;
import br.com.auster.common.xml.DOMUtils;

/**
 * Represents a UDD element. It may contains others elements and attributes.
 * 
 * @version $Id: UDDElement.java 38 2006-06-22 17:34:45Z mtengelm $
 */
public class UDDElement extends UDDNode {

	private final I18n	i18n	= I18n.getInstance(UDDElement.class);

	/**
	 * Represents a UDD parse command. It is used to parse the splitted input
	 * given in the UDDElement.
	 */
	public interface UDDParseCommand {

		/**
		 * This method is called before parsing the input sequence.
		 * 
		 * @param input
		 *          the original input.
		 */
		public void startOfParsing(CharBuffer input);

		/**
		 * This method is called to decide if this input sequence will be parsed or
		 * not.
		 * 
		 * @return a boolean value. If true, the input sequence will be parsed. If
		 *         false, the method <code>parseIndexedSequence</code> will not be
		 *         called.
		 * @param input
		 *          the original input.
		 */
		public boolean mayParse(CharBuffer input);

		/**
		 * This method will be called for every partial char sequence found in the
		 * original input. This original input will be splitted in some partial
		 * sequences based on the separator given in the UDD configuration for the
		 * element.
		 * 
		 * This method will also be called with the full input sequence and index
		 * equals to zero. This is for not indexed elements, or to give the chance
		 * to process the entire input as a hole.
		 * 
		 * @param index
		 *          the position in the original char sequence that this input was
		 *          found.
		 * @param input
		 *          the index-th partial char sequence found in the original input.
		 */
		public void parseIndexedSequence(int index, CharBuffer input)
		    throws SAXException, ParseException;

		/**
		 * This method is called when the method <code>parseIndexedSequence()</code>
		 * will not be called again before a <code>startOfProcessing()</code>
		 * call.
		 * 
		 * @param input
		 *          the original input.
		 */
		public void endOfParsing(CharBuffer input) throws SAXException;

	}

	/**
	 * This inner class is used to parse an input char sequence and generate
	 * attributes using them.
	 */
	private static final class UDDAttrCommand implements UDDParseCommand {

		private final AttributesImpl		atts	= new AttributesImpl();

		private final TIntObjectHashMap	attrMap;

		public UDDAttrCommand(TIntObjectHashMap attrMap) {
			this.attrMap = attrMap;
		}

		public void startOfParsing(CharBuffer input) {
			this.atts.clear();
		}

		public boolean mayParse(CharBuffer input) {
			return (this.attrMap.size() > 0);
		}

		public void parseIndexedSequence(int index, CharBuffer input) {
			final Map attributes = (Map) this.attrMap.get(index);
			if (attributes != null) {
				for (Iterator it = attributes.values().iterator(); it.hasNext();) {
					((UDDAttribute) it.next()).parse(input, this.atts);
				}
			}
		}

		public void endOfParsing(CharBuffer input) {}

		public AttributesImpl getAttributes() {
			return this.atts;
		}
	}

	/**
	 * This inner class is used to parse an input char sequence and generate
	 * elements using them. These elements may generate other elements based on
	 * this input.
	 */
	private static final class UDDElementCommand implements UDDParseCommand {

		private final TIntObjectHashMap	elementMap;

		private final ContentHandler		output;

		private final boolean		        showText;

		private final int		            len;

		public UDDElementCommand(TIntObjectHashMap elementMap,
		    ContentHandler output, boolean showText, int len) {
			this.elementMap = elementMap;
			this.output = output;
			this.showText = showText;
			this.len = len;
		}

		public UDDElementCommand(TIntObjectHashMap elementMap,
		    ContentHandler output, boolean showText) {
			this(elementMap, output, showText, 0);
			/*
			 this.elementMap = elementMap;
			 this.output = output;
			 this.showText = showText;
			 */
		}

		public void startOfParsing(CharBuffer input) {}

		public boolean mayParse(CharBuffer input) {
			return (this.elementMap.size() > 0);
		}

		public void parseIndexedSequence(int index, CharBuffer input)
		    throws SAXException, ParseException {
			List elements = (List) this.elementMap.get(index);
			if (elements != null) {
				for (Iterator it = elements.iterator(); it.hasNext();) {
					((UDDElement) it.next()).parse(input, this.output);
				}
			}
		}

		public void endOfParsing(CharBuffer input) throws SAXException {
			if (this.showText) {
				//this.output.characters(input.array(), 0, input.length());
				int len = this.len;
				if (this.len == -1) {
					len = input.length();
				}
				this.output.characters(input.array(), input.arrayOffset()+input.position(), len);
			}
		}

	}

	/** *********************************** */
	/* START OF UDDELEMENT IMPLEMENTATION */
	/** *********************************** */

	// The instance attributes

	protected final TIntObjectHashMap attrByIndex = new TIntObjectHashMap();

	protected final TIntObjectHashMap tagsByIndex = new TIntObjectHashMap();

	protected char separateChar;
	
	protected final boolean isSeparatorDefined;

	protected final boolean showText;

	protected boolean warnSent = false;

	private boolean isIndexed;

	private int indexSpecified;

	public UDDElement(final Element root) {
		this(root, null);
	}

	public UDDElement(final Element root, final UDDNode parent) {
		super(root, parent);
		this.showText = 
			Boolean.valueOf(root.getAttribute("show-text")).booleanValue();

		// Decides how to process the text
		final String separator = root.getAttribute("separator");

		if (separator.length() > 0) {
			if (this.isEscapeDefined) {
				if (this.escapeChar == this.separateChar) {
					throw new IllegalArgumentException(
					    "Element " + this.getName() + 
					    " separator and escape cannot be equal."
					);
				}
			}
			this.separateChar = StringEscapeUtils.unescapeJava(separator).charAt(0);
			this.isSeparatorDefined = true;
		} else {
			this.isSeparatorDefined = false;
		}

		// Gets the child nodes
		final NodeList nodes = DOMUtils.getElements(root, null);
		for (int i = 0, size = nodes.getLength(); i < size; i++) {
			this.processUDD((Element) nodes.item(i));
		}
	}

	/**
	 * Process an element from the root DOM tree configuration element, given to
	 * this UDDElement in the constructor.
	 * 
	 * @param element
	 *          the element to process.
	 * @return true if the parameter was processed, false if it was ignored
	 *         because it is not a valid UDD element for this class.
	 */
	protected boolean processUDD(Element element) {
		String indexStr = element.getAttribute("index");
		int index = 0;
		isIndexed = false;
		if (this.isSeparatorDefined && (indexStr.length() > 0)) {
			isIndexed = true;
			index = Integer.parseInt(indexStr);
		}

		if (element.getLocalName().equals("attribute")) {
			addUDDAttribute(index, DOMUtils.getAttribute(element, "name", true),
			    new UDDAttribute(element, this));

		} else if (element.getLocalName().equals("element")) {
			// If found an element tag
			addUDDElement(index, DOMUtils.getAttribute(element, "name", false),
			    new UDDElement(element, this));

		} else if (element.getLocalName().equals("choose")) {
			// If found an choose tag
			addUDDElement(index, DOMUtils.getAttribute(element, "name", false),
			    new UDDChoose(element, this));

		} else if (element.getLocalName().equals("content")) {
			// If found an content tag
			addUDDElement(index, DOMUtils.getAttribute(element, "name", true),
			    new UDDContent(element, this));

		} else {
			if (warnSent) {
				log.warn("Unknown Element found.Name is = " + element.getLocalName());
				this.warnSent = true;
			}
			return false;
		}

		return true;
	}

	/**
	 * Creates a UDDAttribute object and add it to the internal structure.
	 */
	protected void addUDDAttribute(int index, String name, UDDAttribute attr) {
		attr.setIndexSpecified(index);
		Map attributes = (Map) this.attrByIndex.get(index);
		if (attributes == null) {
			attributes = new HashMap();
			this.attrByIndex.put(index, attributes);
		}
		attributes.put(name, attr);
	}


	/**
	 * Creates a UDDElement object and add it to the internal structure.
	 */
	protected void addUDDElement(int index, String name, UDDElement element) {
		this.indexSpecified = index;
		List elements = (List) this.tagsByIndex.get(index);
		if (elements == null) {
			elements = new ArrayList();
			this.tagsByIndex.put(index, elements);
		}
		elements.add(element);
	}

	/**
	 * Process the char input and writes the result to the output handler.
	 */
	public void parse(CharBuffer input, ContentHandler output)
	    throws ParseException, SAXException {
		if (input.length() > 0) {
			CharBuffer element = getSubstring(input);
			
			final String name = getName();
			if (name != null && name.length() > 0) {
				// Starts the tag with the attributes from the input
				output.startElement("", name, name, getAttributes(element));
				// Print the children elements
				getElements(element, output);
				// Ends the tag
				output.endElement("", name, name);
			} else {
        // Only Print the children elements
				getElements(element, output);
			}
		}
	}

	/**
	 * Gets the attributes from input char sequence for this element.
	 * 
	 * @return the attributes read from the input, following this element rules.
	 */
	public AttributesImpl getAttributes(CharBuffer input) throws SAXException,
	    ParseException {
		UDDAttrCommand attrCmd = new UDDAttrCommand(this.attrByIndex);
		parse(input, attrCmd);
		return attrCmd.getAttributes();
	}

	/**
	 * Gets the children elements from the input char sequence for this element.
	 * These elements will be created as SAX events using the output handler.
	 */
	public void getElements(CharBuffer input, ContentHandler output)
	    throws SAXException, ParseException {
		parse(input, new UDDElementCommand(this.tagsByIndex, output, this.showText,
		    this.computedLength));
	}

	/**
	 * Parses an input char sequence using a parsing object. This method will
	 * split the input using the UDD configuration, if index and separator were
	 * defined, and pass these pieces of text to the command object. If there is
	 * no split configuration, them this method will just send this input to the
	 * command as it is.
	 */
	protected void parse(CharBuffer input, UDDParseCommand command)
	    throws SAXException, ParseException {
		// Tells to the command that the parsing will start for the given input
		command.startOfParsing(input);

		// Use the elements not indexed to parse the entire input
		command.parseIndexedSequence(0, input);

		// If we have some elements indexed by field number.
		if (this.isSeparatorDefined && command.mayParse(input)) {

			CharBuffer toProcess = input;
			int number = 1, index = 0, escapeOffset = 0;

			// Looks for the separator character in the buffer, 
			// to split the fields.
			// Each field found will be passed to the corresponding 
			// child.
			final CharBuffer searchBuffer = input.duplicate();
			int sepIndex = NIOBufferUtils.findToken(searchBuffer, this.separateChar);
			
			while (sepIndex >= 0) {

				if (sepIndex > 0 && searchBuffer.get(sepIndex - 1) == this.escapeChar) {
          // found an escape
					final int escapeCount = 
						NIOBufferUtils.countAdjacentOccurrencesBackwards(searchBuffer, 
						                                                 this.escapeChar, 
						                                                 sepIndex - 1);
					if (escapeCount % 2 != 0) {
						// means: this one is an escape for a separator
						toProcess = removeChar(toProcess, sepIndex - 1 - escapeOffset);
						++escapeOffset;
						searchBuffer.position(++sepIndex);
						sepIndex = NIOBufferUtils.findToken(searchBuffer, this.separateChar);
						continue;
					}
				}
				
				// found a field
				CharBuffer view = toProcess.duplicate();
				view.position(index).limit(sepIndex - escapeOffset);
				command.parseIndexedSequence(number++, view.slice());
				
				searchBuffer.position(++sepIndex);
				index = sepIndex - escapeOffset;
				sepIndex = NIOBufferUtils.findToken(searchBuffer, this.separateChar);
			}

			// Process the last field (or the unique one)
			CharBuffer view = toProcess.duplicate();
			view.position(index).limit(toProcess.length());
			command.parseIndexedSequence(number++, view.slice());
		}

		// Notifies the command that no more char sequences will come
		// before calling the startOfParsing again
		command.endOfParsing(input);
	}

	/**
	 * Removes a chunk of chars from the buffer, shifting all remaining 
	 * characters to the left.
	 * 
	 * @param cb
	 *          the buffer to be changed.
	 * @param position
	 *          the char start position which will be removed.
	 * @param length
	 *          the length of the chunk (from the position) to be removed.
	 * @return a new char buffer with the chars at the specified position
	 *         an length removed.
	 */
	protected final CharBuffer removeChars(CharBuffer input, 
	                                       int position, 
	                                       int length) {
		final int offset = input.arrayOffset();
		CharBuffer result = CharBuffer.allocate(input.capacity() - length);
		final int endPosition = position + length;
		result.put(input.array(), offset, position);
		result.put(input.array(), offset + endPosition, input.limit() - endPosition);
		return (CharBuffer) result.flip();
	}
	
	protected final CharBuffer removeChar(CharBuffer input, int position) {
		return removeChars(input, position, 1);
	}

	/***
	 * Returns the information regarding if this Element is Indexed or Not.
	 * @return
	 */
	public boolean isIndexProvided() {
		return this.isIndexed;
	}

	public boolean isShowText() {
		return this.showText;
	}

	public String toString() {
		return this.getClass().getName() + "[" + this.getName() + "]";
	}
  /*** 
   * Returns an indication of if this Node was defined thru indexed option
   * @return
   */
  public boolean isIndexed() {
    return (this.getIndexSpecified() != 0);
  }

	
  /**
   * @return Returns the indexSpecified.
   */
  public int getIndexSpecified() {
  	return this.indexSpecified;
  }
  
  public char getSeparateChar() {
  	return this.separateChar;
  }
}


/*
protected void parse(CharBuffer input, UDDParseCommand command)
throws SAXException, ParseException {
// Tells to the command that the parsing will start for the given input
command.startOfParsing(input);

// Use the elements not indexed to parse the entire input
command.parseIndexedSequence(0, input);

// If we have some elements indexed by field number.
if (this.isSeparatorDefined && command.mayParse(input)) {
boolean lastOneWasEscape = false;

CharBuffer toProcess = input;
int number = 1, index = 0, escapeOffset = 0;

// Looks for the separator character in the buffer, 
// to split the fields.
// Each field found will be passed to the corresponding 
// child.
final CharBuffer searchBuffer = input.duplicate();
int[] tokenIndexes = findSeparatorOrEscape(searchBuffer);

while (tokenIndexes[0] >= 0 || tokenIndexes[1] >= 0) {
	
	final int newPosition;
	
	if (tokenIndexes[1] >= tokenIndexes[0]) {
    // found an escape
		lastOneWasEscape = !lastOneWasEscape;
		if (lastOneWasEscape) {
			// means: this one is an escape for a separator
			toProcess = removeChar(toProcess, tokenIndexes[1] - escapeOffset);
			++escapeOffset;
		}
		newPosition = tokenIndexes[1] + 1;
	} else if (lastOneWasEscape) {
		// escaping the separator
		lastOneWasEscape = false;
		newPosition = tokenIndexes[0] + 1;
	} else {
		// found a field
		CharBuffer view = toProcess.duplicate();
		view.position(index).limit(tokenIndexes[0] - escapeOffset);
		command.parseIndexedSequence(number++, view.slice());
		
		newPosition = tokenIndexes[0] + 1;
		index = newPosition - escapeOffset;
	}
	
	searchBuffer.position(newPosition);
	tokenIndexes = findSeparatorOrEscape(searchBuffer);
}

// Process the last field (or the unique one)
CharBuffer view = toProcess.duplicate();
view.position(index).limit(toProcess.length());
command.parseIndexedSequence(number++, view.slice());
}

// Notifies the command that no more char sequences will come
// before calling the startOfParsing again
command.endOfParsing(input);
}

	private final int[] findSeparatorOrEscape(CharBuffer cb) {
		if (!this.isSeparatorDefined) {
			return new int[] {-1, -1};
		} else if (this.isEscapeDefined) {
			return NIOBufferUtils.findToken(cb, this.separateChar, this.escapeChar);
		} else {
			final int i = NIOBufferUtils.findToken(cb, this.separateChar);
			return new int[] {i, -1};
		}
	}
*/


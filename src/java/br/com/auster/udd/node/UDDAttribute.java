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

import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.xml.sax.helpers.AttributesImpl;

import br.com.auster.common.io.NIOBufferUtils;
import br.com.auster.common.xml.DOMUtils;

/**
 * Represents a UDD attribute. The portion of data that this object represent
 * will be shown as an attribute in the XML document generated.
 * 
 * MT[2006-June-13]
 * 	Added Support for Attribute Formatting.
 * 		It is necessary to use this feature, define a -D property telling what is the formatting class.
 * 	  The name of the property is  udd.attribute.formatter and its value is the full-path class name.
 * 		Formatting class MUST implement the Interface UDDFormatter OR extend UDDSimpleAttributeFormatter.
 * 		One instance of formatting class will be created for each attribute that has the type attribute on Data Definition.
 * 		It will be created and initializated during UDD File parsing.
 * 		During record parsing UDDAttributes will call the parse method of the formatter class.
 * 
 * <p>
 * WARN: Its configuration may not contains any child node. If so, they will be
 * ignored.
 * 
 * @version $Id: UDDAttribute.java 47 2006-09-21 16:23:55Z rbarone $
 */
public class UDDAttribute extends UDDNode {

	private static final String FORMATTER_CLASS = "udd.attribute.formatter";
	private static final Logger log	= Logger.getLogger(UDDAttribute.class);
	private static final String TYPE_ATTR	= "type";
	private static final String FORMAT_ATTR = "format";
	private static final String PARSE_ATTR = "parse";

	private int indexSpecified;
	private Map	formatOptions;

	// Formatting specific control attributes
	private static boolean hasFormatter	= false;
	private static String	fClassName;
	private UDDFormatter formater = null;
	private boolean	shouldFormat = false;

	static {
		fClassName = System.getProperty(FORMATTER_CLASS, "");
		if (!"".equals(fClassName)) {
			hasFormatter = true;
		}
	}
	
	public UDDAttribute(Element root) {
		this(root, null);
	}

	public UDDAttribute(Element root, UDDNode parent) {
    super(root, parent);
    // Check if we do have the System Property to Format Attributes.
    if (hasFormatter) {
      try {
        // Let´s See if this particular attribute requires formatting
        String fType = DOMUtils.getAttribute(root, TYPE_ATTR, false);
        if (!"".equals(fType)) {
          formater = (UDDFormatter) Class.forName(fClassName).newInstance();
          this.formater.setName(parent.getName() + "@" + this.getName());
          this.shouldFormat = true;
          if (this.formatOptions == null) {
            this.formatOptions = new HashMap();
          }
          this.formatOptions.put(TYPE_ATTR, fType);
          this.formatOptions.put(FORMAT_ATTR, DOMUtils.getAttribute(root, FORMAT_ATTR, false));
          this.formatOptions.put(PARSE_ATTR, DOMUtils.getAttribute(root, PARSE_ATTR, false));
          // Here we prepare the formatting Stuff.
          this.formater.setFormatOptions(formatOptions);
        }
      } catch (InstantiationException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    }
  }

	/**
	 * Based on the UDD configuration given at the instantiation, parses the
	 * <code>input</code> to define the value for this attribute and puts it in
	 * the attribute list <code>atts</code>. If the parsing of the input could
	 * not find a value for this attribute, this method will do nothing.
	 */
	public final void parse(CharBuffer input, AttributesImpl atts) {
		if (input.length() == 0) {
			return;
		}

		CharBuffer buffer = getSubstring(input);

		if (this.replaceMalformed) {
			// replace invalid XML chars
			// (code points bellow were extracted from XML 1.0 spec)
			for (int i = 0; buffer.hasRemaining(); i++) {
				int c = buffer.get();
				if (c <= 0x0008 || c == 0x000B || c == 0x000C || (c >= 0x000E && c <= 0x001F)
						|| (c >= 0xD800 && c <= 0xDFFF) || c == 0xFFFE || c == 0xFFFF
						|| c >= 0x110000) {
					buffer.put(i, REPLACEMENT_CHAR);
				}
			}
			buffer.rewind();
		}

		if (this.isEscapeDefined) {
			buffer = NIOBufferUtils.removeToken(buffer, this.escapeChar); 
		}
		
		String result = (this.trimElement) ? buffer.toString().trim() : buffer.toString();
		if (result.length() > 0) {
			String name = this.getName();
			if (hasFormatter && this.shouldFormat) {
				result = (String) this.formater.format(result);
			}			
			atts.addAttribute("", name, name, "CDATA", result);
		}
	}

	/*****************************************************************************
	 * Returns an indication of if this Node was defined thru indexed option
	 * 
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

	/**
	 * @param indexSpecified
	 *          The indexSpecified to set.
	 */
	public void setIndexSpecified(int indexSpecified) {
		this.indexSpecified = indexSpecified;
	}

	public String toString() {
		return this.getClass().getName() + "[" + this.getName() + "]";
	}

}

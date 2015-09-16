package br.com.auster.udd.node;

import java.nio.CharBuffer;
import java.text.ParseException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import br.com.auster.common.xml.DOMUtils;


public class UDDKeyDefinition extends UDDElement {
	
  public static final String KEY_DEF_ELT = "key-definition";
  
  public static final String KEY_JOIN_CHAR_ATTR = "join-char";
	
	final String joinChar;

	public UDDKeyDefinition(Element root) {
		super(root);
		this.joinChar = DOMUtils.getAttribute(root, KEY_JOIN_CHAR_ATTR, false);
		
		final NodeList keyFields = DOMUtils.getElements(root, "attribute");
		// validations
		if (keyFields.getLength() == 0) {
			throw new IllegalArgumentException(
			   "Key definition error: at least one attribute node must be defined."
			   );
		}
	}

	public void parse(CharBuffer input, ContentHandler output)
			throws ParseException, SAXException {
		// do nothing
	}

	public String parseKey(CharBuffer input) throws ParseException, SAXException {
		StringBuffer key = null;
		if (input.length() > 0) {
			AttributesImpl atts =  getAttributes( getSubstring(input) );
			for (int i = 0; i < atts.getLength(); i++) {
				if (i == 0) {
					key = new StringBuffer();
				} else if (this.joinChar != null) {
					key.append(this.joinChar);
				}
				key.append( atts.getValue(i) );
			}
		}
		return key == null ? null : key.toString();
	}
	
}

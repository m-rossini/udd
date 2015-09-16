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
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import br.com.auster.common.xml.DOMUtils;

/**
 * Represents a UDD element. It may contains others elements and attributes.
 * 
 * @version $Id: UDDChoose.java 33 2006-06-08 02:46:06Z rbarone $
 */
public class UDDChoose extends UDDElement {

  protected static final Logger log = Logger.getLogger(UDDChoose.class);

  /**
   * Represents the information of how to find the value for this UDDChoose.
   */
  private static final class UDDValue extends UDDNode {

    private int index;

    public UDDValue(final Element config) {
      this(config, null);
    }
    
    public UDDValue(final Element config, final UDDNode parent) {
      super(config, parent);
      try {
        this.index = DOMUtils.getIntAttribute(config, "index", true);
      } catch (IllegalArgumentException e) {
        this.index = 0;
      }
    }

    /**
     * Format the input following the rules defined in this UDD value
     * configuration.
     * 
     * @param input
     *          the input to be formatted.
     * @return the value inside the input for this UDD value configuration.
     */
    public CharSequence getValue(CharBuffer input) {
      if (input.length() == 0) {
        return null;
      }
      return getSubstring(input).toString().trim();
    }

    public int getIndex() {
      return this.index;
    }
  }

  /**
   * Represents the udd:when element (which is a udd:element subclass with an
   * index).
   */
  private static final class UDDWhen extends UDDElement {

    private int index;

    public UDDWhen(final Element config) {
      this(config, null);
    }
    
    public UDDWhen(final Element config, final UDDNode parent) {
      super(config, parent);

      try {
        this.index = DOMUtils.getIntAttribute(config, "index", true);
      } catch (IllegalArgumentException e) {
        this.index = 0;
      }
    }

    public int getIndex() {
      return this.index;
    }
  }

  /**
   * This inner class is used to parse an input char sequence and generate
   * attributes using them. This is used to process a udd:value element.
   */
  private static final class UDDValueCommand implements UDDParseCommand {

    private final UDDValue uddValue;

    private CharSequence value;

    public UDDValueCommand(UDDValue uddValue) {
      this.uddValue = uddValue;
    }

    public void startOfParsing(CharBuffer input) {
      this.value = null;
    }

    public boolean mayParse(CharBuffer input) {
      return (this.uddValue.getIndex() > 0);
    }

    public void parseIndexedSequence(int index, CharBuffer input) {
      if (index == this.uddValue.getIndex()) {
        this.value = this.uddValue.getValue(input);
      }
    }

    public void endOfParsing(CharBuffer input) {
    }

    public String getValue() {
      return this.value.toString();
    }
  }

  /**
   * This inner class is used to parse an input char sequence and generate
   * elements using them. These elements may generate other elements based on
   * this input. This is used to process a udd:when element.
   */
  private static final class UDDWhenCommand implements UDDParseCommand {

    private final int index;

    private final UDDWhen element;

    private final ContentHandler output;

    public UDDWhenCommand(UDDWhen element, ContentHandler output) {
      this.index = element.getIndex();
      this.element = element;
      this.output = output;
    }

    public void startOfParsing(CharBuffer input) {
    }

    public boolean mayParse(CharBuffer input) {
      return true;
    }

    public void parseIndexedSequence(int index, CharBuffer input) throws SAXException,
        ParseException {
      if (this.index == index)
        element.parse(input, this.output);
    }

    public void endOfParsing(CharBuffer input) throws SAXException {
    }
  }

  // The instance attributes
  protected Map elementByValue;

  protected UDDValue uddValue;

  protected boolean canProceed = false;

  public UDDChoose(final Element root) {
    this(root, null);
  }
  
  public UDDChoose(final Element root, final UDDNode parent) {
    super(root, parent);
  }

  /**
   * Process an element from the root DOM tree configuration element, given to
   * this UDDChoose in the constructor.
   * 
   * @param element
   *          the element to process.
   * @return true if the parameter was processed, false if it was ignored
   *         because it is not a valid UDD element for this class.
   */
  protected boolean processUDD(Element element) {
    if (this.elementByValue == null)
      this.elementByValue = new HashMap();

    if (element.getLocalName().equals("when")) {
      // We found a condition to add
      if (this.canProceed) {
        this.elementByValue
            .put(DOMUtils.getAttribute(element, "value", true), new UDDWhen(element));
      } else {
        log.fatal("Achado Elemento When, sem elemento Value especificado.");
        throw new IllegalArgumentException("Achado Elemento When, sem elemento Value especificado.");
      }
    } else if (element.getLocalName().equals("otherwise")) {
      // This condition is used if no other condition is
      // satified with the given input sequences to process
      this.elementByValue.put(null, new UDDWhen(element));

    } else if (element.getLocalName().equals("value")) {
      // This element tell us how to find the value in the input
      this.uddValue = new UDDValue(element);
      this.canProceed = true;
    } else
      return false;

    return true;
  }

  /**
   * Process the char input and writes the result to the output handler.
   */
  public void parse(CharBuffer input, ContentHandler output) throws ParseException, SAXException {
    if (input.length() > 0) {
      input = getSubstring(input);
      
      // Gets the value to decide which UDDElement to use to parse the
      // input
      UDDValueCommand command = new UDDValueCommand(this.uddValue);
      parse(input, command);

      // Gets the UDDElement that must be executed for the value found.
      UDDWhen element = (UDDWhen) this.elementByValue.get(command.getValue());
      if (element == null) {
        log.debug("Element is null");
        element = (UDDWhen) this.elementByValue.get(null);
      }
      if (element == null) {
        log.debug("Element is null again!");
        return;
      }

      UDDWhenCommand command2 = new UDDWhenCommand(element, output);
      parse(input, command2);
      // element.parse(input, output);
    }
  }
  
  public Map getElementsMap() {
  	return this.elementByValue;
  }
}

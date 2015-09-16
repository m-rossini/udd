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
package br.com.auster.udd.reader;

import java.io.IOException;
import java.nio.CharBuffer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import br.com.auster.common.util.I18n;
import br.com.auster.common.xml.DOMUtils;
import br.com.auster.udd.node.UDDChoose;
import br.com.auster.udd.node.UDDContent;
import br.com.auster.udd.node.UDDElement;

/**
 * This XMLReader is used to parse a simple flat input stream, using NIO high
 * performance library, and transform it in SAX events (XML).
 * 
 * @version $Id: SimpleFlatFileReader.java 50 2006-11-14 21:18:17Z rbarone $
 */
public class SimpleFlatFileReader extends FlatNIOReader {

  // Instance variables
  protected final AttributesImpl atts = new org.xml.sax.helpers.AttributesImpl();

  protected List elements;

  protected String name;

  private final I18n i18n = I18n.getInstance(SimpleFlatFileReader.class);

  public SimpleFlatFileReader(Element config) throws ParserConfigurationException, SAXException,
      IOException {
    super(config);
  }

  /**
   * Parses the UDD configuration.
   */
  protected void parseUDD(Element uddConf, String uddFileName) throws SAXException {
    this.name = DOMUtils.getAttribute(uddConf, "name", true);
    // Gets the children that will process the records.
    this.elements = new ArrayList();
    NodeList nodes = DOMUtils.getElements(uddConf, null);
    for (int i = 0, size = nodes.getLength(); i < size; i++) {
      Element element = (Element) nodes.item(i);

      if (element.getLocalName().equals("element")) {
        // If found an element tag
        this.elements.add(new UDDElement(element));
      } else if (element.getLocalName().equals("choose")) {
        // If found a choose tag
        this.elements.add(new UDDChoose(element));
      } else if (element.getLocalName().equals("content")) {
        // If found a content tag
        this.elements.add(new UDDContent(element));
      }
    }
  }

  /**
   * Found a record. Process it.
   */
  protected void processRecord(ContentHandler handler, CharBuffer cb) throws SAXException {
    if (cb.length() > 0)
      for (Iterator it = this.elements.iterator(); it.hasNext();)
        try {
          ((UDDElement) it.next()).parse(cb, handler);
        } catch (ParseException e) {
          throw new SAXException(e);
        }
  }

  /**
   * Called when the document starts.
   */
  protected void startDocument(ContentHandler handler) throws SAXException {
    if (this.name.length() > 0)
      handler.startElement("", this.name, this.name, atts);
  }

  /**
   * Called when the document ends.
   */
  protected void endDocument(ContentHandler handler) throws SAXException {
    if (this.name.length() > 0)
      handler.endElement("", this.name, this.name);
  }
}

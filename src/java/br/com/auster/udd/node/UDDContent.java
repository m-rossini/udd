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

import java.io.File;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;

import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

import br.com.auster.common.io.NIOUtils;
import br.com.auster.common.util.I18n;
import br.com.auster.common.xml.DOMUtils;
import br.com.auster.common.xml.sax.NIOInputSource;

/**
 * Represents a UDD element. It may contains others elements and attributes.
 * 
 * @version $Id: UDDContent.java 47 2006-09-21 16:23:55Z rbarone $
 */
public class UDDContent extends UDDElement {

  private final I18n i18n = I18n.getInstance(UDDContent.class);

  protected static String mode;

  private static final ThreadLocal alreadyDone = new ThreadLocal() {
    protected synchronized Object initialValue() {
      return Boolean.FALSE;
    }
  };

  protected String fName = "";

  protected boolean useName = true;

  protected static final String XML_READER_ELEMENT = "xml-reader";

  protected static final String CLASS_NAME_ATTR = "class-name";

  // holds one xmlReader per Thread - for initialization, see
  // getXMLReaderInstance()
  private static final ThreadLocal xmlReader = new ThreadLocal() {
    protected synchronized Object initialValue() {
      return null;
    }
  };

  protected String inputFile;

  public UDDContent(Element root) {
    this(root, null);
  }
  
  public UDDContent(final Element root, final UDDNode parent) {
    super(root, parent);

    fName = root.getAttribute("file-name");
    useName = Boolean.valueOf(root.getAttribute("use-name")).booleanValue();

    Element element = DOMUtils.getElement(root, XML_READER_ELEMENT, false);

    if (fName.equals("") && element == null) {
      log.error(i18n.getString("noParms"));
    } else if (!fName.equals("") && element != null) {
      log.error(i18n.getString("onlyOne"));
    }

    if (fName.equals("")) {
      try {
        xmlReader.set( getXMLReaderInstance(element) );
        inputFile = DOMUtils.getAttribute(element, "udd-file", true);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    mode = root.getAttribute("mode");
    if (!mode.equals("once") && !mode.equals("many")) {
      log.error(i18n.getString("wrongMode", mode));
      mode = "many";
    }

  }

  public void parse(CharSequence input, ContentHandler ch) throws SAXException {

    if (mode.equals("once") && ((Boolean)alreadyDone.get()).booleanValue()) {
      return;
    }

    alreadyDone.set(Boolean.TRUE);
    if (useName) {
      ch.startElement("", getName(), getName(), new AttributesImpl());
    }

    if (!fName.equals("")) {
      contentFile(input, ch);
    } else {
      contentClass(input, ch);
    }

    if (useName) {
      ch.endElement("", getName(), getName());
    }
  }

  protected static final XMLReader getXMLReaderInstance(Element config)
      throws ClassNotFoundException, NoSuchMethodException, InstantiationException,
      IllegalAccessException, java.lang.reflect.InvocationTargetException {
    String className = DOMUtils.getAttribute(config, CLASS_NAME_ATTR, true);
    Class[] c = { Element.class };
    Object[] o = { config };
    return (XMLReader) Class.forName(className).getConstructor(c).newInstance(o);
  }

  public void contentClass(CharSequence input, ContentHandler ch) {

    SAXSource source = null;
    try {
      ReadableByteChannel file = NIOUtils.openFileForRead(new File(inputFile));
      source = new SAXSource( (XMLReader) UDDContent.xmlReader.get(), 
                               new NIOInputSource(file));
    } catch (IOException e) {
      log.fatal(i18n.getString("sourceNOTCreated", fName));
      e.printStackTrace();
    }

    SAXResult result = new SAXResult(ch);
    transform(source, result);

  }

  public void contentFile(CharSequence input, ContentHandler ch) {
    SAXSource source = null;
    try {
      source = new SAXSource(new InputSource(Channels.newInputStream(NIOUtils
          .openFileForRead(new File(fName)))));
    } catch (IOException e) {
      log.fatal(i18n.getString("sourceNOTCreated", fName));
      e.printStackTrace();
    }
    SAXResult result = new SAXResult(ch);
    transform(source, result);
  }

  private void transform(SAXSource source, SAXResult result) {
    try {
      TransformerFactory stf = SAXTransformerFactory.newInstance();
      Transformer transformer = stf.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      transformer.transform(source, result);
    } catch (Exception e) {
      log.fatal(i18n.getString("contentFileError"));
      e.printStackTrace();
    }
  }
}

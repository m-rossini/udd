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
package br.com.auster.udd.transform;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.xml.sax.XMLReader;

import br.com.auster.common.xml.DOMUtils;

/**
 * @author Marcos Tengelmann
 * @version $Id: File2Memory.java 47 2006-09-21 16:23:55Z rbarone $
 */
public class File2Memory extends UDDAbstractTransformation {

  protected static final String XML_READER_ELEMENT = "xml-reader";

  // rg.xml.sax.XMLReader
  private static final ThreadLocal xmlReader = new ThreadLocal() {
    protected synchronized Object initialValue() {
      return null;
    }
  };

  protected static final String CLASS_NAME_ATTR = "class-name";

  // org.w3c.Element
  private static final ThreadLocal config = new ThreadLocal() {
    protected synchronized Object initialValue() {
      return null;
    }
  };

  private static Logger log = Logger.getLogger(File2Memory.class);

  public File2Memory(Element config) throws ClassNotFoundException, NoSuchMethodException,
      InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException {

    File2Memory.config.set(config);
    config(config);
  }

  public void config(Element config) throws ClassNotFoundException, NoSuchMethodException,
      InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException {

    Element xmlReaderElt = DOMUtils.getElement(config, 
                                               XML_READER_ELEMENT,
                                               true);
    File2Memory.xmlReader.set(getXMLReaderInstance(xmlReaderElt));
  }

  private static final XMLReader getXMLReaderInstance(Element config)
      throws ClassNotFoundException, NoSuchMethodException, InstantiationException,
      IllegalAccessException, java.lang.reflect.InvocationTargetException {

    String className = DOMUtils.getAttribute(config, CLASS_NAME_ATTR, true);

    Class[] c = { Element.class };
    Object[] o = { config };
    return (XMLReader) Class.forName(className).getConstructor(c).newInstance(o);
  }
}

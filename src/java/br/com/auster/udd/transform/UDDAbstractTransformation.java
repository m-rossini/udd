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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * @author Marcos Tengelmann
 * @version $Id: UDDAbstractTransformation.java 2 2005-04-22 16:38:31Z rbarone $
 */
public abstract class UDDAbstractTransformation implements UDDTransformation {

  private Source entrada;

  private Result saida;

  private Transformer transformer;

  private TransformerFactory sft;

  private XMLReader reader;

  public void transform() throws TransformerConfigurationException, TransformerException {

    this.sft = SAXTransformerFactory.newInstance();
    this.transformer = sft.newTransformer();
    this.transformer.transform(this.entrada, this.saida);
  }

  /**
   * Retorna o TransformerFactory utilizado na Transformação
   * 
   * @return
   */
  public TransformerFactory getTransformerFactory() {
    return sft;
  }

  /**
   * Retorna o Transformer utilizado na Transformação
   * 
   * @return Transformer
   */
  public Transformer getTransformer() {
    return this.transformer;
  }

  public void setSource(Source entrada) throws Exception {

    if (entrada instanceof InputSource) {
      this.entrada = new SAXSource((InputSource) entrada);
    } else if (entrada instanceof SAXSource) {
      this.entrada = entrada;
    } else if (entrada instanceof ReadableByteChannel) {
      this.entrada = new StreamSource(Channels.newInputStream((ReadableByteChannel) entrada));
    } else if (entrada instanceof InputStream) {
      this.entrada = new StreamSource((InputStream) entrada);
    } else if (entrada instanceof Reader) {
      this.entrada = new StreamSource((Reader) entrada);
    } else if (entrada instanceof File) {
      this.entrada = new StreamSource((File) entrada);
    } else if (entrada instanceof Node) {
      this.entrada = new DOMSource((Node) entrada);
    } else if (entrada instanceof DOMSource) {
      this.entrada = entrada;
    } else {
      throw new Exception("Unsupported output type: " + entrada.getClass());
    }

    this.entrada = entrada;
  }

  public void setSource(XMLReader reader, InputSource input) {
    this.entrada = new SAXSource(reader, input);
    this.reader = reader;
  }

  public XMLReader getReader() {
    return this.reader;
  }

  public void setReader(XMLReader reader) {
    this.reader = reader;
  }

  public Source getSource() {
    return entrada;
  }

  public void setResult(Result saida) throws Exception {

    if (saida instanceof ContentHandler) {
      this.saida = new SAXResult((ContentHandler) saida);
    } else if (saida instanceof WritableByteChannel) {
      this.saida = new StreamResult(Channels.newOutputStream((WritableByteChannel) saida));
    } else if (saida instanceof OutputStream) {
      this.saida = new StreamResult((OutputStream) saida);
    } else if (saida instanceof Writer) {
      this.saida = new StreamResult((Writer) saida);
    } else if (saida instanceof File) {
      this.saida = new StreamResult((File) saida);
    } else if (saida instanceof Node) {
      this.saida = new DOMResult((Node) saida);
    } else if (saida instanceof DOMResult) {
      this.saida = saida;
    } else {
      throw new Exception("Unsupported output type: " + saida.getClass());
    }

  }

  public Result getResult() {
    return saida;
  }

}

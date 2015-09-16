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

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;


/**
 * @author Marcos Tengelmann
 * @version $Id: UDDTransformation.java 2 2005-04-22 16:38:31Z rbarone $
 */
public interface UDDTransformation {

  /**
   * Realiza a transformação, dado que o Source e o Result foram previamente
   * informados
   */
  public void transform() throws TransformerConfigurationException, TransformerException;

  /**
   * Configura a transformação dado um arquivo de configurações
   * 
   * @param config
   */
  public void config(Element config) throws ClassNotFoundException, NoSuchMethodException,
      InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException;

  /**
   * Assinala um XMLReader
   * 
   * @param reader
   */
  public void setReader(XMLReader reader);

  /**
   * Obtem o Reader que foi utilizado no setReader ou no
   * setSource(XMLReader,InputSource)
   * 
   * @return XMLReader
   */
  public XMLReader getReader();

  public void setSource(XMLReader entrada, InputSource input);

  /**
   * Define o Source da Transfornação
   * 
   * @param entrada
   */
  public void setSource(Source entrada) throws Exception;

  /**
   * Obtem o source da transformação
   * 
   * @return Source
   */
  public Source getSource();

  /**
   * Define o Result da Transformação
   * 
   * @param saida
   */
  public void setResult(Result saida) throws Exception;

  /**
   * Obtem o Result da Transformação
   * 
   * @return Result
   */
  public Result getResult();
}

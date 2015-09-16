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
package br.com.auster.udd.test;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.text.DecimalFormat;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;

import br.com.auster.common.cli.CLOption;
import br.com.auster.common.cli.OptionsParser;
import br.com.auster.common.io.IOUtils;
import br.com.auster.common.io.NIOUtils;
import br.com.auster.common.xml.DOMUtils;
import br.com.auster.common.xml.sax.NIOInputSource;

/**
 * @version $Id: UDDTest.java 2 2005-04-22 16:38:31Z rbarone $
 */
public class UDDTest {

  public static final String LOG4J_NAMESPACE_URI = "http://jakarta.apache.org/log4j/";

  public static final String CONFIGURATION_ELEMENT = "configuration";

  protected static final String XML_READER_ELEMENT = "xml-reader";

  protected static final String CLASS_NAME_ATTR = "class-name";

  public static final String SQL_CONF_PARAM = "sql-conf";

  public static final String CONF_PARAM = "xml-conf";

  public static final String INPUT_PARAM = "input";

  public static final String OUTPUT_PARAM = "output";

  protected static final CLOption[] options = {
      new CLOption(SQL_CONF_PARAM, 's', false, true, "file", "the SQL configuration file"),
      new CLOption(CONF_PARAM, 'x', true, true, "file", "the XML configuration file"),
      new CLOption(INPUT_PARAM, 'i', true, true, "file", "the input file"),
      new CLOption(OUTPUT_PARAM, 'o', true, true, "file", "the output file") };

  protected XMLReader xmlReader = null;

  protected final Logger log = Logger.getLogger(this.getClass());

  /**
   * Gets the XML configuration given by the argument "xml-conf".
   * 
   * @param args
   *          the command line arguments.
   */
  public static Element getXMLConfig(String[] args) throws Exception {
    OptionsParser parser = new OptionsParser(options, UDDTest.class, "UDD Test", true);
    parser.parse(args);

    // Parses the XML config file
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    File configFile = new File(OptionsParser.getOptionValue(CONF_PARAM));
    return dbf.newDocumentBuilder().parse(configFile).getDocumentElement();
  }

  /**
   * Configures the Log4J.
   * 
   * @param root
   *          the DOM tree root that contains all the configuration, including
   *          the Log4J configuration. Starting at this node, the element
   *          "log4j:configuration" will be searched as the root of the Log4J
   *          configuration.
   * @throws IllegalArgumentException
   *           if could not find a configuration for the Log4J.
   */
  public static void configureLog4J(Element root) throws IllegalArgumentException {
    // Configures the Log4J library
    Element log4jConfig = DOMUtils.getElement(root, LOG4J_NAMESPACE_URI, CONFIGURATION_ELEMENT,
                                              true);
    org.apache.log4j.xml.DOMConfigurator.configure(log4jConfig);
  }

  public UDDTest(Element config) throws Exception {
    // Tries to find a XMLReader other than the default, to parse the input
    this.xmlReader = getXMLReaderInstance(DOMUtils.getElement(config, XML_READER_ELEMENT, true));
    log.info("Using the following class as the XMLReader for input: "
             + xmlReader.getClass().getName());
  }

  /**
   * Given a XMLReader configuration, creates a instance of it and returns it.
   * 
   * @param config
   *          the DOM tree corresponding to the XMLReader information and
   *          configuration.
   * @return a XMLReader instance based on the information of the given element.
   *         Null if <code>config</code> is null or empty.
   */
  private static final XMLReader getXMLReaderInstance(Element config)
      throws ClassNotFoundException, NoSuchMethodException, InstantiationException,
      IllegalAccessException, java.lang.reflect.InvocationTargetException {
    String className = DOMUtils.getAttribute(config, CLASS_NAME_ATTR, true);

    Class[] c = { Element.class };
    Object[] o = { config };
    return (XMLReader) Class.forName(className).getConstructor(c).newInstance(o);
  }

  /**
   * Starts the XMLReader, reading from the input and sending the result to the
   * output.
   */
  public final void process(NIOInputSource input, Object output) throws Exception {
    Source source = new SAXSource(this.xmlReader, input);
    Result result = getResult(output);

    // Starts the input interpretation (transformation)
    SAXTransformerFactory.newInstance().newTransformer().transform(source, result);
  }

  protected Result getResult(Object output) throws Exception {
    if (output instanceof ContentHandler) {
      return new SAXResult((ContentHandler) output);
    } else if (output instanceof WritableByteChannel) {
      return new StreamResult(Channels.newOutputStream((WritableByteChannel) output));
    } else if (output instanceof OutputStream) {
      return new StreamResult((OutputStream) output);
    } else if (output instanceof Writer) {
      return new StreamResult((Writer) output);
    } else if (output instanceof File) {
      return new StreamResult((File) output);
    } else if (output instanceof Node) {
      return new DOMResult((Node) output);
    } else {
      throw new Exception("Unsupported output type: " + output.getClass());
    }
  }

  public static void main(String[] args) throws Exception {
    for (int i = 0; i < args.length; i++) {
      System.out.println("Arg #" + i + " is [" + args[i] + "]");
    }

    Element config = getXMLConfig(args);
    configureLog4J(config);
    Logger log = Logger.getLogger(UDDTest.class);

    DecimalFormat df = new DecimalFormat("#,###,###,##0");

    Runtime rt = Runtime.getRuntime();

    log.info("Processadores=>" + rt.availableProcessors());

    long fS = rt.freeMemory();
    log.info("Memória Livre =>" + df.format(fS));
    log.info("Memória Máxima=>" + df.format(rt.maxMemory()));
    log.info("Memória Total =>" + df.format(rt.totalMemory()));

    long sTime = System.currentTimeMillis();
    log.info("Configuring the UDD...");
    UDDTest test = new UDDTest(config);
    long eTime = System.currentTimeMillis();
    log.info("Done in " + (eTime - sTime) + " miliseconds.");

    log.info("Opening input.");
    NIOInputSource input = new NIOInputSource(NIOUtils.openFileForRead(new File(OptionsParser
        .getOptionValue(INPUT_PARAM))));
    log.info("Setting encoding to 'ISO-8859-1'.");
    input.setEncoding("ISO-8859-1");
    log.info("Opening output.");
    OutputStream output = IOUtils.openFileForWrite(new File(OptionsParser
        .getOptionValue(OUTPUT_PARAM)), false);

    log.info("Running transformation.");
    sTime = System.currentTimeMillis();
    test.process(input, new OutputStreamWriter(output, "ISO-8859-1"));
    eTime = System.currentTimeMillis();

    long fE = rt.freeMemory();
    log.info("Memória Livre =>" + df.format(fE));
    log.info("Memória Consumida =>" + df.format((fS - fE)));

    log.info("Closing output.");
    output.close();
    log.info("Done in " + (eTime - sTime) + " miliseconds.");
  }
}

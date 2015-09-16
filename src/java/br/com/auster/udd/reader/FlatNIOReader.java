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
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.security.GeneralSecurityException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import br.com.auster.common.io.NIOBufferUtils;
import br.com.auster.common.io.NIOUtils;
import br.com.auster.common.util.I18n;
import br.com.auster.common.xml.DOMUtils;
import br.com.auster.common.xml.sax.NIOInputSource;

/**
 * This class is used to read data from input and send SAX events related to
 * these data to the content handler. It will separate records read by a
 * separator or by a fixed record length. The abstract method
 * <code>processRecord</code> will process the records found.
 * 
 * @version $Id: FlatNIOReader.java 44 2006-09-18 19:54:56Z rbarone $
 */
public abstract class FlatNIOReader implements XMLReader {

  protected static final String UDD_PATH_ATTR = "udd-path";

  protected static final String ENCRYPTED_ATTR = "encrypted";

  protected static final String ENCODING_FROM_ATTR = "encoding-from";

  protected static final String MAX_LINE_SIZE_ATTR = "max-record-size";

  protected static final String BUFFER_SIZE_ATTR = "buffer-size";

  protected static final String SEPARATOR_CHAR_ATTR = "separator-char";
  
  protected static final String ESCAPE_CHAR_ATTR = "escape-char";

  protected static final String LENGTH_CHAR_ATTR = "record-length";

  protected static final int END_OF_BUFFER = -1;

  // Instance variables
  protected ContentHandler handler;

  protected DTDHandler dtdHandler;

  protected EntityResolver resolver;

  protected ErrorHandler errorHandler;

  protected final CharsetDecoder decoder;

  protected final ByteBuffer bb;

  protected final CharBuffer cbRecord, cbToProcess;

  protected char separateChar, escapeChar;
  
  private final boolean isEscapeDefined;

  protected int length;
  
  private boolean lastOneWasEscape = false;

  protected final Logger log = Logger.getLogger(this.getClass());

  private final I18n i18n = I18n.getInstance(FlatNIOReader.class);

  public FlatNIOReader(Element config) throws ParserConfigurationException, SAXException,
      IOException {
    // Creates the UDD DOM tree
    String uddFileName = null;
    try {
      uddFileName = DOMUtils.getAttribute(config, UDD_PATH_ATTR, true);
      final boolean isEncrypted = 
        DOMUtils.getBooleanAttribute(config, ENCRYPTED_ATTR, true);
      config = DOMUtils.openDocument(uddFileName, isEncrypted);
    } catch (IllegalArgumentException e) {
      log.warn(i18n.getString("usingNoUDDFile"));
    } catch (GeneralSecurityException ge) {
      log.warn(i18n.getString("decryptionFailed", uddFileName));
    }

    log.debug("Loading " + this.getClass() + " configuration.");
    // Charset conversion
    final Charset charset = Charset
        .forName(DOMUtils.getAttribute(config, ENCODING_FROM_ATTR, true));
    
    // Create a decoder from it
    decoder = charset.newDecoder();
    decoder.onMalformedInput(CodingErrorAction.IGNORE);
    decoder.onUnmappableCharacter(CodingErrorAction.IGNORE);

    // Buffer to hold bytes
    bb = ByteBuffer.allocateDirect(DOMUtils.getIntAttribute(config, BUFFER_SIZE_ATTR, true));
    // Buffer to hold characters
    final int averageCharsPerByte = Math.round(decoder.averageCharsPerByte()) + 1;
    cbRecord = CharBuffer.allocate(DOMUtils.getIntAttribute(config, BUFFER_SIZE_ATTR, true)
                                   * averageCharsPerByte);
    cbToProcess = CharBuffer.allocate(DOMUtils.getIntAttribute(config, MAX_LINE_SIZE_ATTR, true)
                                      * averageCharsPerByte);

    final String separator = config.getAttribute(SEPARATOR_CHAR_ATTR);
    final String length = config.getAttribute(LENGTH_CHAR_ATTR);

    if (separator.length() > 0) {
      this.separateChar = StringEscapeUtils.unescapeJava(separator).charAt(0);
    } else if (length.length() > 0) {
      this.length = Integer.parseInt(length);
    } else {
      throw new SAXException(i18n.getString("noRecordLS"));
    }
    
    final String escape = config.getAttribute(ESCAPE_CHAR_ATTR);
    if (escape.length() > 0) {
    	this.escapeChar = StringEscapeUtils.unescapeJava(escape).charAt(0);
    	this.isEscapeDefined = true;
    } else {
    	this.isEscapeDefined = false;
    }

    // Parses the config
    log.debug("Parsing the UDD configuration from file " + uddFileName);
    this.parseUDD(config, uddFileName);
  }

  /**
   * Parses a DOM tree that contains the UDD configuration.
   * 
   * @param uddConf
   *          the root DOM tree with the UDD configuration.
   * @param uddFileName
   *          the file path that contains the <code>uddConf</code> tree. It
   *          may be null, if it was not specified.
   */
  protected abstract void parseUDD(Element uddConf, String uddFileName) throws SAXException;

  /**
   * Process a record found.
   * 
   * @param handler
   *          the content handler used to output the SAX events.
   * @param cb
   *          the buffer that contains the record found.
   */
  protected abstract void processRecord(ContentHandler handler, CharBuffer cb) throws SAXException;

  /**
   * Method called at the beginning of the document, after a call to
   * <code>handler.startDocument()</code>.
   * 
   * @param handler
   *          the content handler used to output the SAX events.
   */
  protected abstract void startDocument(ContentHandler handler) throws SAXException;

  /**
   * Method called at the end of the document, before a call to
   * <code>handler.endDocument()</code>.
   * 
   * @param handler
   *          the content handler used to output the SAX events.
   */
  protected abstract void endDocument(ContentHandler handler) throws SAXException;

  /**
   * Runs the parser itself
   */
  public void parse(InputSource inputSource) throws IOException, SAXException {
    // Initializes the buffers and decoder
    decoder.reset();
    bb.clear();
    cbToProcess.clear();
    this.lastOneWasEscape = false;

    // Get an efficient reader for the file
    final ReadableByteChannel input = ((NIOInputSource) inputSource).getReadableByteChannel();

    // Read the file and display it's contents.
    if (handler == null) {
      throw new SAXException(i18n.getString("noCH"));
    }

    // Note: We're ignoring setDocumentLocator(), as well
    handler.startDocument();
    this.startDocument(this.handler);

    if (this.separateChar != '\0') {
      this.separateUsingSeparator(input);
    } else if (this.length > 0) {
      this.separateUsingSubstring(input);
    } else {
      throw new SAXException(i18n.getString("noRecordLS"));
    }

    // finishes the decoding operation.
    // according to the CharsetDecoder documentation, this should
    // be done always (even if there is nothing else to be read)
    // it will throw an error if there are any malformed remaining chars
    bb.flip(); // Flip byte buffer to prepare for decoding
    decode(bb, cbRecord, true); // Decode bytes into characters (endOfInput)
    cbToProcess.put(cbRecord.array(), 0, cbRecord.length());

    // flushes any remaining chars until there is nothing left
    // (as requested by the CharsetDecoder class)
    CoderResult result = null;
    do {
      cbRecord.clear();
      result = decoder.flush(cbRecord);
      if (result.isError()) {
        result.throwException();
      }
      cbRecord.flip();
      cbToProcess.put(cbRecord.array(), 0, cbRecord.length());
    } while (result.isOverflow());

    // Finishes the handler
    cbToProcess.flip();
    
    this.processRecord(this.handler, cbToProcess);

    this.endDocument(this.handler);
    this.handler.endDocument();
  }

  /**
   * Each record will be defined by a separator.
   * <p>
   * <b>IMPORTANT:</b> this implementation will NOT handle escape chars
   * (actually the configuration of <code>FlatNIOReader</code> does not even
   * support an escape char for the record separator!
   * </p>
   * 
   * @param input
   *          The channel from which the records will be searched - it must be
   *          open and ready for a series of <code>read</code> operations.
   */
  protected final void separateUsingSeparator(ReadableByteChannel input) throws IOException,
      CharacterCodingException, SAXException {
  	
    while (NIOUtils.read(input, this.bb) > 0) {
      decode(this.bb, this.cbRecord, false);

      int i = NIOBufferUtils.findToken(this.cbRecord, this.separateChar);      
      while (i >= 0) {
      	final CharBuffer buffer = this.cbRecord.duplicate();
      	if (this.isEscapeDefined && i > 0 && this.cbRecord.get(i - 1) == this.escapeChar) {
      		final int count = NIOBufferUtils.countAdjacentOccurrencesBackwards(this.cbRecord, 
      		      						                                                 this.escapeChar, 
      		      						                                                 i - 1);
      		if (count % 2 != 0) {
      			buffer.limit(i + 1);
      			this.cbToProcess.put(buffer);
      			this.cbRecord.position(i + 1);
      			i = NIOBufferUtils.findToken(this.cbRecord, this.separateChar);
      			continue;
      		}
      	}
        buffer.limit(i);
        if (this.cbToProcess.position() > 0) {
          // there is some data in cbToProcess, so we need to use it
          this.cbToProcess.put(buffer);
          this.cbToProcess.flip();
          processRecord(this.handler, this.cbToProcess);
          this.cbToProcess.clear();
        } else {
          // no data left in cbToProcess, so let's send a view of
          // the original buffer in order to gain performance
          processRecord(this.handler, buffer.slice());
        }
        this.cbRecord.position(i + 1);
        i = NIOBufferUtils.findToken(this.cbRecord, this.separateChar);
      }

      this.cbToProcess.put(this.cbRecord);
    }
  }

  /**
   * Each record will be defined by a fixed input size.
   */
  protected final void separateUsingSubstring(ReadableByteChannel input) throws IOException,
      CharacterCodingException, SAXException {
    int recordSize = 0;
    while (NIOUtils.read(input, bb) > 0) {
      decode(bb, cbRecord, false); // Decode bytes into characters

      if ((recordSize + cbRecord.remaining()) > this.length) {
      	
      	final int lengthToProcess = this.length - recordSize;
        cbToProcess.put(cbRecord.array(), 0, lengthToProcess);
        cbToProcess.flip();
        
        this.processRecord(this.handler, cbToProcess);
        cbToProcess.clear();
        cbToProcess.put(cbRecord.array(), 
                        lengthToProcess, 
                        cbRecord.remaining() - lengthToProcess);
        recordSize = cbRecord.remaining() - lengthToProcess;
      } else {
        cbToProcess.put(cbRecord);
        recordSize += cbRecord.remaining();
      }
    }
  }

  /** ************************************** */
  /* Start of the XMLReader implementation */
  /** ************************************** */
  public ContentHandler getContentHandler() {
    return handler;
  }

  public void setContentHandler(ContentHandler contentHandler) {
    this.handler = contentHandler;
  }

  public DTDHandler getDTDHandler() {
    return dtdHandler;
  }

  public void setDTDHandler(DTDHandler dtdHandler) {
    this.dtdHandler = dtdHandler;
  }

  public EntityResolver getEntityResolver() {
    return resolver;
  }

  public void setEntityResolver(EntityResolver entityResolver) {
    this.resolver = entityResolver;
  }

  public ErrorHandler getErrorHandler() {
    return errorHandler;
  }

  public void setErrorHandler(ErrorHandler errorHandler) {
    this.errorHandler = errorHandler;
  }

  public boolean getFeature(String str) throws SAXNotRecognizedException, SAXNotSupportedException {
    return false;
  }

  public Object getProperty(String str) throws SAXNotRecognizedException, SAXNotSupportedException {
    return null;
  }

  public void parse(String str) throws IOException, SAXException {
    this.parse(new InputSource(str));
  }

  public void setFeature(String str, boolean param) throws SAXNotRecognizedException,
      SAXNotSupportedException {
  }

  public void setProperty(String str, Object obj) throws SAXNotRecognizedException,
      SAXNotSupportedException {
  }

  /** ************************************ */
  /* End of the XMLReader implementation */
  /** ************************************ */

  /**
   * Decodes the byte buffer into char buffer using the decoder. This method
   * also tries to decide if there is a escape character at the end of the
   * buffer.
   * 
   * @param bb
   *            the byte buffer to decode.
   * @param cb
   *            the char buffer decoded.
   * @param endOfInput
   *            true if there is no more data to be read.
   */
  private final void decode(ByteBuffer bb, CharBuffer cb, boolean endOfInput)
      throws CharacterCodingException {
    cb.clear();
    CoderResult result = decoder.decode(bb, cb, endOfInput);
    cb.flip();
    bb.compact();
    if (result.isError()) {
      // handle malformed-input or an unmappable-character here...
      result.throwException();
    }
  }

}

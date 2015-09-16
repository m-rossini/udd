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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import br.com.auster.common.util.I18n;
import br.com.auster.common.xml.DOMUtils;


/**
 * This is the super class for the UDD nodes. Instances of this class may be
 * used concurrently.
 * 
 * @version $Id: UDDNode.java 47 2006-09-21 16:23:55Z rbarone $
 */
public abstract class UDDNode {
  
  /**
   * {@value}
   */
  public static final String REPLACE_MALFORMED_ATT = "replace-invalid-chars";
  
  /**
   * {@value}
   */
  public static final char REPLACEMENT_CHAR = '?';
  
  /**
   * {@value}
   */
  public static final ThreadLocal EMPTY_CB = new ThreadLocal() {
    protected synchronized Object initialValue() {
      return CharBuffer.wrap(new char[0]);
    }
  };

  protected static final Logger log = Logger.getLogger(UDDNode.class);
  
  // The instance variables
  protected final String name;

  protected final int start, length, end;
  
  protected boolean trimElement;
  
  protected int computedLength = -1;
  
  protected final boolean replaceMalformed;
  
  protected char escapeChar;
  
  protected final boolean isEscapeDefined;

  private final I18n i18n = I18n.getInstance(UDDNode.class);

  public UDDNode(final Element root) {
    this(root, null);
  }
  
  public UDDNode(final Element root, final UDDNode parent) {
    this.name = root.getAttribute("name");
    String start = root.getAttribute("start");
    String length = root.getAttribute("size");
    String end = root.getAttribute("end");
    String trim = root.getAttribute("trim");

    //Decides about trimming data.
    this.trimElement=false;
    if ( (trim.length() == 0) || (!trim.toLowerCase().equals("false")) ) {
    	this.trimElement=true;
    }
    
    // Decides how to process the text
    if (start.length() > 0) {
      this.start = Integer.parseInt(start) - 1;
    }  else {
      this.start = 0;
    }
    if (end.length() > 0) {
      this.end = Integer.parseInt(end);
      this.computedLength = this.end - this.start;
    } else {
      this.end = -1;
    }
    if (length.length() > 0) {
      this.computedLength = this.length = Integer.parseInt(length);
    } else {
      this.length = -1;
    }
    
    final boolean replaceDefalut = parent == null ? false : parent.replaceMalformed;
    this.replaceMalformed = 
      DOMUtils.getBooleanAttribute(root, REPLACE_MALFORMED_ATT, replaceDefalut);
    
    final String escape = root.getAttribute("escape");
    if (escape.length() > 0) {
    	this.escapeChar = StringEscapeUtils.unescapeJava(escape).charAt(0);
    	this.isEscapeDefined = true;
    } else if (parent != null && parent.isEscapeDefined) {
    	this.escapeChar = parent.escapeChar;
    	this.isEscapeDefined = true;
    } else {
    	this.isEscapeDefined = false;
    }
  }

  /**
   * Gets the specified substring of the charbuffer, using the attributes
   * 'start', 'end' and 'length'.
   */
  protected final CharBuffer getSubstring(CharBuffer input) {
    if (this.start == 0 && this.computedLength < 0) {
      // indexed attribute - no start/length provided
      return input;
    } else if (input.length() < this.start) {
      // content is smaller than attribute start position
      return (CharBuffer) EMPTY_CB.get();
    }
    
    // determine end index
    int endIndex = this.start + this.computedLength;
    if (this.computedLength < 0 || input.length() < endIndex) {
      endIndex = input.length();
    }
    
    CharBuffer view = input.duplicate();
    int pos = (this.start < 0)? 0 : this.start;
    view.position(pos).limit(endIndex);
    return view.slice();
  }

  /**
   * Gets the value of the attribute <code>name</code>.
   */
  public final String getName() {
    return this.name;
  }
  
  /***
   * Returns the internally computed lenght of this Node.
   * @return
   */
  public int getComputedLenght() {
  	return this.computedLength;
  }
  
  public int getStart() {
  	return this.start;
  }
  
  public int getEnd() {
  	return this.end;
  }
  
  public String toString() {
  	return this.getClass().getName() + "[" + this.getName() + "]";
  }
}

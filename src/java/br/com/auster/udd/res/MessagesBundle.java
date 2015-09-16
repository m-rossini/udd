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
package br.com.auster.udd.res;

import java.util.ListResourceBundle;

/**
 * @version $Id: MessagesBundle.java 19 2006-02-14 12:19:18Z framos $
 */
public class MessagesBundle extends ListResourceBundle {
	public Object[][] getContents() {
		return contents;
	}

	static final Object[][] contents = {
			// UDDContent
			{"wrongMode",
				  "The specified attribute mode has an invalid value of {0}.Assuming many."},			
			{"contentFileError",
				  "Unable insert file content."},
			{"sourceNOTCreated",
				  "Unable to create SAX Source from file {0}."},			
			{"onlyOne",
				  "You can specify a file or a class as Content, not both."},
			{"noParms",
				  "You must specify a file or a class as Content."},
        
			// UDDNode.java
			{"defineFormat",
					"[ TAG = {0}, NAME = {1} ] -> you must define the attribute 'format' when using type='number'."},
			{"defineFormatParse",
					"[ TAG = {0}, NAME = {1} ] -> you must define the attributes 'format' and 'parse' when using type='date'."},
			{"defineFormatParseNow",
					"[ TAG = {0}, NAME = {1} ] -> you cannot define the attribute 'parse' when using type='now'."},					

			// FlatNIOReader.java
			{"usingNoUDDFile",
					"Using no external UDD file. Assuming info about it is inside the main XML config."},
			{"noRecordLS",
					"The 'document' tag must have attribute 'record-length' or 'separator-char'."},
			{"noCH", "No content handler defined."},
			{"problemSeparator",
					"Problems looking for separator in input: {0}."},
      {"decryptionFailed",
          "Problems decrypting UDD file descriptor: {0}."},
  
      // TaggedFileReader.java
      {"keyAlreadyDefined",
          "The key {0} was already defined at block {1}."},
      {"foundKeyAfter",
          "The key {0} was found in a position different than the configuration for the block {2}."},
      {"keyNotDefined",
          "Found the key {0} that was not defined inside the block {1}."},
      {"couldNotFindKey",
          "Could not find the key {0} inside the data-definition for some block."},
      {"taggedFileLine", "TaggedFile - (line: {0}) {1}"}
  };
  
}
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
 * @version $Id: MessagesBundle_pt_BR.java 33 2006-06-08 02:46:06Z rbarone $
 */
public class MessagesBundle_pt_BR extends ListResourceBundle {
	public Object[][] getContents() {
		return contents;
	}

	static final Object[][] contents = {
			// UDDContent
			{"wrongMode",
				  "O Atributo mode especificado tem um valor inv�lido de {0}.Assumindo many"},			
			{"contentFileError",
				  "N�o foi possivel incluir conteudo do arquivo."},			
			{"sourceNOTCreated",
				  "N�o foi poss�vel criar o SAX Source do arquivo {0}."},			
			{"onlyOne",
				  "Voc� pode especificar um arquivo ou uma classe como Conte�do, n�o ambos."},
			{"noParms",
				  "Uma classe ou um arquivo deve ser especificado como conte�do."},
          
			// UDDNode.java
			{"defineFormat",
					"[ TAG = {0}, NAME = {1} ] -> voc� precisa definir o atributo 'format' quando � utilizado tipo='number'."},
			{"defineFormatParse",
					"[ TAG = {0}, NAME = {1} ] -> voc� precisa definir os atributos 'format' e 'parse' quando � utilizado tipo='date'."},
			{"defineFormatParseNow",
					"[ TAG = {0}, NAME = {1} ] -> voc� n�o pode definir o atributo 'parse' quando � utilizado tipo='now'."},

			// FlatNIOReader.java
			{"usingNoUDDFile",
					"N�o ser� usado nenhum arquivo UDD externo para essa inst�ncia. Assumindo que as informa��es necess�rias estar�o no mesmo XML de configura��o principal."},
			{"noRecordLS",
					"A tag 'document' precisa ter os atributos 'record-length' ou 'separator-char'."},
			{"noCH", "N�o h� content handler definido."},
			{"problemSeparator",
					"Problemas ao procurar o separador na entrada: {0}."},
      {"decryptionFailed",
          "Problemas ao decriptografar o descritor UDD: {0}."},
                     
      // TaggedFileReader.java
      {"keyAlreadyDefined", "A chave {0} j� foi definida no bloco {1}."},
      {"foundKeyAfter",
           "A chave {0} foi encontrada em uma posi��o diferente da configurada para o bloco {1}."},
      {"keyNotDefined",
           "Encontrada a chave {0} que n�o foi definida dentro do bloco {1}."},
      {"couldNotFindKey",
           "N�o foi poss�vel encontrar a chave {0} dentro das defini��es de chaves para algum dos blocos."},
      {"taggedFileLine", "TaggedFile - (linha: {0}) {1}"}
  };                     
}
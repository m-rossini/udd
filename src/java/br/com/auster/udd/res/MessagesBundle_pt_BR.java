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
				  "O Atributo mode especificado tem um valor inválido de {0}.Assumindo many"},			
			{"contentFileError",
				  "Não foi possivel incluir conteudo do arquivo."},			
			{"sourceNOTCreated",
				  "Não foi possível criar o SAX Source do arquivo {0}."},			
			{"onlyOne",
				  "Você pode especificar um arquivo ou uma classe como Conteúdo, não ambos."},
			{"noParms",
				  "Uma classe ou um arquivo deve ser especificado como conteúdo."},
          
			// UDDNode.java
			{"defineFormat",
					"[ TAG = {0}, NAME = {1} ] -> você precisa definir o atributo 'format' quando é utilizado tipo='number'."},
			{"defineFormatParse",
					"[ TAG = {0}, NAME = {1} ] -> você precisa definir os atributos 'format' e 'parse' quando é utilizado tipo='date'."},
			{"defineFormatParseNow",
					"[ TAG = {0}, NAME = {1} ] -> você não pode definir o atributo 'parse' quando é utilizado tipo='now'."},

			// FlatNIOReader.java
			{"usingNoUDDFile",
					"Não será usado nenhum arquivo UDD externo para essa instância. Assumindo que as informações necessárias estarão no mesmo XML de configuração principal."},
			{"noRecordLS",
					"A tag 'document' precisa ter os atributos 'record-length' ou 'separator-char'."},
			{"noCH", "Não há content handler definido."},
			{"problemSeparator",
					"Problemas ao procurar o separador na entrada: {0}."},
      {"decryptionFailed",
          "Problemas ao decriptografar o descritor UDD: {0}."},
                     
      // TaggedFileReader.java
      {"keyAlreadyDefined", "A chave {0} já foi definida no bloco {1}."},
      {"foundKeyAfter",
           "A chave {0} foi encontrada em uma posição diferente da configurada para o bloco {1}."},
      {"keyNotDefined",
           "Encontrada a chave {0} que não foi definida dentro do bloco {1}."},
      {"couldNotFindKey",
           "Não foi possível encontrar a chave {0} dentro das definições de chaves para algum dos blocos."},
      {"taggedFileLine", "TaggedFile - (linha: {0}) {1}"}
  };                     
}
/* 
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.i49.spine.common;

import java.io.OutputStream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stax.StAXResult;

import org.w3c.dom.Document;

public class HtmlDocumentWriter extends DocumentWriter {
    
    private final Transformer transformer;
    
    public HtmlDocumentWriter() throws Exception {
        this.transformer = createHtmlTransformer();
    }
    
    @Override
    protected void writeDocumentTo(OutputStream stream, Document doc) throws Exception {
        String encoding = getEncoding();
        XMLStreamWriter writer = createHtmlStreamWriter(stream, encoding);
        this.transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
        this.transformer.transform(new DOMSource(doc), new StAXResult(writer));
    }
    
    private static Transformer createHtmlTransformer() throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        return transformer;
    }
    
    private XMLStreamWriter createXmlStreamWriter(OutputStream stream, String encoding) throws XMLStreamException, FactoryConfigurationError {
        return XMLOutputFactory.newFactory().createXMLStreamWriter(stream, encoding);
    }

    private XMLStreamWriter createHtmlStreamWriter(OutputStream stream, String encoding) throws XMLStreamException, FactoryConfigurationError {
        return new HtmlStreamWriter(createXmlStreamWriter(stream, encoding));
    }
}

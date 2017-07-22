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
package com.github.i49.spine.common;

import static com.github.i49.spine.common.HtmlSpec.isVoid;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class HtmlStreamWriter extends DelegatingXmlStreamWriter {
    
    private static final String DOCTYPE_DECLARATION = "<!DOCTYPE html>\n";
    
    private String currentElement;

    public HtmlStreamWriter(XMLStreamWriter delegate) {
        super(delegate);
    }

    @Override
    public void writeStartElement(String localName) throws XMLStreamException {
        localName = normalizeElementName(localName);
        if (isVoid(localName)) {
            super.writeEmptyElement(localName);
        } else {
            super.writeStartElement(localName);
        }
        this.currentElement = localName;
    }

    @Override
    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        localName = normalizeElementName(localName);
        if (isVoid(localName)) {
            super.writeEmptyElement(namespaceURI, localName);
        } else {
            super.writeStartElement(namespaceURI, localName);
        }
        this.currentElement = localName;
    }

    @Override
    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        localName = normalizeElementName(localName);
        if (isVoid(localName)) {
            super.writeEmptyElement(prefix, localName, namespaceURI);
        } else {
            super.writeStartElement(prefix, localName, namespaceURI);
        }
        this.currentElement = localName;
    }

    @Override
    public void writeEndElement() throws XMLStreamException {
        if (this.currentElement == null || !isVoid(this.currentElement)) {
            super.writeEndElement();
        }
        this.currentElement = null;
    }

    @Override
    public void writeStartDocument() throws XMLStreamException {
        this.currentElement = null;
        writeDoctype();
    }

    @Override
    public void writeStartDocument(String version) throws XMLStreamException {
        writeStartDocument();
    }

    @Override
    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        writeStartDocument();
    }
    
    private void writeDoctype() throws XMLStreamException {
        writeDTD(DOCTYPE_DECLARATION);
    }
    
    private String normalizeElementName(String localName) {
        return localName.toLowerCase();
    }
}

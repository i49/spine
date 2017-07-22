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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;

public class Documents {

    private static final ThreadLocal<DocumentBuilder> builders = ThreadLocal.withInitial(Documents::createBuilder);
    
    public static Document create() {
        return builders.get().newDocument();
    }
    
    public static Document copy(Document original) throws Exception {
        TransformerFactory f = TransformerFactory.newInstance();
        Transformer transformer = f.newTransformer();
        DOMSource source = new DOMSource(original.getDocumentElement());
        Document doc = create();
        DOMResult target = new DOMResult(doc);
        transformer.transform(source, target);
        doc.setDocumentURI(original.getDocumentURI());
        return doc;
    }
    
    private static DocumentBuilder createBuilder() {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        try {
            DocumentBuilder b = f.newDocumentBuilder();
            return b;
        } catch (ParserConfigurationException e) {
            return null;
        }
    }
    
    private Documents() {
    }
}

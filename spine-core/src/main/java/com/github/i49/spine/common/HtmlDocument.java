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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import io.github.i49.cascade.api.Selector;
import io.github.i49.cascade.api.SelectorCompiler;

public class HtmlDocument {

    private final Document doc;
    private final SelectorCompiler compiler;

    private final Element html;
    private final Element head;

    public static HtmlDocument of(Document doc) {
        return new HtmlDocument(doc);
    }

    private HtmlDocument(Document doc) {
        this.doc = doc;
        this.compiler = SelectorCompiler.create();

        this.html = this.doc.getDocumentElement();
        toLowerCase(this.doc, this.html);
        this.head = find("head");
    }

    public Document getDocument() {
        return doc;
    }

    public HtmlDocument addMetaCharset(String value) {
        Element meta = doc.createElementNS(HtmlSpec.NAMESPACE_URL, "meta");
        meta.setAttribute("charset", value);
        this.head.insertBefore(meta, this.head.getFirstChild());
        return this;
    }

    public Element find(String expression) {
        List<Element> elements = select(expression);
        return elements.size() > 0 ? elements.get(0) : null;
    }

    public HtmlDocument remove(String expression) {
        for (Element element: select(expression)) {
            Node parent = element.getParentNode();
            if (parent != null) {
                parent.removeChild(element);
            }
        }
        return this;
    }

    public HtmlDocument removeAttributesWithPrefix(String prefix) {
        List<Attr> attributes = new ArrayList<>();
        visitAttributes(this.html, a->{
            if (a.getName().startsWith(prefix)) {
                attributes.add(a);
            }
        });
        removeAttributes(attributes);
        return this;
    }

    public HtmlDocument removeDataAttributes() {
        return removeAttributesWithPrefix("data-");
    }

    public HtmlDocument unwrap(String expression) {
        for (Element wrapper: select(expression)) {
            Node parent = wrapper.getParentNode();
            while (wrapper.hasChildNodes()) {
                parent.insertBefore(wrapper.getFirstChild(), wrapper);
            }
            parent.removeChild(wrapper);
       }
        return this;
    }

    private static void toLowerCase(Document doc, Element html) {
        visitNodes(html, node->{
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element)node;
                String oldName = e.getLocalName();
                String newName = oldName.toLowerCase();
                if (!oldName.equals(newName)) {
                    doc.renameNode(e, e.getNamespaceURI(), newName);
                }
            }
        });
    }

    private List<Element> select(String expression) {
        Selector selector = compiler.compile(expression);
        return selector.select(doc.getDocumentElement());
    }

    private static void visitNodes(Node root, Consumer<Node> visitor) {
        if (root == null) {
            return;
        }
        visitor.accept(root);
        Node child = root.getFirstChild();
        while (child != null) {
            Node next = child.getNextSibling();
            visitNodes(child, visitor);
            child = next;
        }
    }

    private static void visitAttributes(Node root, Consumer<Attr> visitor) {
        visitNodes(root, node->{
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element)node;
                NamedNodeMap attrs = e.getAttributes();
                for (int i = 0; i < attrs.getLength(); i++) {
                    Attr a = (Attr)attrs.item(i);
                    visitor.accept(a);
                }
            }
        });
    }

    private static void removeAttributes(List<Attr> attributes) {
        for (Attr a: attributes) {
            Element e = a.getOwnerElement();
            if (e != null) {
                e.removeAttributeNode(a);
            }
        }
    }
}

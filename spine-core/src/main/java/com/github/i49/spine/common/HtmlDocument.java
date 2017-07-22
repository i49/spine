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
import java.util.function.Predicate;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class HtmlDocument {
    
    private final Document doc;
    private final Element html;
    private final Element head;
    private final Element body;
    
    public static HtmlDocument of(Document doc) {
        return new HtmlDocument(doc);
    }
    
    private HtmlDocument(Document doc) {
        this.doc = doc;
        this.html = this.doc.getDocumentElement();
        this.head = getHead();
        this.body = getBody();
    }  
    
    public HtmlDocument addMetaCharset(String value) {
        Element meta = doc.createElementNS(HtmlSpec.NAMESPACE_URL, "meta");
        meta.setAttribute("charset", value);
        this.head.insertBefore(meta, this.head.getFirstChild());
        return this;
    }
    
    public Element find(String expression) {
        List<Element> elements = findElements(expression);
        return elements.size() > 0 ? elements.get(0) : null;
    }
    
    public Document getDocument() {
        return doc;
    }
    
    public HtmlDocument remove(String element) {
        removeNodes(findElements(element));
        return this;
    }
    
    public HtmlDocument removeContainingClass(String className) {
        List<Element> found = findElements(this.body, e->{
            if (!e.hasAttribute("class")) {
                return false;
            }
            for (String value: e.getAttribute("class").split("\\s")) {
                if (value.equals(className)) {
                    return true;
                }
            }
            return false;
        });
        removeNodes(found);
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
    
    public HtmlDocument toLowerCase() {
        visitNodes(this.html, node->{
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element)node;
                String oldName = e.getLocalName();
                String newName = oldName.toLowerCase();
                if (!oldName.equals(newName)) {
                    doc.renameNode(e, e.getNamespaceURI(), newName);
                }
            }
        });
        return this;
    }
    
    public HtmlDocument unwrap(String localName) {
        for (Element wrapper: findElements(localName)) {
            unwrap(wrapper);
        }
        return this;
    }
    
    private Element getHead() {
        return getDirectChildOfRoot("head");
    }
    
    private Element getBody() {
        return getDirectChildOfRoot("body");
    }
    
    private Element getDirectChildOfRoot(String localName) {
        Element html = doc.getDocumentElement();
        for (Node node = html.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element)node;
                if (e.getLocalName().equalsIgnoreCase(localName)) {
                    return e;
                }
            }
        }
        return null;
    }
    
    private List<Element> findElements(String expression) {
        List<Element> elements = new ArrayList<>();
        if (expression.contains("#")) {
            String[] parts = expression.split("#");
            String tagName = parts[0];
            String id = parts[1];
            Element found = doc.getElementById(id);
            if (found != null && (tagName.isEmpty() || found.getLocalName().equals(tagName))) { 
                elements.add(found);
            }
            return elements;
        }
        NodeList list = doc.getElementsByTagNameNS("*", expression);
        for (int i = 0; i < list.getLength(); i++) {
            elements.add((Element)list.item(i));
        }
        return elements;
    }
    
    private List<Element> findElements(Element root, Predicate<Element> predicate) {
        List<Element> found = new ArrayList<>();
        visitNodes(root, node->{
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element)node;
                if (predicate.test(e)) {
                    found.add(e);
                }
            }
        });
        return found;
    }
    
    private void visitNodes(Node root, Consumer<Node> visitor) {
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
    
    private void visitAttributes(Node root, Consumer<Attr> visitor) {
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
    
    private void removeNodes(List<? extends Node> nodes) {
        for (Node node: nodes) {
            Node parent = node.getParentNode();
            if (parent != null) {
                parent.removeChild(node);
            }
        }
    }
    
    private void removeAttributes(List<Attr> attributes) {
        for (Attr a: attributes) {
            Element e = a.getOwnerElement();
            if (e != null) {
                e.removeAttributeNode(a);
            }
        }
    }

    private static void unwrap(Node wrapper) {
        Node parent = wrapper.getParentNode();
        while (wrapper.hasChildNodes()) {
            parent.insertBefore(wrapper.getFirstChild(), wrapper);
        }
        parent.removeChild(wrapper);
    }
}

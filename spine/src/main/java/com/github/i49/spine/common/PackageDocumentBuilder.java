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

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PackageDocumentBuilder {

    private String identifier;
    private String title;
    private String language;
    private OffsetDateTime lastModified;
    private List<String> authors = Collections.emptyList(); 
    private String rights;
    
    private List<Path> pages = Collections.emptyList();
    private Set<Path> resources = Collections.emptySet();
    
    private static final DateTimeFormatter ISO8601_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public PackageDocumentBuilder() {
    }
    
    public PackageDocumentBuilder identifier(String identifier) {
        if (identifier != null) {
            this.identifier = identifier;
        }
        return this;
    }

    public PackageDocumentBuilder title(String title) {
        if (title != null) {
            this.title = title;
        }
        return this;
    }
    
    public PackageDocumentBuilder language(String language) {
        if (language != null) {
            this.language = language;
        }
        return this;
    }
    
    public PackageDocumentBuilder lastModified(OffsetDateTime dateTime) {
        if (dateTime != null) {
            this.lastModified = dateTime;
        }
        return this;
    }
    
    public PackageDocumentBuilder authors(List<String> authors) {
        if (authors != null) {
            this.authors = authors;
        }
        return this;
    }
    
    public PackageDocumentBuilder rights(String rights) {
        if (rights != null) {
            this.rights = rights;
        }
        return this;
    }
    
    public PackageDocumentBuilder pages(List<Path> pages) {
        this.pages = pages;
        return this;
    }
    
    public PackageDocumentBuilder resoures(Set<Path> resources) {
        this.resources = resources;
        return this;
    }
 
    public Document build() {
        Document doc = Documents.create();
        new Generator(doc).generate();
        return doc;
    }
    
    private String getIdentifier() {
        if (identifier != null) {
            return identifier;
        }
        UUID uuid = UUID.randomUUID();
        return "urn:uuid:" + uuid.toString();
    }
    
    private String getTitle() {
        if (title != null) {
            return title;
        }
        return "Untitled";
    }
    
    private String getLanguage() {
        if (language != null) {
            return language;
        }
        return "en_US";
    }
    
    private OffsetDateTime getLastModified() {
        if (lastModified != null) {
            return lastModified;
        }
        return OffsetDateTime.now();
    }
    
    private List<String> getAuthors() {
        return authors;
    }
    
    private Optional<String> getRights() {
        return Optional.ofNullable(rights);
    }
    
    private class Generator {
        
        private static final String NAMESPACE_URI = "http://www.idpf.org/2007/opf";
        private static final String DC_NAMESPACE_URI = "http://purl.org/dc/elements/1.1/";
        private static final String PUBLICAITON_IDENTIFIER = "pub-id";
        
        private final Document doc;
        
        public Generator(Document doc) {
            this.doc = doc;
        }
        
        public void generate() {
            this.doc.appendChild(createPackage());
        }
        
        private Element createPackage() {
            Element e = doc.createElementNS(NAMESPACE_URI, "package");
            e.setAttribute("version", "3.0");
            e.setAttribute("unique-identifier", PUBLICAITON_IDENTIFIER);
            e.appendChild(createMetadata());
            e.appendChild(createManifest());
            e.appendChild(createSpine());
            return e;
        }
        
        private Element createMetadata() {
            Element e = doc.createElementNS(NAMESPACE_URI, "metadata"); 
            e.setAttribute("xmlns:dc", DC_NAMESPACE_URI);
            
            Element identifierElement = doc.createElementNS(DC_NAMESPACE_URI, "dc:identifier");
            identifierElement.setAttribute("id", PUBLICAITON_IDENTIFIER);
            identifierElement.setTextContent(getIdentifier());
            e.appendChild(identifierElement);

            Element titleElement = doc.createElementNS(DC_NAMESPACE_URI, "dc:title");
            titleElement.setTextContent(getTitle());
            e.appendChild(titleElement);
            
            Element languageElement = doc.createElementNS(DC_NAMESPACE_URI, "dc:language");
            languageElement.setTextContent(getLanguage());
            e.appendChild(languageElement);
            
            OffsetDateTime lastModified = OffsetDateTime.ofInstant(getLastModified().toInstant(), ZoneOffset.UTC);
            Element modifiedElement = doc.createElementNS(NAMESPACE_URI, "meta");
            modifiedElement.setAttribute("property", "dcterms:modified");
            modifiedElement.setTextContent(lastModified.format(ISO8601_FORMATTER));
            e.appendChild(modifiedElement);
            
            for (String author: getAuthors()) {
                Element child = doc.createElementNS(DC_NAMESPACE_URI, "dc:creator");
                child.setTextContent(author);
                e.appendChild(child);
            }
            
            getRights().ifPresent(value->{
                Element child = doc.createElementNS(DC_NAMESPACE_URI, "dc:rights");
                child.setTextContent(value);
                e.appendChild(child);
            });
            
            return e;
        }
        
        private Element createManifest() {
            Element e = doc.createElementNS(NAMESPACE_URI, "manifest");
            int i = 1;
            for (Path page: pages) {
                Element item = doc.createElementNS(NAMESPACE_URI, "item");
                item.setAttribute("id", "p" + i++);
                item.setAttribute("href", href(page));
                item.setAttribute("media-type", "application/xhtml+xml");
                e.appendChild(item);
            }
            i = 1;
            for (Path resource: resources) {
                Element item = doc.createElementNS(NAMESPACE_URI, "item");
                item.setAttribute("id", "r" + i++);
                item.setAttribute("href", href(resource));
                item.setAttribute("media-type", guessMediaType(resource));
                e.appendChild(item);
            }
            return e;
        }
        
        private String href(Path path) {
            return path.toString().replaceAll("\\\\", "/");
        }
        
        private String guessMediaType(Path path) {
            String name = path.getFileName().toString();
            if (name.endsWith(".png")) {
                return "image/png";
            } else if (name.endsWith(".jpeg") || name.endsWith(".jpg")) {
                return "image/jpeg";
            } else if (name.endsWith(".gif")) {
                return "image/gif";
            }
            return null;
        }

        private Element createSpine() {
            Element e = doc.createElementNS(NAMESPACE_URI, "spine"); 
            for (int i = 1; i <= pages.size(); i++) {
                Element itemref = doc.createElementNS(NAMESPACE_URI, "itemref");
                itemref.setAttribute("idref", "p" + i);
                e.appendChild(itemref);
            }
            return e;
        }
    }
}

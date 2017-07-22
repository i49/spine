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
package com.github.i49.spine.crawlers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.github.i49.spine.common.HtmlDocument;
import com.github.i49.spine.common.HtmlDocumentWriter;
import com.github.i49.spine.common.DocumentWriter;
import com.github.i49.spine.common.Documents;
import com.github.i49.spine.common.HtmlSpec;
import com.github.i49.spine.common.PackageDocumentBuilder;
import com.github.i49.spine.common.PublicationWriter;
import com.github.i49.spine.common.XmlDocumentWriter;
import com.github.i49.spine.crawlers.CrawlerConfiguration.Metadata;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;

public abstract class AbstractCrawler implements Crawler {

    protected static final Logger log = Logger.getLogger(AbstractCrawler.class.getName());
    
    private String rootLocation;
    private String firatPage;
    private String lastPage;
    private String publicationName;
    private int maxPages;
    
    private Metadata metadata;
  
    private List<Path> pages;
    private Set<Path> resources;
    
    private WebEngine webEngine;
    private LayoutPolicy layoutPolicy;
    private DocumentWriter xmlWriter;
    private DocumentWriter htmlWriter;
    
    protected AbstractCrawler() {
    }
    
    @Override
    public void configure(CrawlerConfiguration conf) throws Exception {
        this.rootLocation = conf.getRootLocation();
        this.firatPage = conf.getFirstPage();
        this.lastPage = conf.getLastPage();
        this.publicationName = conf.getPublicationName();
        this.maxPages = conf.getMaxPages();
        this.metadata = conf.getMetadata();
 
        Path workingDirectory = Paths.get(".");
        this.layoutPolicy = new LayoutPolicy(workingDirectory, this.publicationName);
        this.xmlWriter = new XmlDocumentWriter();
        this.htmlWriter = new HtmlDocumentWriter();
        initializeDirectories(this.layoutPolicy);
     }

    @Override
    public void start(WebEngine webEngine) throws Exception {
        this.webEngine = webEngine;
        
        this.pages = new ArrayList<>();
        this.resources = new LinkedHashSet<>();
        
        JSObject window = (JSObject)webEngine.executeScript("window");
        window.setMember("crawler", this);

        webEngine.getLoadWorker().stateProperty().addListener(this::handleStateChange);
        webEngine.load(this.firatPage);
    }
    
    @Override
    public void finish() {
        Platform.runLater(()->{
            generatePublication();
            Platform.exit();
        });
    }
    
    public WebEngine getWebEngine() {
        return webEngine;
    }
    
    private void initializeDirectories(LayoutPolicy policy) throws IOException {
        Files.deleteIfExists(policy.getPublicationFile());
        Files.createDirectories(policy.getPublicationMetaDirectory());
        copyResource("mimetype", policy.getPublicationDirectory());
        copyResource("container.xml", policy.getPublicationMetaDirectory());
    }

    private void handleStateChange(ObservableValue<? extends State> value, State oldState, State newState) {
        log.info("State changed: " + newState.toString());
        if (newState == State.SUCCEEDED) {
            handleDocumentLoaded(webEngine.getDocument());
        }
    }
  
    protected abstract void handleDocumentLoaded(Document doc);
    
    protected void processContent(Document doc) {
        try {
            addPage(doc);
            if (hasMorePages()) {
                goToNextPage();
            } else {
                finish();
            }
        } catch (Exception e) {
            log.severe(e.getMessage());
            Platform.exit();
        }
    }

    private boolean hasMorePages() {
        if (pages.size() >= maxPages) {
            return false;
        }
        Document doc = getWebEngine().getDocument();
        String location = doc.getDocumentURI();
        return !location.startsWith(lastPage);
    }
    
    private void goToNextPage() {
        getWebEngine().executeScript(
                "$('button.next-topic-button').trigger('click');"
                );
    }
    
    protected void addPage(Document doc) {
        String location = doc.getDocumentURI();
        Path local = mapToLocal(location);
        if (local == null) {
            return;
        }
        try {
            writeContentDocument(doc, layoutPolicy.getOriginalDirectory().resolve(local));
            doc = convertDocument(doc);
            writeContentDocument(doc, layoutPolicy.getPublicationContentDirectory().resolve(local));
            writeAllImages(doc);
            this.pages.add(local);
        } catch (Exception e) {
            log.severe(e.getMessage());
        }
    }
    
    protected Document convertDocument(Document doc) throws Exception {
        Document copied = Documents.copy(doc);
        HtmlDocument.of(copied)
                .toLowerCase()
                .remove("script")
                .remove("meta")
                .remove("link")
                .unwrap("concept")
                .removeContainingClass("MCWebHelpFramesetLink")
                .removeContainingClass("MCBreadcrumbsBox_0")
                .removeDataAttributes()
                .addMetaCharset("utf-8")
                ;
        return copied;
    }
    
    private Path mapToLocal(String location) {
        if  (location.startsWith(this.rootLocation.toString())) {
            String local = location.substring(this.rootLocation.length());
            return Paths.get(local);
        } else {
            log.warning(Message.EXTERNAL_PAGE_WAS_SKIPPED.with(location));
            return null;
        }
    }
 
    private void writeContentDocument(Document doc, Path path) throws Exception {
        this.htmlWriter.writeDocumentAt(path, doc);
    }
 
    private void writeAllImages(Document doc) throws IOException {
        URI base = URI.create(doc.getDocumentURI());
        NodeList nodes = doc.getElementsByTagNameNS(HtmlSpec.NAMESPACE_URL, "img");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element image = (Element)nodes.item(i);
            URI location = base.resolve(image.getAttribute("src"));
            writeResource(location);
        }
    }
    
    private void writeResource(URI location) throws IOException {
        Path local = mapToLocal(location.toString());
        if (local == null) {
            return;
        }
        Path path = layoutPolicy.getPublicationContentDirectory().resolve(local);
        if (!Files.exists(path)) {
            downloadResource(location, path);
        }
        this.resources.add(local);
    }
  
    private void downloadResource(URI remote, Path local) throws IOException {
        log.info("downloading: " + remote.toString());
        Files.createDirectories(local.getParent());
        URLConnection conn = remote.toURL().openConnection();
        String userAgent = getWebEngine().getUserAgent();
        conn.setRequestProperty("User-Agent", userAgent);
        try (InputStream in = conn.getInputStream()) {
            Files.copy(in, local, StandardCopyOption.REPLACE_EXISTING);
        }
    }
    
    private void generatePublication() {
        log.info("Building the package document...");
        PackageDocumentBuilder builder = new PackageDocumentBuilder();
        builder.pages(this.pages).resoures(this.resources);
        buildPackage(builder);
        Document doc = builder.build();

        try {
            log.info("Writing the package document...");
            Path path = layoutPolicy.getPublicationContentDirectory().resolve("package.opf");
            this.xmlWriter.writeDocumentAt(path, doc);
            writePublication(this.publicationName + ".epub");
            log.info("Completed.");
        } catch (Exception e) {
            log.severe(e.getMessage());
        }
    }

    private void writePublication(String fileName) throws IOException {
        Path baseDir = layoutPolicy.getPublicationDirectory();
        Path target = layoutPolicy.getPublicationFile();
        log.info("Writing the publication file: " + target.toString());
        PublicationWriter writer = new PublicationWriter(baseDir);
        writer.writeTo(target);
    }
    
    private void copyResource(String name, Path targetDir) throws IOException {
        Files.createDirectories(targetDir);
        Path target = targetDir.resolve(name);
        try (InputStream in = getClass().getResourceAsStream(name)) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
    
    protected void buildPackage(PackageDocumentBuilder builder) {
        builder.title(metadata.getTitle())
               .language(metadata.getLanguage())
               .authors(metadata.getAuthors())
               .rights(metadata.getRights());
    }
}

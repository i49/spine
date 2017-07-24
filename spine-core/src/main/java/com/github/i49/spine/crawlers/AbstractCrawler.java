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

import com.github.i49.spine.common.HtmlDocumentWriter;
import com.github.i49.spine.common.DocumentConverter;
import com.github.i49.spine.common.DocumentWriter;
import com.github.i49.spine.common.Documents;
import com.github.i49.spine.common.HtmlSpec;
import com.github.i49.spine.common.PackageDocumentBuilder;
import com.github.i49.spine.common.PublicationWriter;
import com.github.i49.spine.common.XmlDocumentWriter;
import com.github.i49.spine.crawlers.CrawlerConfiguration.Converter;
import com.github.i49.spine.crawlers.CrawlerConfiguration.Metadata;
import com.github.i49.spine.message.Message;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;

/**
 * A skeletal implementation of {@link Crawler}.
 */
public abstract class AbstractCrawler implements Crawler {

    protected static final Logger log = Logger.getLogger(AbstractCrawler.class.getName());
    private static final String PACKAGE_DOCUMENT_NAME = "package.opf";
    private static final String INDEX_NAME = "index.html";
    
    private String rootLocation;
    private String firatPage;
    private String lastPage;
    private String publicationName;
    private int maxPages;
    
    private Pager pager; 
    private Metadata metadata;
    private List<DocumentConverter> converters;
  
    private List<Path> pages;
    private Set<Path> resources;
    
    private WebEngine webEngine;
    private LayoutPolicy layoutPolicy;
    private DocumentWriter xmlWriter;
    private DocumentWriter htmlWriter;
    
    private JSObject window;
    
    protected AbstractCrawler() {
        this.converters = new ArrayList<>();
    }
    
    @Override
    public void configure(CrawlerConfiguration conf) throws Exception {
        this.rootLocation = conf.getRootLocation();
        this.firatPage = conf.getFirstPage();
        this.lastPage = conf.getLastPage();
        this.publicationName = conf.getPublicationName();
        this.maxPages = conf.getMaxPages();
        this.metadata = conf.getMetadata();
        this.pager = createPager(conf.getPager());

        configuraConverters(conf.getConverters());
        
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
        
        this.window = (JSObject)webEngine.executeScript("window");
        this.window.setMember("crawler", this);

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
    
    @Override
    public void cancel() {
        Platform.exit();
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
        log.fine("State changed to: " + newState.toString());
        if (newState == State.SUCCEEDED) {
            handleDocumentLoaded(webEngine.getDocument());
        } else if (newState == State.FAILED) {
            log.severe(Message.DOCUMENT_LOADING_FAILED.with(webEngine.getLocation()));
            cancel();
        }
    }
  
    protected abstract void handleDocumentLoaded(Document doc);
    
    protected void processContent(Document doc) {
        try {
            addPage(doc);
            finishPage();
        } catch (Exception e) {
            log.severe(e.getMessage());
            Platform.exit();
        }
    }
    
    private void finishPage() {
        Document doc = getWebEngine().getDocument();
        if (!hasMorePages(doc) || !goToNextPage(doc)) {
            finish();
        }
    }

    private boolean hasMorePages(Document doc) {
        if (pages.size() >= maxPages) {
            return false;
        }
        String location = doc.getDocumentURI();
        return !location.startsWith(lastPage);
    }
    
    private boolean goToNextPage(Document doc) {
        if (this.pager == null) {
            return false;
        }
        return pager.goNext(doc);
    }
    
    protected void addPage(Document doc) {
        String location = doc.getDocumentURI();
        Path local = mapToLocalPath(location);
        if (local == null) {
            return;
        }
        try {
            writeContentDocument(doc, layoutPolicy.getOriginalDirectory().resolve(local));
            doc = convertDocument(doc);
            writeContentDocument(doc, layoutPolicy.getPublicationContentDirectory().resolve(local));
            log.info(Message.PAGE_WAS_SAVED.with(local));
            writeAllImages(doc);
            this.pages.add(local);
        } catch (Exception e) {
            log.severe(e.getMessage());
        }
    }
    
    protected Document convertDocument(Document doc) throws Exception {
        Document copied = Documents.copy(doc);
        for (DocumentConverter converter: this.converters) {
            converter.convert(copied);
        }
        return copied;
    }
    
    /**
     * Maps a remote location to a path on the local filesystem.
     * 
     * @param location the remote location.
     * @return the path on the local filesystem. can be {@code null}.
     */
    private Path mapToLocalPath(String location) {
        if  (location.startsWith(this.rootLocation.toString())) {
            String local = location.substring(this.rootLocation.length());
            if (local.isEmpty() || local.endsWith("/")) {
                local = local.concat(INDEX_NAME);
            }
            return Paths.get(local);
        } else {
            log.warning(Message.PAGE_WAS_SKIPPED.with(location));
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
        Path local = mapToLocalPath(location.toString());
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
        log.info(Message.DOWNLOADING_RESOURCE.with(remote.toString()));
        Files.createDirectories(local.getParent());
        URLConnection conn = remote.toURL().openConnection();
        String userAgent = getWebEngine().getUserAgent();
        conn.setRequestProperty("User-Agent", userAgent);
        try (InputStream in = conn.getInputStream()) {
            Files.copy(in, local, StandardCopyOption.REPLACE_EXISTING);
        }
    }
    
    private void generatePublication() {
        log.info(Message.GENERATING_PACKAGE_DOCUMENT.with(PACKAGE_DOCUMENT_NAME));
        PackageDocumentBuilder builder = new PackageDocumentBuilder();
        builder.pages(this.pages).resoures(this.resources);
        buildPackage(builder);
        Document doc = builder.build();

        try {
            Path path = layoutPolicy.getPublicationContentDirectory().resolve(PACKAGE_DOCUMENT_NAME);
            this.xmlWriter.writeDocumentAt(path, doc);
            writePublication(this.publicationName + ".epub");
            log.info(Message.COMPLETED.toString());
        } catch (Exception e) {
            log.severe(e.getMessage());
        }
    }

    protected void buildPackage(PackageDocumentBuilder builder) {
        builder.title(metadata.getTitle())
               .language(metadata.getLanguage())
               .authors(metadata.getAuthors())
               .rights(metadata.getRights());
    }

    private void writePublication(String fileName) throws IOException {
        Path baseDir = layoutPolicy.getPublicationDirectory();
        Path target = layoutPolicy.getPublicationFile();
        log.info(Message.GENERATING_PUBLICATION.with(target.toString()));
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
    
    /**
     * Configures the document converters.
     * 
     * @param configurations the configuration for the converters.
     */
    private void configuraConverters(List<Converter> configurations) {
        DocumentConverterFactory factory = DocumentConverterFactory.getInstance();
        for (Converter c: configurations) {
            DocumentConverter converter = factory.createConverter(c);
            if (converter != null) {
                this.converters.add(converter);
            }
        }
    }
    
    private static Pager createPager(CrawlerConfiguration.Pager conf) {
        switch (conf.getMethod()) {
        case CLICK:
            return ClickingPager.create(conf.getTarget());
        default:
            return null;
        }
    }
}

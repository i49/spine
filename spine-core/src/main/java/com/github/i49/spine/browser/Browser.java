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
package com.github.i49.spine.browser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import org.yaml.snakeyaml.Yaml;

import com.github.i49.spine.crawlers.Crawler;
import com.github.i49.spine.crawlers.CrawlerConfiguration;
import com.github.i49.spine.crawlers.CrawlerException;
import com.github.i49.spine.crawlers.CrawlerType;
import com.github.i49.spine.crawlers.FrameCrawler;
import com.github.i49.spine.crawlers.BasicCrawler;
import com.github.i49.spine.message.Message;

/**
 * Web browser application.
 */
public class Browser extends Application {

    private static final Logger log = Logger.getLogger(Browser.class.getName());
    private static final String DEFAULT_CONFIGURATION_NAME = "crawler.yaml";
    
    private boolean initialized;
    private Crawler crawler;
    
    @Override
    public void init() throws Exception {
        try {
            List<String> args = getParameters().getRaw();
            String confPath = (args.size() > 0) ? args.get(0) : DEFAULT_CONFIGURATION_NAME;
            
            CrawlerConfiguration configuration = loadCrawlerConfiguration(Paths.get(confPath));
            this.crawler = createCrawler(configuration.getType());
            if (this.crawler == null) {
                return;
            }
            this.crawler.configure(configuration);
            initialized = true;
        } catch (Exception e) {
            log.severe(e.getMessage());
            initialized = false;
        }
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        if (!initialized) {
            Platform.runLater(this::stop);
            return;
        }
        
        WebView webView = new WebView();
        crawler.start(webView.getEngine());
        
        primaryStage.setScene(new Scene(webView, 800, 800));
        primaryStage.show();
    }
    
    @Override
    public void stop() {
        try {
            super.stop();
        } catch (Exception e) {
        }
        Platform.exit();
    }
    
    private static CrawlerConfiguration loadCrawlerConfiguration(Path path) {
        Yaml yaml = new Yaml();
        try (InputStream input = Files.newInputStream(path)) {
            CrawlerConfiguration conf = yaml.loadAs(input, CrawlerConfiguration.class);
            conf.setPublicationName(getPublicationName(path));
            return conf;
        } catch (IOException e) {
            throw new CrawlerException(Message.CRAWLER_CONFIGURATION_NOT_FOUND.with(path), e);
        }
    }
    
    private static String getPublicationName(Path path) {
        String fileName = path.getFileName().toString();
        int lastIndex = fileName.lastIndexOf('.');
        if (lastIndex >= 0) {
            return fileName.substring(0, lastIndex);
        } else {
            return fileName;
        }
    }
    
    private static Crawler createCrawler(CrawlerType type) {
        switch (type) {
        case BASIC:
            return new BasicCrawler();
        case FRAME:
            return new FrameCrawler();
        default:
            log.severe(Message.UNSUPPORTED_CRAWLER_TYPE.with(type));
            return null;
        }
    }
    
    public static void main(String[] args) {
        try {
            configureLogger();
            launch(args);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    
    private static void configureLogger() throws IOException {
        final LogManager logManager = LogManager.getLogManager();
        try (InputStream input = Browser.class.getResourceAsStream("logging.properties")) {
            logManager.readConfiguration(input);
        }
    }
}


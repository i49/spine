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

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for crawlers.
 */
public class CrawlerConfiguration {
    
    private String type;
    private String firstPage;
    private String lastPage;
    private String rootLocation;
    private String publicationName;
    private int maxPages;
    private String contentFrame;
    private Metadata metadata;
   
    public CrawlerConfiguration() {
        this.type = "simple";
        this.maxPages = Integer.MAX_VALUE;
        this.metadata = new Metadata();
    }
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFirstPage() {
        return firstPage;
    }
 
    public void setFirstPage(String firstPage) {
        this.firstPage = firstPage;
    }
    
    public String getLastPage() {
        return lastPage;
    }
    
    public void setLastPage(String lastPage) {
        this.lastPage = lastPage;
    }
    
    public String getRootLocation() {
        return rootLocation;
    }
    
    public void setRootLocation(String rootLocation) {
        this.rootLocation = rootLocation;
    }
    
    public String getPublicationName() {
        return publicationName;
    }

    public void setPublicationName(String publicationName) {
        this.publicationName = publicationName;
    }

    public int getMaxPages() {
        return maxPages;
    }

    public void setMaxPages(int maxPages) {
        this.maxPages = maxPages;
    }
    
    public String getContentFrame() {
        return contentFrame;
    }

    public void setContentFrame(String frame) {
        this.contentFrame = frame;
    }

    public Metadata getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }
    
    public static class Metadata {
        
        private String title;
        private String language;
        private List<String> authors;
        private String rights;
        
        public Metadata() {
            this.title = "(untitled)";
            this.language = "en";
            this.authors = new ArrayList<>();
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public List<String> getAuthors() {
            return authors;
        }
        
        public void setAuthors(List<String> authors) {
            this.authors = authors;
        }

        public String getRights() {
            return rights;
        }

        public void setRights(String rights) {
            this.rights = rights;
        }
    }
}

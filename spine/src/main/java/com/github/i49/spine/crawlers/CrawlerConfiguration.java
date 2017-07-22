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
    
    public CrawlerConfiguration() {
        this.type = "basic";
        this.maxPages = Integer.MAX_VALUE;
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
}

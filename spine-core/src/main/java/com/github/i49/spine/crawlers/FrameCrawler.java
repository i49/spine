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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLIFrameElement;

import com.github.i49.spine.common.HtmlDocument;

/**
 * Crawler for parsing web pages those have contents within iframes.
 */
public class FrameCrawler extends AbstractCrawler {

    private String contentFrame;
    
    public FrameCrawler() {
        super();
    }
 
    @Override
    public void configure(CrawlerConfiguration conf) throws Exception {
        super.configure(conf);
        this.contentFrame = conf.getContentFrame();
    }

    @Override
    protected void handleDocumentLoaded(Document doc) {
        Element iframe = getContentFrame(doc);
        if (iframe != null) {
            ((EventTarget)iframe).addEventListener("load", this::handleContentLoaded, false);
        }
    }

    private void handleContentLoaded(Event event) {
        Document doc = getWebEngine().getDocument();
        Element iframe = getContentFrame(doc);
        if (iframe != null) {
            processContent(((HTMLIFrameElement)iframe).getContentDocument());
        }
    }
    
    private Element getContentFrame(Document doc) {
        HtmlDocument html = HtmlDocument.of(doc);
        if (this.contentFrame != null) {
            return html.find(this.contentFrame);
        }
        return null;
    }
}

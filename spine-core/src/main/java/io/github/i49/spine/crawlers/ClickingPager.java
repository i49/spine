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

package io.github.i49.spine.crawlers;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.UIEvent;
import org.w3c.dom.views.DocumentView;

import io.github.i49.spine.common.HtmlDocument;

/**
 *
 */
public class ClickingPager implements Pager {

    private static final String EVENT_NAME = "click";
    private final String eventTarget;
    
    private ClickingPager(String target) {
        this.eventTarget = target;
    }
    
    @Override
    public boolean goNext(Document doc) {
        HtmlDocument html = HtmlDocument.of(doc);
        Element element = html.find(this.eventTarget);
        if (element == null) {
            return false;
        }
        DocumentEvent docEvent = (DocumentEvent)doc;
        DocumentView docView = (DocumentView)doc;
        UIEvent event = (UIEvent)docEvent.createEvent("UIEvents");
        event.initUIEvent(EVENT_NAME, true, true, docView.getDefaultView(), 0);
        EventTarget target = (EventTarget)element;
        target.dispatchEvent(event);
        return true;
    }
    
    public static Pager create(String eventTarget) {
        return new ClickingPager(eventTarget);
    }
}

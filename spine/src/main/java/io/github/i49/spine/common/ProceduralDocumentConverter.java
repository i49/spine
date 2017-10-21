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

package io.github.i49.spine.common;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.w3c.dom.Document;

/**
 */
public class ProceduralDocumentConverter implements DocumentConverter {

    private final List<Consumer<HtmlDocument>> procedures;
    
    public static Builder builder() {
        return new Builder();
    }
    
    private ProceduralDocumentConverter(Builder builder) {
        this.procedures = builder.procedures;
    }
    
    @Override
    public Document convert(Document doc) {
        HtmlDocument html = HtmlDocument.of(doc);
        for (Consumer<HtmlDocument> procedure: this.procedures) {
            procedure.accept(html);
        }
        return doc;
    }
    
    public static class Builder {
        
        private final List<Consumer<HtmlDocument>> procedures;

        public Builder() {
            procedures = new ArrayList<>();
        }
        
        public Builder remove(String expression) {
            procedures.add(doc->{
                doc.remove(expression);
            });
            return this;
        }
        
        public Builder unwrap(String expression) {
            procedures.add(doc->{
                doc.unwrap(expression);
            });
            return this;
        }
        
        public Builder removeDataAttributes() {
            procedures.add(doc->{
                doc.removeDataAttributes();
            });
            return this;
        }

        public Builder addMetaCharset(String charset) {
            procedures.add(doc->{
                doc.addMetaCharset(charset);
            });
            return this;
        }

        public ProceduralDocumentConverter build() {
            return new ProceduralDocumentConverter(this);
        }
    }
}

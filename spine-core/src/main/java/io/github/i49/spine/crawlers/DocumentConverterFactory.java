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

import java.util.Map;

import io.github.i49.spine.common.DocumentConverter;
import io.github.i49.spine.common.ProceduralDocumentConverter;

/**
 *
 */
public class DocumentConverterFactory {

    private static final DocumentConverterFactory SINGLETON = new DocumentConverterFactory();

    public static DocumentConverterFactory getInstance() {
        return SINGLETON;
    }

    private DocumentConverterFactory() {
    }

    public DocumentConverter createConverter(CrawlerConfiguration.Converter conf) {
        switch (conf.getType()) {
        case PROCEDURAL:
            return createPreceduralConverter(conf);
        default:
            return null;
        }
    }

    private DocumentConverter createPreceduralConverter(CrawlerConfiguration.Converter conf) {
        ProceduralDocumentConverter.Builder builder =   ProceduralDocumentConverter.builder();
        for (Object command: conf.getCommands()) {
            if (command instanceof String) {
                if ("removeDataAttributes".equals(command)) {
                    builder.removeDataAttributes();
                }
            } else if (command instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, ?> map = (Map<String, ?>)command;
                if (map.containsKey("remove")) {
                    String expression = (String)map.get("remove");
                    builder.remove(expression);
                } else if (map.containsKey("unwrap")){
                    String expression = (String)map.get("unwrap");
                    builder.unwrap(expression);
                } else if (map.containsKey("addMetaCharset")){
                    String charset = (String)map.get("addMetaCharset");
                    builder.addMetaCharset(charset);
                }
            }
        }
        return builder.build();
    }
}

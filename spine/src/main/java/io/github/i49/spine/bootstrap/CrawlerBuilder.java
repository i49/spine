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

package io.github.i49.spine.bootstrap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import io.github.i49.spine.converters.Command;
import io.github.i49.spine.converters.CommandDocumentConverter;
import io.github.i49.spine.converters.DocumentConverter;
import io.github.i49.spine.crawlers.BasicCrawler;
import io.github.i49.spine.crawlers.Crawler;
import io.github.i49.spine.crawlers.CrawlerConfiguration;
import io.github.i49.spine.crawlers.CrawlerType;
import io.github.i49.spine.crawlers.FrameCrawler;
import io.github.i49.spine.crawlers.CrawlerConfiguration.Converter;
import io.github.i49.spine.message.Message;

/**
 * Builder of a web crawler.
 */
public class CrawlerBuilder {

    private static final Logger log = Logger.getLogger(CrawlerBuilder.class.getName());
    
    public Crawler build(CrawlerConfiguration conf) throws Exception {
        Crawler crawler = createCrawler(conf.getType());
        if (crawler == null) {
            return null;
        }
        crawler.configure(conf);
        addConverters(crawler, conf.getConverters());
        return crawler;
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
    
    private void addConverters(Crawler crawler, List<Converter> configurations) {
        for (Converter c: configurations) {
            DocumentConverter converter = createDocumentConverter(c);
            if (converter != null) {
                crawler.addConverter(converter);
            }
        }
    }
    
    private DocumentConverter createDocumentConverter(Converter conf) {
        switch (conf.getType()) {
        case PROCEDURAL:
            return createCommandConverter(conf);
        default:
            return null;
        }
    }
    
    private DocumentConverter createCommandConverter(Converter conf) {
        CommandDocumentConverter converter = new CommandDocumentConverter();
        for (Object command: conf.getCommands()) {
            converter.addCommand(createConverterCommand(command));
        }
        return converter;
    }
    
    @SuppressWarnings("unchecked")
    private Command createConverterCommand(Object conf) {
        String name = null;
        Map<String, Object> params = null;
        if (conf instanceof String) {
            name = (String)conf;
        } else if (conf instanceof Map) {
            Map<String, Object> map = (Map<String, Object>)conf;
            if (map.isEmpty()) {
                return null;
            }
            name = map.keySet().iterator().next();
            Object value = map.get(name);
            if (value instanceof Map) {
                params = (Map<String, Object>)value;
            } else if (value instanceof String) {
                params = new HashMap<String, Object>();
                params.put("value", value);
            } else {
                return null;
            }
        }
        return Command.create(name, params);
    }
}

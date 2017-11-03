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

package io.github.i49.spine.converters;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import io.github.i49.spine.common.HtmlDocument;

/**
 * @author i49
 *
 */
public class CommandDocumentConverter implements DocumentConverter {

    private final List<Command> commands;
    
    public CommandDocumentConverter() {
        this.commands = new ArrayList<>();
    }
    
    public void addCommand(Command command) {
        this.commands.add(command);
    }

    @Override
    public Document convert(Document doc) {
        HtmlDocument html = HtmlDocument.of(doc);
        this.commands.forEach(c->c.execute(html));
        return doc;
    }
}

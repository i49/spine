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

import java.util.HashMap;
import java.util.Map;

import io.github.i49.spine.common.HtmlDocument;

/**
 *
 */
public interface Command {
    
    void execute(HtmlDocument doc);

    static Command create(String name, Map<String, Object> params) {
        AbstractCommand command = null;
        switch (name) {
        case "addMetaCharset":
            command = new AddMetaCharset();
            break;
        case "remove":
            command = new Remove();
            break;
        case "removeDataAttributes":
            command = new RemoveDataAttributes();
            break;
        case "replace":
            command = new Replace();
            break;
        case "unwrap":
            command = new Remove();
            break;
        default:
            return null;
        }
        if (params != null) {
            command.addParams(params);
        }
        return command;
    }
}

abstract class AbstractCommand implements Command {
    
    private final Map<String, Object> params = new HashMap<>();
    
    public String getValue() {
        return getParamAsString("value");
    }
    
    public String getParamAsString(String name) {
        return (String)params.get(name);
    }
    
    public void addParams(Map<String, Object> params) {
        this.params.putAll(params);
    }
}

class AddMetaCharset extends AbstractCommand {
    
    @Override
    public void execute(HtmlDocument doc) {
        doc.addMetaCharset(getValue());
    }
}

class Remove extends AbstractCommand {

    @Override
    public void execute(HtmlDocument doc) {
        doc.remove(getValue());
    }
}

class RemoveDataAttributes extends AbstractCommand {
    
    @Override
    public void execute(HtmlDocument doc) {
        doc.removeDataAttributes();
    }
}

class Replace extends AbstractCommand {

    @Override
    public void execute(HtmlDocument doc) {
        doc.replace(getParamAsString("source"), getParamAsString("target"));
    }
}

class Unwrap extends AbstractCommand {

    @Override
    public void execute(HtmlDocument doc) {
        doc.unwrap(getValue());
    }
}

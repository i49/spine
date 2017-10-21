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
package io.github.i49.spine.message;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Localized messages.
 */
public enum Message {
    CRAWLER_CONFIGURATION_NOT_FOUND,
    UNSUPPORTED_CRAWLER_TYPE,
    DOCUMENT_LOADING_FAILED,
    PAGE_WAS_SAVED,
    PAGE_WAS_SKIPPED,
    DOWNLOADING_RESOURCE,
    GENERATING_PACKAGE_DOCUMENT,
    GENERATING_PUBLICATION,
    COMPLETED
    ;
  
    private static final String BASE_NAME = "io.github.i49.spine.message.messages";
    private static final ResourceBundle bundle = ResourceBundle.getBundle(BASE_NAME);
   
    @Override
    public String toString() {
        return getPattern();
    }

    public String with(Object... arguments) {
        return MessageFormat.format(getPattern(), arguments);
    }

    private String getPattern() {
        return bundle.getString(name());
    }
}

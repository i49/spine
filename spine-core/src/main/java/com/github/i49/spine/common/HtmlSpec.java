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
package com.github.i49.spine.common;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial") 
public class HtmlSpec {

    public static final String NAMESPACE_URL = "http://www.w3.org/1999/xhtml";
   
    private static final Set<String> voidElements;
    
    static {
        voidElements = new HashSet<String>() {{
            add("area");
            add("base");
            add("br");
            add("col");
            add("embed");
            add("hr");
            add("img");
            add("input");
            add("keygen");
            add("link");
            add("menuitem");
            add("meta");
            add("param");
            add("source");
            add("track");
            add("wbr");
        }};
    }
    
    public static boolean isVoid(String elementName) {
        return voidElements.contains(elementName);
    }
}

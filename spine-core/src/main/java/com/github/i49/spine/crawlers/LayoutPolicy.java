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

import java.nio.file.Path;

/**
 * The policy which determines directory layout.
 */
public class LayoutPolicy {

    private final Path basePath;
    private final String publicationName;
    
    public LayoutPolicy(Path basePath, String publicationName) {
        this.basePath = basePath;
        this.publicationName = publicationName;
    }
    
    public Path getBasePath() {
        return basePath;
    }
    
    public Path getOriginalDirectory() {
        return getPublicationDirectory().resolve(".original");
    }
    
    public Path getPublicationDirectory() {
        return basePath.resolve(this.publicationName);
    }
    
    public Path getPublicationContentDirectory() {
        return getPublicationDirectory().resolve("EPUB");
    }

    public Path getPublicationMetaDirectory() {
        return getPublicationDirectory().resolve("META-INF");
    }
    
    public Path getPublicationFile() {
        return getBasePath().resolve(publicationName + ".epub");
    }
}

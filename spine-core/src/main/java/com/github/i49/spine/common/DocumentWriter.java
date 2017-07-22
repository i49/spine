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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.w3c.dom.Document;

/**
 * XML document writer.
 */
public abstract class DocumentWriter {
    
    private String encoding;
    
    protected DocumentWriter() {
        this.encoding = "UTF-8";
    }
    
    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    
    /**
     * Writes the document at the specified path.
     * 
     * @param path the path where the document should be saved.
     * @param doc the document to be saved.
     * @throws Exception if an error occurred while writing the document.
     */
    public void writeDocumentAt(Path path, Document doc) throws Exception {
        try {
            Files.createDirectories(path.getParent());
            try (OutputStream stream = Files.newOutputStream(path)) {
                writeDocumentTo(stream, doc);
            }
        } catch (IOException e) {
            throw e;
        }
    }
    
    protected abstract void writeDocumentTo(OutputStream stream, Document doc) throws Exception;
}

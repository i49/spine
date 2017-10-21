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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A writer for EPUB file.
 */
public class PublicationWriter {
    
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    
    private final Path baseDir;
    private ZipOutputStream zstream;
    
    public PublicationWriter(Path baseDir) {
        this.baseDir = baseDir;
    }
    
    public void writeTo(Path target) {
        try (OutputStream stream = Files.newOutputStream(target)) {
            try (ZipOutputStream zstream = new ZipOutputStream(stream, CHARSET)) {
                this.zstream = zstream;
                writeRaw(baseDir.resolve("mimetype"));
                weiteAllDirectories(baseDir);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeRaw(Path path) throws IOException {
        ZipEntry entry = openEntry(path, ZipEntry.STORED);
        byte[] content = Files.readAllBytes(path);
        entry.setSize(content.length);
        entry.setCrc(computeCrc(content));
        zstream.putNextEntry(entry);
        zstream.write(content);
        zstream.closeEntry();
    }
    
    private void weiteAllDirectories(Path root) throws IOException {
        Files.list(root)
            .filter(PublicationWriter::isNormalDirectory)
            .forEach(this::writeAllEntries);
    }
    
    private static boolean isNormalDirectory(Path path) {
        return Files.isDirectory(path) &&
               !path.getFileName().toString().startsWith(".");
    }
    
    private void writeAllEntries(Path root) {
        try {
            Files.walk(root).filter(Files::isRegularFile).forEach(this::processEntry);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    private void processEntry(Path path) {
        try {
            writeEntry(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    private void writeEntry(Path path) throws IOException {
        ZipEntry entry = openEntry(path, ZipEntry.DEFLATED);
        zstream.putNextEntry(entry);
        try (InputStream in = Files.newInputStream(path)) {
            copyBytes(in, zstream);
        }
        zstream.closeEntry();
    }
    
    private ZipEntry openEntry(Path path, int method) throws IOException {
        Path relative = baseDir.relativize(path);
        String name = relative.toString().replaceAll("\\\\", "/");
        ZipEntry entry = new ZipEntry(name);
        entry.setMethod(method);
        return entry;
    }
    
    private static long computeCrc(byte[] content) {
        CRC32 crc = new CRC32();
        crc.update(content);
        return crc.getValue();
    }
    
    private static long copyBytes(InputStream in, OutputStream out) throws IOException {
        long total = 0;
        byte[] buffer = new byte[128 * 1024];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
            total += len;
        }
        return total;
    }
}

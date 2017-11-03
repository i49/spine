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

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

import javafx.application.Application;

/**
 *
 */
public class Launcher {

    public static void main(String[] args) {
        try {
            configureLogger();
            Application.launch(BrowserApplication.class, args);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    
    private static void configureLogger() throws IOException {
        final LogManager logManager = LogManager.getLogManager();
        try (InputStream input = Launcher.class.getResourceAsStream("logging.properties")) {
            logManager.readConfiguration(input);
        }
    }

}

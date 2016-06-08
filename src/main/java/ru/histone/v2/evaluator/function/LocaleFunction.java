/*
 * Copyright (c) 2016 MegaFon
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
package ru.histone.v2.evaluator.function;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Alexey Nevinsky
 */
public abstract class LocaleFunction extends AbstractFunction {

    protected ConcurrentMap<String, Properties> props;

    public LocaleFunction() {
        props = new ConcurrentHashMap<>();
        loadDefaultProperties();
        loadProperties();
    }

//    public LocaleFunction(Executor executor) {
//        super(executor);
//        loadProperties();
//    }

    private void loadProperties() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(LocaleFunction.class.getResource("/i18n/").toURI()))) {
            for (Path path : stream) {
                if (path.getFileName().toString().matches(".+[.]properties")) {
                    String fileName = path.getFileName().toString().split("\\.")[0];
                    Properties properties = new Properties();
                    properties.load(Files.newInputStream(path));
                    props.put(fileName, properties);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void loadDefaultProperties() {
        try (InputStream propertyListInputStream =
                     Thread.currentThread().getContextClassLoader().getResourceAsStream("i18n/props.conf")) {
            if (propertyListInputStream != null) {
                BufferedReader propertyListReader =
                        new BufferedReader(new InputStreamReader(propertyListInputStream));
                for (String fileName : propertyListReader.readLine().split(" ")) {
                    try (InputStream resourceInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("i18n/" + fileName)) {
                        Properties properties = new Properties();
                        properties.load(resourceInputStream);
                        props.put(fileName.split("\\.")[0], properties);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected Properties getCurrentProperties(Locale locale) {
        Properties properties = props.get(locale.getLanguage());
        if (properties == null) {
            properties = props.get("default");
        }
        return properties;
    }
}

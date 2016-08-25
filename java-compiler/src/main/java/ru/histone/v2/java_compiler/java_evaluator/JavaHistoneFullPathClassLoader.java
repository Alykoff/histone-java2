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

package ru.histone.v2.java_compiler.java_evaluator;

import ru.histone.v2.evaluator.resource.Resource;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Alexey Nevinsky
 */
public class JavaHistoneFullPathClassLoader extends JavaHistoneClassLoader {

    public static final String SCHEME = "fullPathClass";

    private JavaHistoneClassRegistry registry;

    public JavaHistoneFullPathClassLoader(URL basePath, JavaHistoneClassRegistry registry) throws MalformedURLException {
        super(basePath);
        this.registry = registry;
    }

    @Override
    public CompletableFuture<Resource> loadResource(URI url, Map<String, Object> params) {
        Path path = Paths.get(url).relativize(basePath);

        String className = registry.getClassName(path);

        return loadClass(className);
    }

    @Override
    public String getScheme() {
        return SCHEME;
    }
}

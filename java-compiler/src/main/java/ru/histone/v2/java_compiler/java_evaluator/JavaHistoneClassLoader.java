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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.histone.v2.evaluator.resource.Resource;
import ru.histone.v2.evaluator.resource.loader.Loader;

import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Alexey Nevinsky
 */
public class JavaHistoneClassLoader implements Loader {

    public static final String SCHEME = "class";
    private static final Logger LOG = LoggerFactory.getLogger(JavaHistoneClassLoader.class);

    protected final URLClassLoader classLoader;
    protected Path basePath;

    public JavaHistoneClassLoader(URL basePath) {
        this.basePath = Paths.get(URI.create(basePath.toString()));
        classLoader = new URLClassLoader(new URL[]{basePath}, JavaHistoneClassLoader.class.getClassLoader());
    }

    @Override
    public CompletableFuture<Resource> loadResource(URI url, Map<String, Object> params) {
        String className = url.toString().replace(SCHEME + "://", "");
        return loadClass(className);
    }

    protected CompletableFuture<Resource> loadClass(String className) {
        try {
            Class<?> t = classLoader.loadClass(className);

            JavaHistoneClassResource resource = new JavaHistoneClassResource(t, basePath.toString(), "class");
            return CompletableFuture.completedFuture(resource);
        } catch (Exception e) {
            LOG.error("Error on loading class", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getScheme() {
        return SCHEME;
    }
}

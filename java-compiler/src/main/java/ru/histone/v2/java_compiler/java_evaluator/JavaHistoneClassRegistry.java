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
import ru.histone.v2.java_compiler.bcompiler.StdLibrary;
import ru.histone.v2.java_compiler.bcompiler.data.Template;

import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Alexey Nevinsky
 */
public class JavaHistoneClassRegistry implements HistoneClassRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(JavaHistoneClassRegistry.class);

    private final ClassLoader classLoader;
    private final Path basePath;
    private final StdLibrary stdLibrary;

    public JavaHistoneClassRegistry(URL basePath, StdLibrary stdLibrary) {
        this.basePath = Paths.get(URI.create(basePath.toString()));
        classLoader = new URLClassLoader(new URL[]{basePath}, JavaHistoneClassRegistry.class.getClassLoader());
        this.stdLibrary = stdLibrary;
    }

    @Override
    public Template loadInstance(String className) {
        try {
            Class<?> t = classLoader.loadClass(className);

            Template tpl = (Template) t.newInstance();
            tpl.setStdLibrary(stdLibrary);
            return tpl;
        } catch (ClassNotFoundException e) {
            LOG.error("Error", e);
            return null;
        } catch (InstantiationException e) {
            LOG.error("Error", e);
            return null;
        } catch (IllegalAccessException e) {
            LOG.error("Error", e);
            return null;
        }
    }

    @Override
    public String getOriginBasePath() {
        return basePath.toString();
    }

    @Override
    public String getRealBasePath() {
        return basePath.toString();
    }
}

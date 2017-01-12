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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.histone.v2.evaluator.Converter;
import ru.histone.v2.java_compiler.bcompiler.StdLibrary;
import ru.histone.v2.java_compiler.bcompiler.Translator;
import ru.histone.v2.java_compiler.bcompiler.data.Template;
import ru.histone.v2.java_compiler.java_evaluator.support.HistoneTemplateCompiler;
import ru.histone.v2.java_compiler.java_evaluator.support.TemplateFileObject;
import ru.histone.v2.parser.Parser;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * @author Alexey Nevinsky
 */
public class JavaHistoneClassRegistry implements HistoneClassRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(JavaHistoneClassRegistry.class);

    protected final Path basePath;
    protected final StdLibrary stdLibrary;
    protected final Converter converter;
    protected HistoneTemplateCompiler compiler;

    protected final Map<String, RegistryObj> data = new ConcurrentHashMap<>();
    protected final URL[] baseURL;

    public JavaHistoneClassRegistry(URL basePath, StdLibrary stdLibrary, Parser parser, Translator histoneTranslator,
                                    Converter converter) {
        baseURL = new URL[]{basePath};
        this.basePath = Paths.get(URI.create(basePath.toString()));
        this.stdLibrary = stdLibrary;
        this.converter = converter;
        this.compiler = new HistoneTemplateCompiler(this, parser, histoneTranslator);
    }

    @Override
    public Template loadInstance(String className) {
        RegistryObj obj = getOrCreateLock(className);
        if (obj.instance != null) {
            return obj.instance;
        }

        obj.lock.writeLock().lock();
        try {
            URLClassLoader tmp = new URLClassLoader(baseURL, getClass().getClassLoader());
            Template t = loadTemplateByClassName(className, tmp);
            obj.instance = t;
            return t;
        } finally {
            obj.lock.writeLock().unlock();
        }
    }

    @Override
    public void add(String className, JavaFileObject javaFile) {
        RegistryObj obj = getOrCreateLock(className);
        obj.lock.writeLock().lock();
        try {
            obj.instance = null;
            obj.file = javaFile;
        } finally {
            obj.lock.writeLock().unlock();
        }
    }

    @Override
    public Collection<? extends JavaFileObject> files() {
        return Collections.unmodifiableCollection(data.values().stream()
                .filter(x -> x.file != null)
                .map(x -> x.file)
                .collect(Collectors.toList())
        );
    }

    @Override
    public void remove(final String className) {
        RegistryObj obj = data.get(className);
        data.remove(className, obj);
    }

    @Override
    public Template loadInstanceFromTpl(String className, String tpl) {
        RegistryObj obj = getOrCreateLock(className);
        obj.lock.writeLock().lock();
        try {
            String javaCode;
            try {
                javaCode = compiler.translate(className, tpl);
            } catch (IOException e) {
                LOG.error("Error translate tpl '" + className + "'", e);
                data.remove(className, obj);
                return null;
            }

            Map<String, String> classesObjects = Collections.singletonMap(className, javaCode);
            compiler.compile(classesObjects);

            JavaFileObject file = obj.file;
            if (file != null) {
                Template t = loadTemplateFromFile(file, className);
                obj.instance = t;
                if (t != null) {
                    return t;
                }
            }
            data.remove(className, obj);
            return null;
        } finally {
            obj.lock.writeLock().unlock();
        }
    }

    protected RegistryObj getOrCreateLock(String className) {
        return data.computeIfAbsent(className, k -> new RegistryObj());
    }

    /**
     * Method without locking. You must use lock from locks map then you will use this method
     *
     * @param file
     * @param className
     * @return instance of template
     */
    protected Template loadTemplateFromFile(JavaFileObject file, String className) {
        TemplateFileObject castedFile = (TemplateFileObject) file;
        URLClassLoader tmp = new URLClassLoader(baseURL, getClass().getClassLoader()) {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                if (className.equals(name)) {
                    byte[] bytes = castedFile.getByteCode();
                    return defineClass(className, bytes, 0, bytes.length);
                }
                return super.loadClass(name);
            }
        };

        return loadTemplateByClassName(className, tmp);
    }

    protected Template loadTemplateByClassName(String className, URLClassLoader tmp) {
        try {
            Template t = (Template) tmp.loadClass(className).newInstance();
            t.setStdLibrary(stdLibrary);
            t.setConverter(converter);
            return t;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            LOG.error(e.getMessage(), e);
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

    protected static class RegistryObj {
        public volatile ReadWriteLock lock;
        public volatile JavaFileObject file;
        public volatile Template instance;

        public RegistryObj() {
            lock = new ReentrantReadWriteLock();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            RegistryObj that = (RegistryObj) o;

            return new EqualsBuilder()
                    .append(lock, that.lock)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(lock)
                    .toHashCode();
        }
    }
}

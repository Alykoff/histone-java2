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
import ru.histone.v2.java_compiler.bcompiler.Compiler;
import ru.histone.v2.java_compiler.bcompiler.StdLibrary;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static ru.histone.v2.java_compiler.support.TemplateFileUtils.JAVA_EXTENSION;

/**
 * @author Alexey Nevinsky
 */
public class JavaHistoneClassRegistry implements HistoneClassRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(JavaHistoneClassRegistry.class);

    private final ClassLoader classLoader;
    private final Path basePath;
    private final StdLibrary stdLibrary;
    private final HistoneTemplateCompiler compiler;

    private final Map<String, ReadWriteLock> locks = new ConcurrentHashMap<>();
    private final Map<String, JavaFileObject> classes = new HashMap<>();
    private final Map<String, Template> instances = new HashMap<>();

    private final URL[] baseURL;

    public JavaHistoneClassRegistry(URL basePath, StdLibrary stdLibrary, Parser parser, Compiler histoneTranslator) {
        baseURL = new URL[]{basePath};
        this.basePath = Paths.get(URI.create(basePath.toString()));
        classLoader = new URLClassLoader(baseURL, JavaHistoneClassRegistry.class.getClassLoader());
        this.stdLibrary = stdLibrary;
        this.compiler = new HistoneTemplateCompiler(this, parser, histoneTranslator);
    }

    @Override
    public Template loadInstance(String className) {
        locks.putIfAbsent(className, new ReentrantReadWriteLock());
        ReadWriteLock lock = locks.get(className);

        lock.readLock().lock();
        try {
            Template t = instances.get(className);
            if (t != null) {
                return t;
            }
        } finally {
            lock.readLock().unlock();
        }

        lock.readLock().lock();
        try {
            URLClassLoader tmp = new URLClassLoader(baseURL, getClass().getClassLoader());
            return loadTemplateByClassName(className, tmp);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void add(String qualifiedClassName, JavaFileObject javaFile) {
        locks.putIfAbsent(qualifiedClassName, new ReentrantReadWriteLock());
        ReadWriteLock lock = locks.get(qualifiedClassName);
        lock.writeLock().lock();
        try {

            classes.put(qualifiedClassName, javaFile);
            instances.remove(qualifiedClassName);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Collection<? extends JavaFileObject> files() {
        return Collections.unmodifiableCollection(classes.values());
    }

    @Override
    public void remove(final String qualifiedClassName) {
        ReadWriteLock lock = locks.get(qualifiedClassName);
        if (lock == null) {
            return;
        }

        lock.writeLock().lock();
        try {

            classes.remove(qualifiedClassName);
            instances.remove(qualifiedClassName);
            locks.remove(qualifiedClassName);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Template loadInstanceFromTpl(String className, String tpl) {
        locks.putIfAbsent(className, new ReentrantReadWriteLock());
        ReadWriteLock lock = locks.get(className);

        Template t;

        lock.readLock().lock();
        try {
            t = instances.get(className);
            if (t != null) {
                return t;
            }
        } finally {
            lock.readLock().unlock();
        }


        lock.writeLock().lock();
        try {
            String javaCode;
            try {
                javaCode = compiler.translate(className, tpl);
            } catch (IOException e) {
                LOG.error("Error translate tpl '" + className + "'", e);
                locks.remove(className);
                return null;
            }

            Map<String, String> classesObjects = Collections.singletonMap(className, javaCode);
            compiler.compile(classesObjects);

            JavaFileObject file = classes.get(className);
            if (file != null) {
                t = loadTemplateFromFile(file, className);
                if (t != null) {
                    return t;
                }
            }
            locks.remove(className);
            return null;
        } finally {
            lock.writeLock().unlock();
        }
    }

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

    private Template loadTemplateByClassName(String className, URLClassLoader tmp) {
        try {
            Template t = (Template) tmp.loadClass(className).newInstance();
            t.setStdLibrary(stdLibrary);

            instances.put(className, t);

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
}

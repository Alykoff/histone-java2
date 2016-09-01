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

package ru.histone.v2.java_compiler.java_evaluator.support;

import ru.histone.v2.java_compiler.java_evaluator.HistoneClassRegistry;

import javax.tools.*;
import java.io.IOException;
import java.net.URI;
import java.util.*;

/**
 * @author Alexey Nevinsky
 */
public class HistoneFileManager extends ForwardingJavaFileManager<JavaFileManager> {

    private final HistoneClassRegistry registry;
    private Map<URI, JavaFileObject> fileObjects = new HashMap<>();

    public HistoneFileManager(JavaFileManager fileManager, HistoneClassRegistry registry) {
        super(fileManager);
        this.registry = registry;
    }

    public void putFileForInput(StandardLocation location, String packageName, String relativeName,
                                JavaFileObject file) {
        fileObjects.put(uri(location, packageName, relativeName), file);
    }

    private URI uri(Location location, String packageName, String relativeName) {
        return URI.create(location.getName() + '/' + packageName + '/' + relativeName);
    }

    @Override
    public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
        FileObject o = fileObjects.get(uri(location, packageName, relativeName));
        if (o != null) {
            return o;
        }
        return super.getFileForInput(location, packageName, relativeName);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String qualifiedName,
                                               JavaFileObject.Kind kind, FileObject outputFile) throws IOException {
        JavaFileObject file = new TemplateFileObject(qualifiedName, kind);
        registry.add(qualifiedName, file);
        return file;
    }

    @Override
    public String inferBinaryName(Location loc, JavaFileObject file) {
        // For our TemplateFileObject instances, return the file's name, else
        // simply run the default implementation
        if (file instanceof TemplateFileObject) {
            return file.getName();
        }
        return super.inferBinaryName(loc, file);
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds,
                                         boolean recurse) throws IOException {
        Iterable<JavaFileObject> result = super.list(location, packageName, kinds, recurse);
        List<JavaFileObject> files = new ArrayList<>();
        if (location == StandardLocation.CLASS_PATH && kinds.contains(JavaFileObject.Kind.CLASS)) {
            for (JavaFileObject file : fileObjects.values()) {
                if (file.getKind() == JavaFileObject.Kind.CLASS && file.getName().startsWith(packageName)) {
                    files.add(file);
                }
            }
            files.addAll(registry.files());
        } else if (location == StandardLocation.SOURCE_PATH && kinds.contains(JavaFileObject.Kind.SOURCE)) {
            for (JavaFileObject file : fileObjects.values()) {
                if (file.getKind() == JavaFileObject.Kind.SOURCE && file.getName().startsWith(packageName)) {
                    files.add(file);
                }
            }
        }
        for (JavaFileObject file : result) {
            files.add(file);
        }
        return files;
    }

    public void remove(StandardLocation location, String packageName, String relativeName) {
        fileObjects.remove(uri(location, packageName, relativeName));
    }
}

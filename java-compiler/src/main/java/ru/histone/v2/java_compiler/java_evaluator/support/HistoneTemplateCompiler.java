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

import com.squareup.javapoet.JavaFile;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.exceptions.ParserException;
import ru.histone.v2.java_compiler.bcompiler.Compiler;
import ru.histone.v2.java_compiler.java_evaluator.HistoneClassRegistry;
import ru.histone.v2.java_compiler.support.TemplateFileUtils;
import ru.histone.v2.parser.Parser;
import ru.histone.v2.parser.node.AstNode;
import ru.histone.v2.utils.AstJsonProcessor;

import javax.tools.*;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ru.histone.v2.java_compiler.java_evaluator.support.TemplateFileObject.JAVA_EXTENSION;

/**
 * @author Alexey Nevinsky
 */
public class HistoneTemplateCompiler {

    private static final Logger LOG = LoggerFactory.getLogger(HistoneTemplateCompiler.class);

    private final HistoneClassRegistry registry;
    private final HistoneFileManager fileManager;
    private final JavaCompiler compiler;
    private DiagnosticCollector<JavaFileObject> diagnostics;
    private final Parser parser;
    private final Compiler histoneTranslator;

    public HistoneTemplateCompiler(HistoneClassRegistry registry, Parser parser, Compiler histoneTranslator) {
        this.registry = registry;
        this.parser = parser;
        this.histoneTranslator = histoneTranslator;
        compiler = ToolProvider.getSystemJavaCompiler();
        diagnostics = new DiagnosticCollector<>();

        JavaFileManager javaFileManager = compiler.getStandardFileManager(diagnostics, null, null);
        fileManager = new HistoneFileManager(javaFileManager, registry);
    }

    public void compile(final Map<String, String> classes) {
        if (classes.isEmpty()) {
            return;
        }

        List<JavaFileObject> sources = new ArrayList<>();
        for (Map.Entry<String, String> entry : classes.entrySet()) {
            String qualifiedClassName = entry.getKey();
            CharSequence javaSource = entry.getValue();
            if (javaSource != null) {
                final int dotPos = qualifiedClassName.lastIndexOf('.');
                final String className = dotPos == -1 ? qualifiedClassName : qualifiedClassName.substring(dotPos + 1);
                final String packageName = dotPos == -1 ? "" : qualifiedClassName.substring(0, dotPos);
                final TemplateFileObject source = new TemplateFileObject(className, javaSource);
                sources.add(source);
                // Store the source file in the FileManager via package/class name.
                // For source files, we add a .java extension
                fileManager.putFileForInput(StandardLocation.SOURCE_PATH, packageName, className + JAVA_EXTENSION,
                        source);
            }
        }

        URL[] urls = ((URLClassLoader) (registry.getClass().getClassLoader())).getURLs();
        String classPath = "";
        for (URL classUrl : urls) {
            classPath += ":" + classUrl.toString();
        }

        //todo replace to common class Loader
        List<String> options = new ArrayList<>();
        options.add("-classpath");
        options.add(classPath);

        diagnostics = new DiagnosticCollector<>();
        // Get a CompilationTask from the compiler and compile the sources
        final JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, sources);
        final Boolean result = task.call();

        if (BooleanUtils.isFalse(result)) {
            StringBuilder cause = new StringBuilder("\n");
            for (Diagnostic d : diagnostics.getDiagnostics()) {
                cause.append(d).append(" ");
            }
            throw new HistoneTemplateCompilerException("Compilation failed. Causes: " + cause);
        }
    }

    public String translate(String baseURI, String inputStr) throws IOException {
        AstNode root;
        if (EvalUtils.isAst(inputStr)) {
            root = AstJsonProcessor.read(inputStr);
        } else {
            try {
                root = parser.process(inputStr, baseURI);
            } catch (ParserException e) {
                LOG.error(e.getMessage() + " in line " + e.getLine() + " in file '" + baseURI + "'", e);
                return null;
            }
        }
        String fileName = TemplateFileUtils.getSimpleClassName(baseURI);

        JavaFile file;
        try {
            file = histoneTranslator.createFile(TemplateFileUtils.getPackageName(baseURI), fileName, root);
        } catch (Throwable e) {
            LOG.error("Error on translate template '" + fileName + "'", e);
            return null;
        }

        StringBuilder sb = new StringBuilder();
        file.writeTo(sb);
        return sb.toString();
    }
}

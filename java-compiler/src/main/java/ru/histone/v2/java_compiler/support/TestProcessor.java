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

package ru.histone.v2.java_compiler.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.javapoet.JavaFile;
import org.apache.commons.lang3.StringUtils;
import ru.histone.v2.acceptance.HistoneTestCase;
import ru.histone.v2.exceptions.ParserException;
import ru.histone.v2.java_compiler.bcompiler.Translator;
import ru.histone.v2.parser.Parser;
import ru.histone.v2.parser.SsaOptimizer;
import ru.histone.v2.parser.node.AstNode;
import ru.histone.v2.utils.AstJsonProcessor;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.histone.v2.utils.ParserUtils.isAst;

/**
 * @author Alexey Nevinsky
 */
public class TestProcessor {

    private static final String TEST_GENERATED_CLASSES_LOCATION = "/generated-test-sources";
    private static final String TEST_JSON_GENERATED_CLASSES_LOCATION = TEST_GENERATED_CLASSES_LOCATION + "/ru/histone/v2/acceptance";
    private static final String TEST_CLASSES_LOCATION = "/test-classes";
    private static final String TEST_JSON_LOCATION = TEST_CLASSES_LOCATION + "/ru/histone/v2/acceptance";

    public void doCompile(String baseDir) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference type = new TypeReference<List<HistoneTestCase>>() {
        };
        Translator translator = new Translator();
        Parser parser = new Parser();

        Path jsonBaseDirPath = Paths.get(URI.create("file://" + baseDir + TEST_JSON_LOCATION));
        Path tplBaseDirPath = Paths.get(URI.create("file://" + baseDir + TEST_JSON_GENERATED_CLASSES_LOCATION));
        Path classesDirPath = Paths.get(URI.create("file://" + baseDir + TEST_GENERATED_CLASSES_LOCATION));
        Path testClassesDirPath = Paths.get(URI.create("file://" + baseDir + TEST_CLASSES_LOCATION));

        print("Start processing json-tests from '%s'", jsonBaseDirPath);

        processTestFiles(mapper, type, translator, parser, jsonBaseDirPath, classesDirPath, testClassesDirPath);

        processTpl(translator, parser, tplBaseDirPath, classesDirPath, classesDirPath);
    }

    protected void processTestFiles(ObjectMapper mapper, TypeReference type, Translator translator, Parser parser,
                                    Path jsonBaseDirPath, Path classesDirPath, Path testClassesDirPath)
            throws IOException {
        Files.walk(jsonBaseDirPath).forEach(jsonFilePath -> {
            //todo remove this fucking hardcode
            if (jsonFilePath.toString().endsWith("optimize.json")) {
                return;
            }

            AstNode root = null;
            try {
                if (!Files.isDirectory(jsonFilePath)) {
                    if (jsonFilePath.toString().endsWith(".json")) {
                        root = processTestFile(mapper, type, translator, parser, jsonBaseDirPath, classesDirPath,
                                               testClassesDirPath, jsonFilePath);
                    }
                }
            } catch (Exception e) {
                if (root != null) {
                    System.out.println("    Compiled template " + AstJsonProcessor.write(root));
                }
                throw new RuntimeException(e);
            }
        });
    }

    protected void processTpl(Translator translator, Parser parser, Path tplBaseDirPath, Path classesDirPath, Path testClassesDirPath) throws IOException {

        TestProcessorClassRegistry registry = new TestProcessorClassRegistry(testClassesDirPath);

        Files.walk(tplBaseDirPath).forEach(jsonFilePath -> {
            //todo remove this fucking hardcode
            if (jsonFilePath.toString().endsWith("optimize.json")) {
                return;
            }

            AstNode root = null;
            try {
                if (!Files.isDirectory(jsonFilePath)) {
                    if (jsonFilePath.toString().endsWith("tpl")) {
                        print("Compiling template '%s'...", getPathFromBaseDir(tplBaseDirPath, jsonFilePath));
                        root = createAstFromTpl(jsonFilePath, parser);

                        registry.processAst(jsonFilePath, root);

                        writeTplClass(translator, tplBaseDirPath, classesDirPath, jsonFilePath, root);
                    }
                }
            } catch (Exception e) {
                if (root != null) {
                    System.out.println("    Compiled template " + AstJsonProcessor.write(root));
                    System.out.println("        Some error occured: " + e.getMessage());
                }
            }
        });
    }

    private AstNode createAstFromTpl(Path jsonFilePath, Parser parser) throws IOException {
        AstNode root = null;
        String str = Files.lines(jsonFilePath).collect(Collectors.joining("\n"));
        if (isAst(str)) {
            root = AstJsonProcessor.read(str);
            SsaOptimizer optimizer = new SsaOptimizer();
            optimizer.process(root);
        } else {
            try {
                root = parser.process(str, jsonFilePath.toString());
            } catch (ParserException ignore) {
                //ignore
            }
        }
        return root;
    }

    private void writeTplClass(Translator translator, Path tplBaseDirPath, Path classesDirPath, Path jsonFilePath,
                               AstNode root) throws IOException {
        if (root != null) {
            String packageName = createPackage(tplBaseDirPath, jsonFilePath.getParent());
            String className = compileTemplates(
                    translator, packageName, classesDirPath, root, jsonFilePath.getFileName().toString(), -1
            );
            String classDef = "class:" + className;
            Files.write(jsonFilePath, classDef.getBytes());
        } else {
            Files.write(jsonFilePath, "".getBytes());
        }
    }

    private AstNode processTestFile(ObjectMapper mapper, TypeReference type, Translator translator, Parser parser,
                                    Path jsonBaseDirPath, Path classesDirPath, Path testClassesPath, Path jsonFilePath)
            throws IOException {
        print("Processing file '%s'...", getPathFromBaseDir(jsonBaseDirPath, jsonFilePath));

        Stream<String> stringStream = Files.lines(jsonFilePath);

        TestProcessorClassRegistry registry = new TestProcessorClassRegistry(testClassesPath);

        List<HistoneTestCase> histoneCases = mapper.readValue(stringStream.collect(Collectors.joining()), type);
        AstNode root = null;
        for (HistoneTestCase cases : histoneCases) {
            print("  Start processTemplate test '%s'", cases.getName());

            List<HistoneTestCase.Case> casesToRemove = new ArrayList<>();

            int i = 0;
            for (HistoneTestCase.Case testCase : cases.getCases()) {
                try {
                    if (StringUtils.isNotBlank(testCase.getInput())) {
                        System.out.println("    Compiling template " + testCase.getInput());
                        root = parser.process(testCase.getInput(), jsonFilePath.toString());
                    } else if (StringUtils.isNotBlank(testCase.getInputAST())) {
                        System.out.println("    Compiling template " + testCase.getInputAST());
                        root = AstJsonProcessor.read(testCase.getInputAST());
                    } else {
                        System.out.println("    Compiling template from file " + testCase.getInputFile());
                        String fileName = StringUtils.substring(jsonFilePath.getFileName().toString(), 0, -5);
                        String tplFilePath = jsonFilePath.getParent().toString() + "/tpl/" + fileName + "/" + testCase.getInputFile();
                        Stream<String> tplFile = Files.lines(Paths.get(tplFilePath));
                        root = parser.process(tplFile.collect(Collectors.joining()), jsonFilePath.toString());
                    }

                    registry.processAst(jsonFilePath, root);

                    String packageName = createPackage(jsonBaseDirPath, jsonFilePath.getParent());
                    String className = compileTemplates(translator, packageName, classesDirPath, root, cases.getName(), i);
                    testCase.setInputClass(className);
                } catch (ParserException ignore) {
                    casesToRemove.add(testCase);
                }

                i++;
            }

            cases.getCases().removeAll(casesToRemove);
        }

        String resFile = mapper.writeValueAsString(histoneCases);
        Files.write(jsonFilePath, resFile.getBytes());
        return root;
    }

    private String getPathFromBaseDir(Path basePath, Path path) {
        final String pathStr = path.toString();
        final String basePathStr = basePath.toString();
        return pathStr.substring(basePathStr.length(), pathStr.length());
    }

    private String createPackage(Path basePath, Path path) {
        String res = getPathFromBaseDir(basePath, path);
        res = "ru/histone/v2/acceptance" + res;
        res = res.replaceAll("/", ".");
        return res;
    }

    private String compileTemplates(Translator translator, String packageName, Path path, AstNode root, String testName, int caseNumber)
            throws IOException {
        String number = caseNumber == -1 ? "" : caseNumber + "";
        String name = "Template" + testName.replaceAll("[\\s*\\-\\.\\(\\)\\:\\,\\'\\+\\&\\>\\{\\}\\#\\[\\]]", "") + number;

        JavaFile javaFile = translator.createFile(packageName, name, root);
        javaFile.writeTo(path);

        return packageName + "." + name;
    }

    private void print(String message, Object... args) {
        System.out.println(String.format(message, args));
    }
}

//<noscript id="res_test.js"></noscript><noscript id="res_resource1.css"></noscript><noscript id="res_resource2.css"></noscript>["test.js","resource11.js"][]<noscript id="res_test.css"></noscript><noscript id="res_ololo.js"></noscript>["resource1.css","resource11.css","resource2.css","test.css"]["ololo.js"]>
//<noscript id="res_test.js"></noscript><noscript id="res_resource1.css"></noscript><noscript id="res_resource2.css"></noscript>["test.js"][]<noscript id="res_test.css"></noscript><noscript id="res_ololo.js"></noscript>["resource1.css","resource2.css","test.css"]["ololo.js"]
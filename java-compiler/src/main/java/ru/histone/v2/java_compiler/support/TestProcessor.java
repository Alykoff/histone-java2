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
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import ru.histone.v2.acceptance.HistoneTestCase;
import ru.histone.v2.exceptions.ParserException;
import ru.histone.v2.java_compiler.bcompiler.Compiler;
import ru.histone.v2.parser.Parser;
import ru.histone.v2.parser.node.AstNode;
import ru.histone.v2.utils.AstJsonProcessor;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Alexey Nevinsky
 */
public class TestProcessor {

    private static final String TEST_GENERATED_CLASSES_LOCATION = "/generated-test-sources";
    private static final String TEST_CLASSES_LOCATION = "/test-classes";
    private static final String TEST_JSON_LOCATION = TEST_CLASSES_LOCATION + "/ru/histone/v2/acceptance";

    public void doCompile(String baseDir) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference type = new TypeReference<List<HistoneTestCase>>() {
        };
        Compiler compiler = new Compiler();
        Parser parser = new Parser();

        Path jsonBaseDirPath = Paths.get(URI.create("file://" + baseDir + TEST_JSON_LOCATION));
        Path classesDirPath = Paths.get(URI.create("file://" + baseDir + TEST_GENERATED_CLASSES_LOCATION));

        print("Start processing json-tests from '%s'", jsonBaseDirPath);

        Files.walk(jsonBaseDirPath).forEach(jsonFilePath -> {
            try {
                if (!Files.isDirectory(jsonFilePath) && jsonFilePath.endsWith("arithmetic.json")) {
                    print("Processing file '%s'...", getPathFromBaseDir(jsonBaseDirPath, jsonFilePath));

                    Stream<String> stringStream = Files.lines(jsonFilePath);

                    List<HistoneTestCase> histoneCases = mapper.readValue(stringStream.collect(Collectors.joining()), type);
                    for (HistoneTestCase cases : histoneCases) {
                        print("  Start process test '%s'", cases.getName());

                        int i = 0;
                        for (HistoneTestCase.Case testCase : cases.getCases()) {
                            System.out.println("    Compile template " + testCase.getInput());

                            AstNode root;
                            if (StringUtils.isNotBlank(testCase.getInput())) {
                                root = parser.process(testCase.getInput(), jsonFilePath.toString());
                            } else if (StringUtils.isNotBlank(testCase.getInputAST())) {
                                root = AstJsonProcessor.read(testCase.getInputAST());
                            } else {
                                throw new NotImplementedException("Implement me!");
                                //todo load ast from file
                            }

                            String packageName = createPackage(jsonBaseDirPath, jsonFilePath.getParent());
                            String className = compileTemplates(compiler, packageName, classesDirPath, root, cases.getName(), i);
                            testCase.setInputClass(className);

                            i++;
                        }
                    }

                    String resFile = mapper.writeValueAsString(histoneCases);
                    Files.write(jsonFilePath, resFile.getBytes());
                }
            } catch (ParserException ignore) {
                //it is parser exception, so we ignore it
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
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

    private String compileTemplates(Compiler compiler, String packageName, Path path, AstNode root, String testName, int caseNumber) throws IOException {
        String name = "Template" + testName.replaceAll("[\\s*\\-\\.\\(\\)\\:\\,\\'\\+\\&\\>\\{\\}\\#\\[\\]]", "") + caseNumber;

        JavaFile javaFile = compiler.createFile(packageName, name, root);
        javaFile.writeTo(path);

        return packageName + "." + name;
    }

    private void print(String message, Object... args) {
        System.out.println(String.format(message, args));
    }
}
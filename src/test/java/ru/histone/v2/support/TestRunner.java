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

package ru.histone.v2.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.ObjectUtils;
import org.junit.Assert;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.Evaluator;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.exceptions.HistoneException;
import ru.histone.v2.exceptions.ParserException;
import ru.histone.v2.parser.Parser;
import ru.histone.v2.parser.node.ExpAstNode;
import ru.histone.v2.rtti.RunTimeTypeInfo;
import ru.histone.v2.utils.ParserUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Alexey Nevinsky
 */
public class TestRunner {
    public static List<HistoneTestCase> loadTestCases(String testPath) throws URISyntaxException, IOException {
        DirectoryStream<Path> stream = Files.newDirectoryStream(
                Paths.get(TestRunner.class.getResource("/acceptance/" + testPath).toURI())
        );

        ObjectMapper mapper = new ObjectMapper();
        TypeReference type = new TypeReference<List<HistoneTestCase>>() {
        };

        List<HistoneTestCase> cases = new ArrayList<>();
        Map<String, Path> tplMap = new HashMap<>();
        for (Path path : stream) {
            List<Path> paths = getFiles(path);
            for (Path p : paths) {
                if (p.toString().endsWith(".json")) {
                    Stream<String> stringStream = Files.lines(p);
                    List<HistoneTestCase> histoneCases = mapper.readValue(stringStream.collect(Collectors.joining()), type);
                    histoneCases.forEach(histoneTestCase -> histoneTestCase.getCases().forEach(c -> c.setBaseURI("file://" + p.toString())));
                    cases.addAll(histoneCases);
                } else {
                    tplMap.put(p.getFileName().toString(), p);
                }

            }
        }
        for (HistoneTestCase test : cases) {
            for (HistoneTestCase.Case testCase : test.getCases()) {
                if (testCase.getInputFile() != null) {
                    testCase.setInput(Files.lines(tplMap.get(testCase.getInputFile())).collect(Collectors.joining()));
                }
            }
        }

        return cases;
    }

    private static List<Path> getFiles(Path path) throws IOException {
        List<Path> files = new ArrayList<>();
        if (Files.isDirectory(path)) {
            for (Path p : Files.newDirectoryStream(path)) {
                files.addAll(getFiles(p));
            }
        } else {
            files.add(path);
        }
        return files;
    }


    public static void doTest(String input, RunTimeTypeInfo rtti, HistoneTestCase.Case testCase,
                              Evaluator evaluator, Parser parser) throws HistoneException {

        try {
            ExpAstNode root = parser.process(input, "");
            if (testCase.getExpectedAST() != null) {
                Assert.assertEquals(testCase.getExpectedAST(), ParserUtils.astToString(root));
            }
            if (testCase.getExpectedResult() != null) {
                Context context = Context.createRoot(testCase.getBaseURI(), rtti);
                if (testCase.getContext() != null) {
                    for (Map.Entry<String, CompletableFuture<EvalNode>> entry : convertContext(testCase).entrySet()) {
                        if (entry.getKey().equals("this")) {
                            context.getThisVars().put("this", entry.getValue());
                        } else {
                            context.getVars().put(entry.getKey(), entry.getValue());
                        }
                    }
                }
                String result = evaluator.process(root, context);
                Assert.assertEquals(testCase.getExpectedResult(), result);
            }
        } catch (ParserException ex) {
            if (testCase.getExpectedException() != null) {
                HistoneTestCase.ExpectedException e = testCase.getExpectedException();
                Assert.assertEquals(e.getLine(), ex.getLine());
                if (e.getMessage() != null) {
                    Assert.assertEquals(e.getMessage(), ex.getMessage());
                } else {
                    Assert.assertEquals("unexpected '" + e.getFound() + "', expected '" + e.getExpected() + "'", ex.getMessage());
                }
            } else {
                throw new RuntimeException(ex);
            }
        } catch (Exception ex) {
            if (testCase.getExpectedException() != null) {
                HistoneTestCase.ExpectedException e = testCase.getExpectedException();
                if (e.getMessage() != null) {
                    Assert.assertEquals(e.getMessage(), ex.getMessage());
                } else {
                    Assert.assertEquals("unexpected '" + e.getFound() + "', expected '" + e.getExpected() + "'", ex.getMessage());
                }
            } else {
                throw new RuntimeException(ex);
            }
        }
    }


    public static Map<String, CompletableFuture<EvalNode>> convertContext(HistoneTestCase.Case testCase) {
        Map<String, CompletableFuture<EvalNode>> res = new HashMap<>();
        for (Map.Entry<String, Object> entry : testCase.getContext().entrySet()) {
            if (entry.getValue() == null) {
                res.putIfAbsent(entry.getKey(), EvalUtils.getValue(ObjectUtils.NULL));
            } else if (entry.getValue() instanceof List) {
                List list = (List) entry.getValue();
                Map<String, Object> map = new LinkedHashMap<>(list.size());
                for (int i = 0; i < list.size(); i++) {
                    map.put(i + "", getObjectValue(list.get(i)));
                }
                res.putIfAbsent(entry.getKey(), EvalUtils.getValue(map));
            } else if (entry.getValue() instanceof Map) {
                Map<String, Object> m = (Map<String, Object>) entry.getValue();
                Map<String, Object> map = new LinkedHashMap<>(m.size());
                for (Map.Entry<String, Object> e : m.entrySet()) {
                    map.put(e.getKey(), getObjectValue(e.getValue()));
                }
                res.putIfAbsent(entry.getKey(), EvalUtils.getValue(map));
            } else {
                res.putIfAbsent(entry.getKey(), EvalUtils.getValue(entry.getValue()));
            }
        }
        return res;
    }

    public static boolean isDouble(Object value) {
        return value instanceof Double;
    }

    public static EvalNode getObjectValue(Object rawValue) {
        Object value = rawValue;
        if (isDouble(value)) {
            Double v = (Double) value;
            if (EvalUtils.canBeLong(v)) {
                value = v.longValue();
            }
        } else if (value instanceof Integer) {
            value = ((Integer) value).longValue();
        }
        return EvalUtils.createEvalNode(value);
    }
}

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

package ru.histone.v2;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import ru.histone.v2.acceptance.ExpectedException;
import ru.histone.v2.acceptance.HistoneTestCase;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.Evaluator;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.exceptions.HistoneException;
import ru.histone.v2.exceptions.ParserException;
import ru.histone.v2.java_compiler.bcompiler.data.Template;
import ru.histone.v2.parser.Parser;
import ru.histone.v2.property.DefaultPropertyHolder;
import ru.histone.v2.rtti.RunTimeTypeInfo;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
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
    private static final Locale US_LOCALE = Locale.US;

    public static List<HistoneTestCase> loadTestCases(String testPath) throws URISyntaxException, IOException {
        DirectoryStream<Path> stream = Files.newDirectoryStream(
                Paths.get(URI.create("file://" + TestRunner.class.getResource("/").getFile() + "ru/histone/v2/acceptance/" + testPath))
        );

        ObjectMapper mapper = new ObjectMapper();
        TypeReference type = new TypeReference<List<HistoneTestCase>>() {
        };

        List<HistoneTestCase> cases = new ArrayList<>();
        Map<String, Path> tplMap = new HashMap<>();
        for (Path path : stream) {
            List<Path> paths = getFiles(path);
            for (Path p : paths) {
                if (p.toString().endsWith("arithmetic.json")) {
                    Stream<String> stringStream = Files.lines(p);
                    List<HistoneTestCase> histoneCases = mapper.readValue(stringStream.collect(Collectors.joining()), type);
                    histoneCases.forEach(histoneTestCase -> histoneTestCase.getCases().forEach(c -> c.setBaseURI(p.toUri().toString())));
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
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"));
        try {
            URL[] urls = new URL[]{new URL("file:///")};
            URLClassLoader loader = new URLClassLoader(urls, TestRunner.class.getClassLoader());
            Class<?> t = loader.loadClass(testCase.getInputClass());

            Template template = (Template) t.newInstance();

            if (StringUtils.isNotBlank(testCase.getExpectedAST())) {
                org.testng.Assert.assertEquals(template.getStringAst(), testCase.getExpectedAST());
            }

            Context context = Context.createRoot("", US_LOCALE, rtti, new DefaultPropertyHolder());

            String result = (String) template.render(context).join().getValue();

            org.testng.Assert.assertEquals(result, testCase.getExpectedResult());
//
//            ExpAstNode root = parser.process(input, "");
//            String stringAst = AstJsonProcessor.write(root);
//            if (testCase.getExpectedAST() != null) {
//                Assert.assertEquals(testCase.getExpectedAST(), stringAst);
//            }
//
//            root = AstJsonProcessor.read(stringAst);
//            if (testCase.getExpectedResult() != null) {
//                Context context = Context.createRoot(testCase.getBaseURI(), US_LOCALE, rtti,
//                        new DefaultPropertyHolder());
//                if (testCase.getContext() != null) {
//                    for (Map.Entry<String, CompletableFuture<EvalNode>> entry : convertContext(testCase).entrySet()) {
//                        if (entry.getKey().equals("this")) {
//                            context.put("this", entry.getValue());
//                        } else {
//                            context.getVars().put(entry.getKey(), entry.getValue());
//                        }
//                    }
//                }
//                String result = evaluator.process(root, context);
//                Assert.assertEquals(normalizeLineEndings(testCase.getExpectedResult()), normalizeLineEndings(result));
//            } else if (testCase.getExpectedException() != null) {
//                Context context = Context.createRoot(testCase.getBaseURI(), US_LOCALE, rtti,
//                        new DefaultPropertyHolder());
//                evaluator.process(root, context);
//            }
        } catch (Exception ex) {
            checkException(testCase, ex);
        }
    }

    private static void checkException(HistoneTestCase.Case testCase, Exception ex) {
        if (testCase.getExpectedException() != null) {
            ExpectedException e = testCase.getExpectedException();
            if (ex instanceof ParserException) {
                Assert.assertEquals(e.getLine(), ((ParserException) ex).getLine());
            }
            if (e.getMessage() != null) {
                Assert.assertEquals(e.getMessage(), ex.getMessage());
            } else {
                Assert.assertEquals("unexpected '" + e.getFound() + "', expected '" + e.getExpected() + "'", ex.getMessage());
            }
        } else {
            throw new RuntimeException(ex);
        }
    }

    public static Map<String, CompletableFuture<EvalNode>> convertContext(HistoneTestCase.Case testCase) {
        Map<String, CompletableFuture<EvalNode>> res = new HashMap<>();
        for (Map.Entry<String, Object> entry : testCase.getContext().entrySet()) {
            final CompletableFuture<EvalNode> v;
            if (entry.getValue() == null) {
                v = EvalUtils.getValue(ObjectUtils.NULL);
            } else if (entry.getValue() instanceof List) {
                List list = (List) entry.getValue();
                Map<String, Object> map = new LinkedHashMap<>(list.size());
                for (int i = 0; i < list.size(); i++) {
                    map.put(i + "", getObjectValue(list.get(i)));
                }
                v = EvalUtils.getValue(map);
            } else if (entry.getValue() instanceof Map) {
                Map<String, Object> m = (Map<String, Object>) entry.getValue();
                Map<String, Object> map = new LinkedHashMap<>(m.size());
                for (Map.Entry<String, Object> e : m.entrySet()) {
                    map.put(e.getKey(), getObjectValue(e.getValue()));
                }
                v = EvalUtils.getValue(map);

            } else {
                v = EvalUtils.getValue(entry.getValue());
            }
            res.putIfAbsent(entry.getKey(), v);
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

    private static String normalizeLineEndings(String value) {
        return value.replaceAll("\\r\\n", "\n");
    }
}

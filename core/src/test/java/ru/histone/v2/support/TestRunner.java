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
import org.junit.Assert;
import org.junit.jupiter.api.DynamicTest;
import ru.histone.v2.acceptance.ExpectedException;
import ru.histone.v2.acceptance.HistoneTestCase;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * todo: move to test module
 *
 * @author Alexey Nevinsky
 */
public class TestRunner {
    public static final Locale US_LOCALE = Locale.US;

    private List<HistoneTestCase> loadTestCases(String testPath) throws URISyntaxException, IOException {
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
                    histoneCases
                            .forEach(histoneTestCase -> histoneTestCase.getCases()
                                    .forEach(c -> c.setBaseURI(p.toUri().toString()))
                            );
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

    public Stream<DynamicTest> loadCases(String param, Consumer<HistoneTestCase.Case> testConsumer) throws IOException, URISyntaxException {
        final List<DynamicTest> result = new ArrayList<>();
        final List<HistoneTestCase> histoneTestCases = loadTestCases(param);
        for (HistoneTestCase histoneTestCase : histoneTestCases) {
            for (HistoneTestCase.Case testCase : histoneTestCase.getCases()) {
                DynamicTest test =
                        DynamicTest.dynamicTest("Expression: " + testCase.getInput(), () -> testConsumer.accept(testCase));
                result.add(test);
            }
        }
        return result.stream();
    }

    public void checkException(Exception e, ExpectedException expectedException) {
        if (expectedException.getMessage() != null) {
            Assert.assertEquals(expectedException.getMessage(), e.getMessage());
        } else {
            Assert.assertEquals("unexpected '" + expectedException.getFound() + "', expected '" + expectedException.getExpected() + "'", e.getMessage());
        }
    }

    private String normalizeLineEndings(String value) {
        return value.replaceAll("\\r\\n", "\n");
    }

    public void assertEquals(String expected, String actual) {
        Assert.assertEquals(normalizeLineEndings(expected), normalizeLineEndings(actual));
    }
}

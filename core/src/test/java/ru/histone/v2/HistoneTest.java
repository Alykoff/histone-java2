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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import ru.histone.v2.evaluator.Evaluator;
import ru.histone.v2.evaluator.function.StopExecutionExceptionFunction;
import ru.histone.v2.evaluator.function.ThrowExceptionFunction;
import ru.histone.v2.evaluator.resource.SchemaResourceLoader;
import ru.histone.v2.evaluator.resource.loader.DataLoader;
import ru.histone.v2.evaluator.resource.loader.FileLoader;
import ru.histone.v2.evaluator.resource.loader.HttpLoader;
import ru.histone.v2.parser.Parser;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.rtti.RunTimeTypeInfo;
import ru.histone.v2.support.HistoneTestCase;
import ru.histone.v2.support.TestRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * @author Alexey Nevinsky
 */
public class HistoneTest {

    protected static ExecutorService executor = Executors.newFixedThreadPool(20);
    protected static RunTimeTypeInfo rtti;
    protected static Evaluator evaluator;
    protected static Parser parser;


    @BeforeAll
    public static void doInitSuite() {
        parser = new Parser();
        evaluator = new Evaluator();

        SchemaResourceLoader loader = new SchemaResourceLoader(executor);
        loader.addLoader(SchemaResourceLoader.DATA_SCHEME, new DataLoader());
        loader.addLoader(SchemaResourceLoader.HTTP_SCHEME, new HttpLoader(executor));
        loader.addLoader(SchemaResourceLoader.FILE_SCHEME, new FileLoader());

        rtti = new RunTimeTypeInfo(executor, loader, evaluator, parser);
        rtti.register(HistoneType.T_GLOBAL, new ThrowExceptionFunction());
        rtti.register(HistoneType.T_GLOBAL, new StopExecutionExceptionFunction());
    }

    public Stream<DynamicTest> loadCases(String param) throws IOException, URISyntaxException {
        final List<DynamicTest> result = new ArrayList<>();
        final List<HistoneTestCase> histoneTestCases = TestRunner.loadTestCases(param);
        for (HistoneTestCase histoneTestCase : histoneTestCases) {
            for (HistoneTestCase.Case testCase : histoneTestCase.getCases()) {
                DynamicTest test = DynamicTest.dynamicTest("Expression: " + testCase.getInput(),
                        () -> {
                            TestRunner.doTest(testCase.getInput(), rtti, testCase, evaluator, parser);
                        });
                result.add(test);
            }
        }
        return result.stream();
    }
}

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

import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.Evaluator;
import ru.histone.v2.evaluator.function.StopExecutionExceptionFunction;
import ru.histone.v2.evaluator.function.ThrowExceptionFunction;
import ru.histone.v2.evaluator.resource.SchemaResourceLoader;
import ru.histone.v2.evaluator.resource.loader.DataLoader;
import ru.histone.v2.evaluator.resource.loader.FileLoader;
import ru.histone.v2.evaluator.resource.loader.HttpLoader;
import ru.histone.v2.exceptions.ParserException;
import ru.histone.v2.parser.Parser;
import ru.histone.v2.parser.SsaOptimizer;
import ru.histone.v2.parser.node.ExpAstNode;
import ru.histone.v2.property.DefaultPropertyHolder;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.rtti.RunTimeTypeInfo;
import ru.histone.v2.support.ExpectedException;
import ru.histone.v2.support.TestRunner;
import ru.histone.v2.utils.AstJsonProcessor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static ru.histone.v2.support.TestRunner.US_LOCALE;

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
        TestRunner runner = new TestRunner();
        return runner.loadCases(param, (testCase) -> {
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"));
            try {
                if (testCase.getInputAST() != null) {
                    ExpAstNode root = AstJsonProcessor.read(testCase.getInputAST());
                    SsaOptimizer optimizer = new SsaOptimizer();
                    optimizer.process(root);

                    String optimizedTree = AstJsonProcessor.write(root);
                    runner.assertEquals(testCase.getExpectedAST(), optimizedTree);
                    return;
                }

                ExpAstNode root = parser.process(testCase.getInput(), "");
                String stringAst = AstJsonProcessor.write(root);
                if (testCase.getExpectedAST() != null) {
                    Assert.assertEquals(testCase.getExpectedAST(), stringAst);
                }

                root = AstJsonProcessor.read(stringAst);
                if (testCase.getExpectedResult() != null) {
                    Context context = Context.createRoot(testCase.getBaseURI(), US_LOCALE, rtti, new DefaultPropertyHolder());
                    if (testCase.getContext() != null) {
                        for (Map.Entry<String, Object> entry : testCase.getContext().entrySet()) {
                            if (entry.getKey().equals("this")) {
                                context.put("this", CompletableFuture.completedFuture(EvalUtils.constructFromObject(entry.getValue())));
                            } else {
                                context.getVars().put(entry.getKey(), CompletableFuture.completedFuture(EvalUtils.constructFromObject(entry.getValue())));
                            }
                        }
                    }

                    String result = evaluator.process(root, context);
                    runner.assertEquals(testCase.getExpectedResult(), result);
                } else if (testCase.getExpectedException() != null) {
                    Context context = Context.createRoot(testCase.getBaseURI(), US_LOCALE, rtti,
                            new DefaultPropertyHolder());
                    evaluator.process(root, context);
                }

                if (testCase.getExpectedException() != null) {
                    Assert.fail("Test doesn't thrown expected exception!");
                }
            } catch (Exception ex) {
                if (testCase.getExpectedException() != null) {
                    ExpectedException e = testCase.getExpectedException();
                    if (ex instanceof ParserException) {
                        Assert.assertEquals(e.getLine(), ((ParserException) ex).getLine());
                    }
                    runner.checkException(ex, e);
                } else {
                    throw new RuntimeException(ex);
                }
            }
        });
    }
}

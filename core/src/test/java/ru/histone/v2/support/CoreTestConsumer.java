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

import org.junit.jupiter.api.Assertions;
import ru.histone.v2.acceptance.ExpectedException;
import ru.histone.v2.acceptance.HistoneTestCase;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.Evaluator;
import ru.histone.v2.exceptions.ParserException;
import ru.histone.v2.parser.Parser;
import ru.histone.v2.parser.SsaOptimizer;
import ru.histone.v2.parser.node.ExpAstNode;
import ru.histone.v2.property.DefaultPropertyHolder;
import ru.histone.v2.rtti.RunTimeTypeInfo;
import ru.histone.v2.utils.AstJsonProcessor;

import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static ru.histone.v2.acceptance.TestUtils.*;

/**
 * @author Alexey Nevinsky
 */
public class CoreTestConsumer implements Consumer<HistoneTestCase.Case> {

    private Parser parser;
    private RunTimeTypeInfo rtti;
    private Evaluator evaluator;

    public CoreTestConsumer(Parser parser, RunTimeTypeInfo rtti, Evaluator evaluator) {
        this.parser = parser;
        this.rtti = rtti;
        this.evaluator = evaluator;
    }

    @Override
    public void accept(HistoneTestCase.Case testCase) {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"));
        try {
            if (testCase.getInputAST() != null) {
                ExpAstNode root = AstJsonProcessor.read(testCase.getInputAST());
                SsaOptimizer optimizer = new SsaOptimizer();
                optimizer.process(root);

                String optimizedTree = AstJsonProcessor.write(root);
                assertEquals(testCase.getExpectedAST(), optimizedTree);
                return;
            }

            ExpAstNode root = parser.process(testCase.getInput(), "");
            String stringAst = AstJsonProcessor.write(root);
            if (testCase.getExpectedAST() != null) {
                Assertions.assertEquals(testCase.getExpectedAST(), stringAst);
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
                assertEquals(testCase.getExpectedResult(), result);
            } else if (testCase.getExpectedException() != null) {
                Context context = Context.createRoot(testCase.getBaseURI(), US_LOCALE, rtti,
                        new DefaultPropertyHolder());
                evaluator.process(root, context);
            }

            if (testCase.getExpectedException() != null) {
                Assertions.fail("Test doesn't thrown expected exception!");
            }
        } catch (Exception ex) {
            if (testCase.getExpectedException() != null) {
                ExpectedException e = testCase.getExpectedException();
                if (ex instanceof ParserException) {
                    Assertions.assertEquals(e.getLine(), ((ParserException) ex).getLine());
                }
                checkException(ex, e);
            } else {
                throw new RuntimeException(ex);
            }
        }
    }
}

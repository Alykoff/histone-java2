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

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import ru.histone.v2.acceptance.HistoneTestCase;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.node.StringEvalNode;
import ru.histone.v2.java_compiler.bcompiler.data.Template;
import ru.histone.v2.property.DefaultPropertyHolder;
import ru.histone.v2.rtti.RunTimeTypeInfo;
import ru.histone.v2.utils.RttiUtils;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static ru.histone.v2.acceptance.TestUtils.US_LOCALE;
import static ru.histone.v2.acceptance.TestUtils.checkException;

/**
 * @author Alexey Nevinsky
 */
public class CompilerTestConsumer implements Consumer<HistoneTestCase.Case> {

    private RunTimeTypeInfo rtti;

    public CompilerTestConsumer(RunTimeTypeInfo rtti) {
        this.rtti = rtti;
    }

    @Override
    public void accept(HistoneTestCase.Case testCase) {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"));
        try {
            URL[] urls = new URL[]{new URL("file:///")};
            URLClassLoader loader = new URLClassLoader(urls, this.getClass().getClassLoader());
            Class<?> t = loader.loadClass(testCase.getInputClass());

            Template template = (Template) t.newInstance();

            if (StringUtils.isNotBlank(testCase.getExpectedAST())) {
                Assertions.assertEquals(template.getStringAst(), testCase.getExpectedAST());
            }

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

            String result = template.render(context)
                    .thenCompose(v -> RttiUtils.callToString(context, v))
                    .thenApply(n -> ((StringEvalNode) n).getValue())
                    .join();

            Assertions.assertEquals(testCase.getExpectedResult(), result);
        } catch (Exception ex) {
            if (testCase.getExpectedException() != null) {
                checkException(ex, testCase.getExpectedException());
            } else {
                throw new RuntimeException(ex);
            }
        }
    }
}

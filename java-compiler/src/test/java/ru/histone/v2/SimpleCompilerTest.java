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

import org.junit.jupiter.api.Test;
import org.testng.Assert;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.node.StringEvalNode;
import ru.histone.v2.java_compiler.bcompiler.Compiler;
import ru.histone.v2.java_compiler.bcompiler.StdLibrary;
import ru.histone.v2.java_compiler.bcompiler.data.Template;
import ru.histone.v2.parser.SsaOptimizer;
import ru.histone.v2.parser.node.AstNode;
import ru.histone.v2.property.DefaultPropertyHolder;
import ru.histone.v2.support.ByteClassLoader;
import ru.histone.v2.utils.AstJsonProcessor;
import ru.histone.v2.utils.RttiUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static ru.histone.v2.acceptance.TestUtils.US_LOCALE;

/**
 * @author Alexey Nevinsky
 */
public class SimpleCompilerTest extends BaseCompilerTest {


    // "input": "{{for x in [1,2,3,4,5,6,7,8,9,10]}}{{if x > 7}}{{true}} {{elseif x > 5}}{{false}} {{else}}{{\"ha\"}} {{/if}}{{/for}}"
    // "expectedAST": "[29,[10,\"10\",2]]"
    // "expectedResult": "8"
    @Test
    public void doTest() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        String expectedAST = "[29,[23,[1,\"1\",\"id\"],1],[23,[1,\"2\",\"id\"],2],[25,null,1,[29,[21,0,1]],[8,[19,\"1\",\"1\"],[21,0,1],[21,0,2]]]]";
        String expectedResult = "1";

        Compiler compiler = new Compiler();

        AstNode tree = AstJsonProcessor.read(expectedAST);
        SsaOptimizer optimizer = new SsaOptimizer();
        optimizer.process(tree);

        expectedAST = AstJsonProcessor.write(tree);

        byte[] classBytes = compiler.compile("Template1", tree);
        Map<String, byte[]> classes = Collections.singletonMap("Template1", classBytes);

        ByteClassLoader loader = new ByteClassLoader(new URL[]{}, getClass().getClassLoader(), classes);
        Class<?> t = loader.loadClass("Template1");

        StdLibrary library = new StdLibrary();

        Template template = (Template) t.newInstance();
        template.setStdLibrary(library);

        Assert.assertEquals(template.getStringAst(), expectedAST);

        String baseURI = "acceptance/simple/function/global";

        Context context = Context.createRoot(baseURI, US_LOCALE, rtti, new DefaultPropertyHolder());

//        if (testCase.getContext() != null) {
//        for (Map.Entry<String, Object> entry : getMap().entrySet()) {
//            if (entry.getKey().equals("this")) {
//                context.put("this", CompletableFuture.completedFuture(EvalUtils.constructFromObject(entry.getValue())));
//            } else {
//                context.getVars().put(entry.getKey(), CompletableFuture.completedFuture(EvalUtils.constructFromObject(entry.getValue())));
//            }
//        }
//        }

        String result = template.render(context)
                .thenCompose(v -> RttiUtils.callToString(context, v))
                .thenApply(n -> ((StringEvalNode) n).getValue())
                .join();
        Assert.assertEquals(result, expectedResult);
    }

    private Map<String, Object> getMap() {
        Map<String, Object> res = new HashMap<>();

        Map<String, Object> values = new LinkedHashMap<>();
        values.put("0", 1L);
        values.put("1", 2L);
        values.put("2", 3L);

        res.put("items", values);

        Map<String, Object> t = new HashMap<>();
        t.put("this", res);
        return t;
    }
}

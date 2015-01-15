/**
 *    Copyright 2013 MegaFon
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package ru.histone.optimizer;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Ignore;
import org.junit.Test;
import ru.histone.HistoneException;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@Ignore
public class SafeASTEvaluationTest extends AbstractOptimizersTest {

//    @Ignore("TODO")
    @Test
    public void expr_int() throws IOException, HistoneException {
        String input = "A{{1+2}}{{x+3}}{{3}}";
        ArrayNode expectedAST = (ArrayNode) getJackson().readTree("[\"A3\",[9,[105,[\"x\"]],[\"3\"]],\"3\"]]");
        ObjectNode context = getNodeFactory().jsonObject();
        ArrayNode initialAST = getHistone().parseTemplateToAST(input);
        ArrayNode optimizedAST = getHistone().optimizeAST(initialAST, context,OptimizationTypes.SAFE_CODE_EVALUATION);

        assertEquals(expectedAST.toString(), optimizedAST.toString());
    }

    @Test
    public void expr_if() throws IOException, HistoneException {
        String input = "{{var b = false}}{{if a}}AAA{{elseif b}}BBB{{/if}}";
        ArrayNode expectedAST = (ArrayNode) getJackson().readTree("[[1001,\"b\",[17]],[1000,[[[16],[\"AAA\"]],[[17],[\"BBB\"]]]]]");
        ObjectNode context = getNodeFactory().jsonObject();
        context.put("a", true);
        ArrayNode initialAST = getHistone().parseTemplateToAST(input);
        ArrayNode optimizedAST = getHistone().optimizeAST(initialAST, context, OptimizationTypes.SAFE_CODE_EVALUATION);

        assertEquals(expectedAST.toString(), optimizedAST.toString());
    }

    @Test
    public void expr_complexIf() throws IOException, HistoneException {
        String input = "a {{if ZZZ is 'login'}} A{{'login'}}A {{elseif ZZZ is 'ttt'}} B{{123}}B {{else}} C{{true}}C {{/if}} b";
        ArrayNode expectedAST = (ArrayNode) getJackson().readTree("[\"a \",[1000,[[[3,[105,[\"ZZZ\"]],[103,\"login\"]],[\" AloginA \"]],[[3,[105,[\"ZZZ\"]],[103,\"ttt\"]],[\" B123B \"]],[[16],[\" CtrueC \"]]]],\" b\"]");
        ObjectNode context = getNodeFactory().jsonObject();
        context.put("a", true);
        ArrayNode initialAST = getHistone().parseTemplateToAST(input);
        ArrayNode optimizedAST = getHistone().optimizeAST(initialAST, context, OptimizationTypes.SAFE_CODE_EVALUATION);

        assertEquals(expectedAST.toString(), optimizedAST.toString());
    }
    @Test
    public void expr_complexFor() throws IOException, HistoneException {
        String input = "a {{var x = 123}} {{for y in items}} A{{y}}{{'ttt'}}A {{/for}} {{x}} b";
        ArrayNode expectedAST = (ArrayNode) getJackson().readTree("[\"a \",[1002,[\"x\"],[105,[\"items\"]],[[\" A\",[105,[\"x\"]],\"tttA \"]]],\" b\"]");
        ObjectNode context = getNodeFactory().jsonObject();
        context.put("a", true);
        ArrayNode initialAST = getHistone().parseTemplateToAST(input);
        ArrayNode optimizedAST = getHistone().optimizeAST(initialAST, context, OptimizationTypes.SAFE_CODE_EVALUATION);

        assertEquals(expectedAST.toString(), optimizedAST.toString());
    }

}

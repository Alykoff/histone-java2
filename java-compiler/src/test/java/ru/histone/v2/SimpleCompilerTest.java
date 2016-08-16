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
        String expectedAST = "[29,[23,[22,[4],\"getMethod\",\"loadJSON\"],1],[23,[7,[22,[4],\"getMethod\",\"asyncLoadJSON\"],[21,0,1]],2],[23,[1,\"/мес.\",\"0\",\"/день\",\"1\"],3],[23,[26,[[21,0,3]],[29,[27,[22,[21,1,3],0,[22,[21,0,1],0,\"dailyMonthRate\"]]]],1],4],[23,[26,0,[29,[23,[22,[22,[22,[21,0,1],\"toString\"],\"replace\",[2,\"[^0-9]+\",\"g\"]],\"replace\",[2,\"^[78]\"]],1],[27,[22,[1,[9,\"+\",[7,[22,[21,0,2],\"toString\"],\"7\"]],\"0\",[22,[21,0,1],\"slice\",0,3],\"1\",[22,[21,0,1],\"slice\",3,3],\"2\",[22,[21,0,1],\"slice\",6,4],\"3\"],\"join\",\" \"]]],2],5],[23,[26,0,[29,[24,[29,[23,[13,[21,1,1],100],0],[23,[13,[21,0,0],10],1],[27,[22,[21,1,2],0,[8,[6,[18,[21,0,0],11],[17,[21,0,0],19]],2,[8,[19,[21,0,1],1],0,[8,[7,[7,[19,[21,0,1],2],[19,[21,0,1],3]],[19,[21,0,1],4]],1,2]]]]]],[6,[22,[21,0,2],\"isArray\"],[19,[22,[21,0,2],\"length\"],3]],[29,[27,[21,1,2]]]],\"\\n\\t\\n\"],2],6],[23,[26,0,[29,[23,[22,[21,0,1],\"length\"],2],[27,[8,[7,[5,[21,0,2]],[5,[22,[21,0,1],\"isArray\"]]],\"\",[8,[19,[21,0,2],1],[22,[21,0,1],0,0],[8,[19,[21,0,2],2],[9,[9,[22,[21,0,1],0,0],\" и \"],[22,[21,0,1],0,1]],[9,[9,[22,[22,[21,0,1],\"slice\",0,[14,1]],\"join\",\", \"],\" и \"],[22,[21,0,1],\"slice\",[14,1]]]]]]]],1],7],[23,[26,0,[29,[23,[22,[21,0,1],\"split\",\" \"],1],[23,[22,[22,[21,0,1],0,0],\"split\",\".\"],3],[23,[22,[22,[21,0,3],0,0],\"toNumber\"],4],[23,[22,[22,[21,0,3],0,1],\"toNumber\"],5],[23,[22,[22,[21,0,3],0,2],\"toNumber\"],6],\"\\n\\n\\t\\n\\n\\t\",[8,[6,[6,[21,0,4],[21,0,5]],[21,0,6]],[9,[22,[1,[21,0,4],\"0\",[22,[22,[4],\"getMonthNameLong\",[21,0,5],1],\"toLowerCase\"],\"1\",[21,0,6],\"2\"],\"join\",\" \"],\" г.\"],[8,[6,[21,0,4],[21,0,5]],[9,[22,[1,[22,[22,[4],\"getMonthNameLong\",[21,0,4]],\"toLowerCase\"],\"0\",[21,0,5],\"1\"],\"join\",\" \"],\" г.\"]]],\"\\n\\n\\t\",[24,[29,[23,[22,[22,[21,1,1],0,1],\"split\",\":\"],0],[23,[22,[21,0,0],0,0],1],[23,[22,[21,0,0],0,1],2],\"\\n\\t\\t\\n\\t\\t\",[8,[6,[21,0,1],[21,0,2]],[9,[9,[9,\" в \",[21,0,1]],\":\"],[21,0,2]]],\"\\n\\t\"],[21,0,2]],\"\\n\\n\"],2],8],[23,[26,[[21,0,6]],[29,[23,[28,\"\\n\\t\",[24,\"\\n\\t\\t--\\n\\t\",[19,[21,1,1],\"\"],[29,[23,[8,[21,2,2],[22,[21,3,6],1,[21,2,1],[21,2,2]],\"<i class=\\\"rub\\\">&#8381;</i>\"],0],[23,[8,[22,[21,2,1],\"isNumber\"],[21,2,1],[22,[21,2,1],\"toNumber\"]],1],[23,[8,[15,[21,0,1],0],\"&minus;\"],2],[23,[22,[22,[22,[21,0,1],\"toAbs\"],\"toString\"],\"split\",\".\"],1],[23,[22,[21,0,1],0,0],3],[23,[22,[21,0,1],0,1],4],[23,[8,[21,0,4],[22,[9,[9,\",\",[21,0,4]],\"0\"],\"slice\",0,3]],4],[23,[9,\" \",[21,0,0]],0],[23,[28,[25,null,null,[29,[22,[21,2,3],0,[10,[22,[21,0,0],0,\"last\"],[22,[21,0,0],0,\"index\"]]]],[22,[21,1,3],\"split\"]]],3],[23,[28,[25,null,1,[29,[24,\" \",[19,[13,[22,[21,0,0],0,\"index\"],3],0]],[21,0,1]],[22,[21,1,3],\"split\"]]],3],[23,[28,[25,null,null,[29,[22,[21,2,3],0,[10,[22,[21,0,0],0,\"last\"],[22,[21,0,0],0,\"index\"]]]],[22,[21,1,3],\"split\"]]],3],\"\\n\\t\\t\\n\\t\\t\\n\\t\\t\\n\\t\\t\\n\\t\\t\\n\\t\\t\\n\\t\\t\\n\\n\\t\\t\\n\\n\\t\\t\\n\\t\\t\\n\\t\\t\\n\\n\\t\\t\",[24,\"<i class=\\\"red\\\">\",[21,0,2]],\"\\n\\t\\t\",[24,[29,[23,[8,[19,[21,1,4],null],\",00\",[21,1,4]],0],\"\\n\\t\\t\\t\\n\\t\\t\\t\",[9,[21,1,2],[22,[21,1,3],\"strip\"]],\"<span>\",[21,0,0],\"</span>\",[21,1,0],\"\\n\\t\\t\"],[21,2,3],[29,\"\\n\\t\\t\\t\",[9,[9,[21,1,2],[22,[21,1,3],\"strip\"]],[21,1,4]],[21,1,0],\"\\n\\t\\t\"]],\"\\n\\t\\t\",[24,\"</i>\",[21,0,2]],\"\\n\\n\\t\"]],\"\\n\"],4],[22,[21,0,4],\"strip\"]],3],9],[23,[26,0,[29,[23,[9,[8,[22,[21,0,5],\"isArray\"],[21,0,5],[1]],[1,[22,[22,[4],\"getEnv\"],0,\"CSRF\"],\"CSRF\"]],5],[23,[22,[21,0,3],1,[9,[9,\"pipes://\",[22,[21,0,4],\"toString\"]],[8,[19,[21,0,2],\"GET\"],[9,\"?\",[22,[22,[21,0,5],\"map\",[26,0,[29,[27,[9,[9,[21,0,2],\"=\"],[21,0,1]]]],2]],\"join\",\"&\"]]]],[1,[21,0,2],\"method\",[21,0,5],\"data\",[21,0,1],\"cache\"]],6],[23,[22,[21,0,6],0,\"errors\"],7],[27,[8,[6,[22,[21,0,7],\"isArray\"],[22,[21,0,7],\"length\"]],[8,[6,[19,[22,[4],0,\"server\"],\"java\"],[22,[21,0,7],\"some\",[26,0,[29,[27,[19,[22,[21,0,1],0,\"id\"],\"403\"]]],1]]],[22,[4],\"redirect\",\"/\"],[9,[21,0,6],[1,[22,[22,[22,[21,0,7],\"group\",[26,0,[29,[27,[22,[21,0,1],0,\"msg\"]]],1]],\"keys\"],\"join\",\"<br />\"],\"errorMsg\"]]],[21,0,6]]]],5],10],[23,[26,0,[29,[27,[22,[21,0,2],\"call\",[9,[1,[22,[22,[22,[22,[21,0,1],\"group\",[26,0,[29,[27,[22,[21,0,1],0,\"errorMsg\"]]],1]],\"keys\"],\"filter\",[26,0,[29,[27,[21,0,1]]],1]],\"join\",\"<br />\"],\"0\"],[21,0,1]]]]],2],11],[23,[26,0,[29,\"\\n\\t<div class=\\\"\",[1,\"mf-t-description\",\"0\",\"mf-t-description-mobile\",\"1\"],\"\\\"><span>\\n\\t\\t\\t\\t<span>\",[22,[21,0,1],0,\"content\"],\"</span>\\n\\t\\t\\t</span>\\n\\t</div>\\n\"],1],12],[23,[26,0,[29,\"\\n\\t<div class=\\\"\",[1,\"mf-t-description\",\"0\",[8,[22,[21,0,1],0,\"top\"],\"mf-t-description-top\",\"mf-t-description-bottom\"],\"1\",[8,[22,[21,0,1],0,\"mobile\"],\"\",\"mf-t-description-modile\"],\"2\",[22,[21,0,1],0,\"class\"],\"3\"],\"\\\">\\n\\t\\t\",[22,[21,0,1],0,\"content\"],\"\\n\\t</div>\\n\"],1],13],[23,[1,[21,0,7],\"listFormat\",[21,0,9],\"summFormat\",[21,0,11],\"pipes\",[21,0,8],\"dateFormatStr\",[21,0,12],\"descriptionTileMobile\",[21,0,13],\"descriptionTile\",[21,0,4],\"ratePeriods_macro\",[21,0,6],\"formatNumberText\",[21,0,5],\"phoneNumberFormat\",[22,[21,0,10],\"bind\",false,\"GET\",[21,0,1]],\"getPipe\",[22,[21,0,10],\"bind\",false,\"POST\",[21,0,1]],\"postPipe\",[22,[21,0,10],\"bind\",false,\"GET\",[21,0,2]],\"getPipeAsync\",[22,[21,0,10],\"bind\",false,\"POST\",[21,0,2]],\"postPipeAsync\",[22,[21,0,10],\"bind\",true,\"GET\",[21,0,2]],\"getPipeAsyncCache\",[26,0,[29,[27,[22,[22,[22,[4],\"require\",\"result/result.tpl\",[22,[4],\"context\"]],\"toMacro\"],\"call\",[22,[21,0,0],0,\"arguments\"]]]]],\"result\"],14],[27,[9,[21,0,14],[1,[26,[[21,0,14]],[29,[27,[22,[22,[22,[4],\"require\",\"link/link.tpl\",[21,1,14]],\"toMacro\"],\"call\",[22,[21,0,0],0,\"arguments\"]]]]],\"link\",[26,[[21,0,14]],[29,[27,[22,[22,[22,[4],\"require\",\"modal/modal.tpl\",[21,1,14]],\"toMacro\"],\"call\",[22,[21,0,0],0,\"arguments\"]]]]],\"modal\",[26,[[21,0,14]],[29,[27,[22,[22,[22,[4],\"require\",\"radio/radio.tpl\",[21,1,14]],\"toMacro\"],\"call\",[22,[21,0,0],0,\"arguments\"]]]]],\"radio\",[26,[[21,0,14]],[29,[27,[22,[22,[22,[4],\"require\",\"label/label.tpl\",[21,1,14]],\"toMacro\"],\"call\",[22,[21,0,0],0,\"arguments\"]]]]],\"label\",[26,[[21,0,14]],[29,[27,[22,[22,[22,[4],\"require\",\"timer/timer.tpl\",[21,1,14]],\"toMacro\"],\"call\",[22,[21,0,0],0,\"arguments\"]]]]],\"timer\",[26,[[21,0,14]],[29,[27,[22,[22,[22,[4],\"require\",\"switch/switch.tpl\",[21,1,14]],\"toMacro\"],\"call\",[22,[21,0,0],0,\"arguments\"]]]]],\"switch\",[26,[[21,0,14]],[29,[27,[22,[22,[22,[4],\"require\",\"tabBox/tabBox.tpl\",[21,1,14]],\"toMacro\"],\"call\",[22,[21,0,0],0,\"arguments\"]]]]],\"tabBox\",[26,[[21,0,14]],[29,[27,[22,[22,[4],\"require\",\"inputDate/inputDate.tpl\",[21,1,14]],\"call\",[22,[21,0,0],0,\"arguments\"]]]]],\"inputDate\",[26,[[21,0,14]],[29,[27,[22,[22,[22,[4],\"require\",\"button/button.tpl\",[21,1,14]],\"toMacro\"],\"call\",[22,[21,0,0],0,\"arguments\"]]]]],\"button\",[26,[[21,0,14]],[29,[27,[22,[22,[22,[4],\"require\",\"select/select.tpl\",[21,1,14]],\"toMacro\"],\"call\",[22,[21,0,0],0,\"arguments\"]]]]],\"select\",[26,[[21,0,14]],[29,[27,[22,[22,[22,[4],\"require\",\"calendar/calendar.tpl\",[21,1,14]],\"toMacro\"],\"call\",[22,[21,0,0],0,\"arguments\"]]]]],\"calendar\",[26,[[21,0,14]],[29,[27,[22,[22,[22,[4],\"require\",\"textArea/textArea.tpl\",[21,1,14]],\"toMacro\"],\"call\",[22,[21,0,0],0,\"arguments\"]]]]],\"textArea\",[26,[[21,0,14]],[29,[27,[22,[22,[22,[4],\"require\",\"checkbox/checkbox.tpl\",[21,1,14]],\"toMacro\"],\"call\",[22,[21,0,0],0,\"arguments\"]]]]],\"checkbox\",[26,[[21,0,14]],[29,[27,[22,[22,[22,[4],\"require\",\"inputText/inputText.tpl\",[21,1,14]],\"toMacro\"],\"call\",[22,[21,0,0],0,\"arguments\"]]]]],\"inputText\",[26,[[21,0,14]],[29,[27,[22,[22,[22,[4],\"require\",\"avatar/avatar.tpl\",[21,1,14]],0,\"showAvatar\"],\"call\",[22,[21,0,0],0,\"arguments\"]]]]],\"showAvatar\",[26,[[21,0,14]],[29,[27,[22,[22,[22,[4],\"require\",\"avatar/avatar.tpl\",[21,1,14]],0,\"uploadAvatar\"],\"call\",[22,[21,0,0],0,\"arguments\"]]]]],\"uploadAvatar\",[26,[[21,0,14]],[29,[27,[22,[22,[22,[4],\"require\",\"loader/loader.tpl\",[21,1,14]],\"toMacro\"],\"call\",[22,[21,0,0],0,\"caller\"],[22,[21,0,0],0,\"arguments\"]]]]],\"template\",[26,[[21,0,14]],[29,[27,[22,[22,[22,[4],\"require\",\"stateButton/stateButton.tpl\",[21,1,14]],\"toMacro\"],\"call\",[22,[21,0,0],0,\"arguments\"]]]]],\"stateButton\"]]]]";
        String expectedResult = "8";

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

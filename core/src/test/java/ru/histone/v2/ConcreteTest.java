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
import ru.histone.v2.exceptions.HistoneException;
import ru.histone.v2.support.HistoneTestCase;
import ru.histone.v2.support.TestRunner;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Alexey Nevinsky
 */
public class ConcreteTest extends HistoneTest {

    @Test
    public void concreteTest() throws HistoneException {

        HistoneTestCase.Case testCase = new HistoneTestCase.Case();
        testCase.setExpectedResult("-3");
//        testCase.setContext(getMap());
//        testCase.setContext(getMap());
//        testCase.setExpectedAST("[29,\"e\",[28,\" 5 \",[27,10],\" \"],\"uuu\"]");
//        TestRunner.doTest("{{var f = (n) => n <= 1 ? n : self.callee(n - 1) + self.callee(n - 2)}}{{f(100)}}", rtti, testCase, evaluator, parser);
        TestRunner.doTest("{{(-3.14)->getMethod('toCeil')->call()}}", rtti, testCase, evaluator, parser);
//        TestRunner.doTest("{{var a = 6}}{{a}}", rtti, testCase, evaluator, parser);
    }

    private Map<String, Object> getMap() {
        Map<String, Object> res = new HashMap<>();

        Map<String, Object> values = new LinkedHashMap<>();
        values.put("foo", 1L);
        values.put("bar", 2L);
        values.put("y", 3L);

        res.put("this", values);
        return res;
    }
}

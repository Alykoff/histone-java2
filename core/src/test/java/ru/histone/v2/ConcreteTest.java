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
import ru.histone.v2.acceptance.HistoneTestCase;
import ru.histone.v2.exceptions.HistoneException;
import ru.histone.v2.support.CoreTestConsumer;

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
        testCase.setExpectedResult("true");
        //        testCase.setContext(getMap());
        //        testCase.setExpectedAST("[29,\"e\",[28,\" 5 \",[27,10],\" \"],\"uuu\"]");
        testCase.setInput("{{/^[a-zа-я-]{1,10}$/i->test('Test')}}");
        new CoreTestConsumer(parser, rtti, evaluator).accept(testCase);
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

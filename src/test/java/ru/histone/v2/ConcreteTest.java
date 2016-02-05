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

import org.junit.Test;
import ru.histone.HistoneException;
import ru.histone.v2.test.dto.HistoneTestCase;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by inv3r on 19/01/16.
 */
public class ConcreteTest extends BaseTest {
    @Test
    public void concreteTest() throws HistoneException {
        HistoneTestCase.Case testCase = new HistoneTestCase.Case();
        testCase.setExpectedResult("true");
        testCase.setContext(getMap());
//        testCase.setExpectedAST("[31,[25,[2,\"ab+c\",0],\"re\"],[24,[22,[21,\"re\"],\"test\"],\"ac\"]]");
        doTest("{{getMax(100.03E+8, 200.3E1, 3000.8, 4000.12E1, 3.2E0, 1000.2E+0, 200.122E2)}} ", testCase);
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

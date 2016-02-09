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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import ru.histone.HistoneException;
import ru.histone.v2.rtti.RunTimeTypeInfo;
import ru.histone.v2.test.TestRunner;
import ru.histone.v2.test.dto.HistoneTestCase;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Created by alexey.nevinsky on 25.12.2015.
 */
@RunWith(Parameterized.class)
public class EvaluatorTest extends BaseTest {

    private static final RunTimeTypeInfo rtti = new RunTimeTypeInfo(Executors.newFixedThreadPool(20));

    private String input;
    private HistoneTestCase.Case expected;
    private Integer index;

    public EvaluatorTest(Integer index, Integer testIndex, String testCaseName, String input, HistoneTestCase.Case expected) {
        this.index = index;
        this.input = input;
        this.expected = expected;
    }

    @Parameterized.Parameters(name = " {0}: {2}[{1}] `{3}` ")
    public static Collection<Object[]> data() throws IOException, URISyntaxException {
        final List<Object[]> result = new ArrayList<>();
        final List<HistoneTestCase> histoneTestCases = TestRunner.loadTestCases();
        int i = 1;
        for (HistoneTestCase histoneTestCase : histoneTestCases) {
            System.out.println("Run test '" + histoneTestCase.getName() + "'");
            int j = 1;
            for (HistoneTestCase.Case testCase : histoneTestCase.getCases()) {
                System.out.println("Expression: " + testCase.getInput());
                String testName = histoneTestCase.getName();
                result.add(new Object[]{i++, j++, testName, testCase.getInput(), testCase});
            }
        }
        return result;
    }

    @Test
    public void test() throws HistoneException {
        doTest(input, rtti, expected);
    }
}

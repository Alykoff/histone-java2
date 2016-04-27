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

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import ru.histone.v2.evaluator.Evaluator;
import ru.histone.v2.evaluator.resource.SchemaResourceLoader;
import ru.histone.v2.evaluator.resource.loader.DataLoader;
import ru.histone.v2.evaluator.resource.loader.FileLoader;
import ru.histone.v2.evaluator.resource.loader.HttpLoader;
import ru.histone.v2.exceptions.HistoneException;
import ru.histone.v2.parser.Parser;
import ru.histone.v2.rtti.RunTimeTypeInfo;
import ru.histone.v2.support.HistoneTestCase;
import ru.histone.v2.support.JerseyServerResource;
import ru.histone.v2.support.TestRunner;

import javax.ws.rs.core.Application;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Alexey Nevinsky
 */
@RunWith(Parameterized.class)
public class HttpResourceLoaderTest extends JerseyTestNg.ContainerPerClassTest {

    private static final ExecutorService executor = Executors.newFixedThreadPool(20);
    private static final RunTimeTypeInfo rtti;
    private static final Evaluator evaluator;
    private static final Parser parser;

    static {
        parser = new Parser();
        evaluator = new Evaluator();
        SchemaResourceLoader loader = new SchemaResourceLoader(executor);
        loader.addLoader(SchemaResourceLoader.DATA_SCHEME, new DataLoader());
        loader.addLoader(SchemaResourceLoader.HTTP_SCHEME, new HttpLoader(executor));
        loader.addLoader(SchemaResourceLoader.FILE_SCHEME, new FileLoader());
        rtti = new RunTimeTypeInfo(executor, loader, evaluator, parser);
    }

    private String input;
    private HistoneTestCase.Case expected;
    private Integer index;

    public HttpResourceLoaderTest(Integer index, Integer testIndex, String testCaseName, String input, HistoneTestCase.Case expected) {
        this.index = index;
        this.input = input;
        this.expected = expected;
    }

    @Parameterized.Parameters(name = " {0}: {2}[{1}] `{3}` ")
    public static Collection<Object[]> data() throws IOException, URISyntaxException {
        final List<Object[]> result = new ArrayList<>();
        final List<HistoneTestCase> histoneTestCases = TestRunner.loadTestCases("http");
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

    @Override
    protected Application configure() {
        forceSet(TestProperties.CONTAINER_PORT, "4442");

        return new ResourceConfig(JerseyServerResource.class);
    }

    @Test
    public void doSomething() throws Exception {

    }

    @Test
    public void test() throws HistoneException {
        TestRunner.doTest(input, rtti, expected, evaluator, parser);
    }
}

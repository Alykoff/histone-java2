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

import org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.StdErrLog;
import org.junit.jupiter.api.Test;
import ru.histone.v2.acceptance.HistoneTestCase;
import ru.histone.v2.acceptance.TestServerResource;
import ru.histone.v2.support.CoreTestConsumer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Alexey Nevinsky
 */
public class HttpConcreteTest extends HistoneTest {
    public static final String BASE_URI = "http://127.0.0.1:4442/";
    public Server server;

    @Test
    public void concreteTest() throws Exception {

        HistoneTestCase.Case testCase = new HistoneTestCase.Case();
        testCase.setExpectedResult("true");
        testCase.setContext(getMap());
        testCase.setInput("{{var response = loadJSON('http://127.0.0.1:4442/', [headers: ['via': 123]])}}{{response -> isArray() && response.headers['via'] != '123'}}");
        try {
            ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
            servletContextHandler.setContextPath("/");

            // CXF Servlet
            ServletHolder cxfServletHolder = new ServletHolder(new CXFNonSpringJaxrsServlet());
            cxfServletHolder.setInitParameter("jaxrs.serviceClasses", TestServerResource.class.getName());
            servletContextHandler.addServlet(cxfServletHolder, "/*");

            server = new Server(4442);
            server.setHandler(servletContextHandler);

            server.start();
            Log.setLog(new StdErrLog());

            new CoreTestConsumer(parser, rtti, evaluator).accept(testCase);
        } finally {
            try {
                server.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

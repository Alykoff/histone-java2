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
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import ru.histone.v2.acceptance.CasePack;
import ru.histone.v2.acceptance.TestServerResource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.stream.Stream;

/**
 * @author Alexey Nevinsky
 */
@CasePack("http")
public class HttpResourceLoaderTest extends HistoneTest {

    private final String BASE_URI = "http://127.0.0.1:4442/";
    private Server server;

    @TestFactory
    @Override
    public Stream<DynamicTest> loadCases(String param) throws IOException, URISyntaxException {
        return super.loadCases(param)
                    .map(test -> DynamicTest.dynamicTest(test.getDisplayName(), () -> {
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

                            test.getExecutable().execute();
                        } finally {
                            try {
                                server.stop();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }));
    }
}

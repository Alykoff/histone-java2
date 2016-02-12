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

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;

/**
 * @author alexey.nevinsky
 */
public class HttpResourceLoaderTest extends JerseyTestNg.ContainerPerMethodTest {

    @Override
    protected Application configure() {
        forceSet(TestProperties.CONTAINER_PORT, "4442");

        return new ResourceConfig(Resource.class);
    }

    @Test
    public void doSomething() throws Exception {

    }

    @Path("/")
    @Singleton
    @Produces("text/plain")
    public static class Resource {

        private int i = 1;

        @GET
        public int get() {
            return i++;
        }
    }
}

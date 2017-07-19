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
package ru.histone.v2.acceptance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.jersey.server.ContainerRequest;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

/**
 * @author Alexey Nevinsky
 */
@Path("/")
@Singleton
@Produces("application/json")
@Consumes("*/*")
public class JerseyServerResource {

    @Context
    private UriInfo uri;

    @Context
    private ContainerRequest request;

    private volatile AtomicInteger counter = new AtomicInteger();
    private volatile AtomicInteger counter1 = new AtomicInteger(0);

    @GET
    public String get() throws JsonProcessingException {
        return getResString();
    }

    @GET
    @Path("news")
    public String news() throws JsonProcessingException {
        return getResString();
    }

    private String getResString() throws JsonProcessingException {
        Map<String, Object> res = new HashMap<>();
        res.put("path", "/" + uri.getPath());
        res.put("query", uri.getQueryParameters().entrySet().stream()
                            .map(e -> e.getKey() + "=" + e.getValue().get(0))
                            .collect(Collectors.joining("&"))
        );
        res.put("method", request.getMethod());
        res.put("headers", request.getHeaders().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0))));
        res.put("body", readJson());

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(res);
    }

    private Map<String, String> readJson() {
        if (APPLICATION_JSON_TYPE.equals(request.getMediaType())) {
            return request.readEntity(Map.class);
        }
        return readBody();
    }

    private Map<String, String> readBody() {
        MultivaluedMap<String, String> map = request.readEntity(MultivaluedMap.class);
        if (map == null) {
            return Collections.emptyMap();
        }
        Map<String, String> res = new HashMap<>(map.size());
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            res.put(entry.getKey(), entry.getValue() != null ? entry.getValue().get(0) : null);
        }
        return res;
    }

    @POST
    public String post() throws JsonProcessingException {
        return getResString();
    }

    @PUT
    public String put() throws JsonProcessingException {
        return getResString();
    }

    @DELETE
    public String delete() throws JsonProcessingException {
        return getResString();
    }

    @OPTIONS
    public String options() throws JsonProcessingException {
        return getResString();
    }

    @HEAD
    public String head() throws JsonProcessingException {
        return getResString();
    }

    @GET
    @Path("longRequest")
    public String longRequest() throws InterruptedException {
        Thread.sleep(3000);
        return "done";
    }

    @GET
    @Path("redirect:200")
    public Response redirectGET() {
        return Response.temporaryRedirect(URI.create("/")).build();
    }

    @OPTIONS
    @Path("redirect:200")
    public Response redirectOPTIONS() {
        return Response.temporaryRedirect(URI.create("/")).build();
    }

    @POST
    @Path("redirect:200")
    public Response redirectPOST() {
        return Response.temporaryRedirect(URI.create("/")).build();
    }

    @PUT
    @Path("redirect:200")
    public Response redirectPUT() {
        return Response.temporaryRedirect(URI.create("/")).build();
    }

    @DELETE
    @Path("redirect:200")
    public Response redirectDELETE() {
        return Response.temporaryRedirect(URI.create("/")).build();
    }

    @HEAD
    @Path("redirect:200")
    public Response redirectHEAD() {
        return Response.temporaryRedirect(URI.create("/")).build();
    }

    @GET
    @Path("testCache")
    public String testCache(final @Context ContainerRequest request) throws JsonProcessingException {
        request.getMethod();
        int i = counter1.incrementAndGet();
        System.out.println("count = " + i);
        Map<String, Object> res = Collections.singletonMap("requestCount", i);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(res);
    }
}

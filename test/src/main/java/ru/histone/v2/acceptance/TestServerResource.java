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

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Alexey Nevinsky
 */
@Path("/")
//@Singleton
@Produces("application/json")
@Consumes("*/*")
public class TestServerResource {

    @Context
    private UriInfo uri;

    @Context
    private Request request;

    @Context
    private HttpHeaders headers;

    @Context
    private Providers providers;

    @Context
    private ContextResolver contextResolver;

    private volatile AtomicInteger counter1 = new AtomicInteger(0);

    @GET
    public String get() throws JsonProcessingException {
        return getResString(null);
    }

    @GET
    @Path("news")
    public String news() throws JsonProcessingException {
        return getResString(null);
    }

    private String getResString(MultivaluedMap<String, String> input) throws JsonProcessingException {
        Map<String, Object> res = new HashMap<>();
        res.put("path", "/".equals(uri.getPath()) ? "/" : "/" + uri.getPath());
        res.put("query", uri.getQueryParameters().entrySet().stream()
                            .map(e -> e.getKey() + "=" + e.getValue().get(0))
                            .collect(Collectors.joining("&"))
        );
        res.put("method", request.getMethod());
        res.put("headers", headers.getRequestHeaders().entrySet().stream()
                                  .collect(Collectors.toMap(Map.Entry::getKey,
                                                            e -> e.getValue().get(0) != null ? e.getValue().get(0) : ""
                                  ))
        );
        res.put("body", readJson(input));

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(res);
    }

    private Map<String, String> readJson(MultivaluedMap<String, String> input) {
        if (input != null) {
            return input.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey,
                                                  e -> e.getValue().get(0) != null ? e.getValue().get(0) : ""
                        ));
        }
        return Collections.emptyMap();
    }

    @POST
    public String post(MultivaluedMap<String, String> input) throws JsonProcessingException {
        return getResString(input);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String put(MultivaluedMap<String, String> input) throws IOException {
        return getResString(input);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public String put(String input) throws IOException {
        return getResString(null);
    }


    @DELETE
    public String delete() throws JsonProcessingException {
        return getResString(null);
    }

    @HEAD
    public String head() throws JsonProcessingException {
        return getResString(null);
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
    public String testCache() throws JsonProcessingException {
        int i = counter1.incrementAndGet();
        System.out.println("count = " + i);
        Map<String, Object> res = Collections.singletonMap("requestCount", i);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(res);
    }
}

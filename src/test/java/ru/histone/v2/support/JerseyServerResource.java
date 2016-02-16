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
package ru.histone.v2.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.jersey.server.ContainerRequest;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author alexey.nevinsky
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
        res.put("body", request.readEntity(MultivaluedMap.class));

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(res);
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
}

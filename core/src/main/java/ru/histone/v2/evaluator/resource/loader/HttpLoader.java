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
package ru.histone.v2.evaluator.resource.loader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.cxf.jaxrs.provider.FormEncodingProvider;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.resource.ContentType;
import ru.histone.v2.evaluator.resource.HistoneStringResource;
import ru.histone.v2.evaluator.resource.Resource;
import ru.histone.v2.exceptions.FunctionExecutionException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Alexey Nevinsky
 */
public class HttpLoader implements Loader {

    public static final String READ_TIMEOUT = "http.receive.timeout";
    public static final String CONNECT_TIMEOUT = "http.connection.timeout";

    public static final String HTTP_SCHEME = "http";

    private static final String[] PROHIBITED_HEADERS = {"accept-charset", "accept-encoding", "access-control-request-headers",
            "access-control-request-method", "connection", "content-length", "cookie", "cookie2",
            "content-transfer-encoding", "date", "expect", "host", "keep-alive", "origin", "referer", "te", "trailer",
            "transfer-encoding", "upgrade", "user-agent", "via"};

    private static final List<String> ALLOWED_METHODS = Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD");

    @Override
    public CompletableFuture<Resource> loadResource(Context ctx, URI url, Map<String, Object> params) {
        String method = getMethod(params);
        String path = url.getPath();
        if (ctx.getCtxCache() != null) {
            if (!"GET".equals(method)) {
                ctx.getCtxCache().remove(path);
            } else {
                if (Boolean.TRUE.equals(params.get("cache"))) {
                    return loadResourceFromCache(ctx, path, url, params);
                } else if ("fullCheck".equals(params.get("cache"))) {
                    try {
                        String paramsStr = new ObjectMapper().writeValueAsString(params);
                        String key = DigestUtils.sha512Hex((paramsStr + path).getBytes());
                        return loadResourceFromCache(ctx, key, url, params);
                    } catch (JsonProcessingException e) {
                        throw new FunctionExecutionException(e.getMessage(), e);
                    }
                }
            }
        }

        return loadRequest(url, params);
    }

    protected CompletableFuture<Resource> loadRequest(URI url, Map<String, Object> params) {
        return doRequest(url, params, String.class)
                .thenApply(s -> (Resource) new HistoneStringResource(s, url.toString(), ContentType.TEXT.getId()));
    }


    private CompletableFuture<Resource> loadResourceFromCache(Context ctx, String key, URI url, Map<String, Object> params) {
        return (CompletableFuture<Resource>) ctx.getCtxCache().computeIfAbsent(key, k -> loadRequest(url, params));
    }


    @Override
    public String getScheme() {
        return HTTP_SCHEME;
    }

    protected <T> CompletableFuture<T> doRequest(URI url, Map<String, Object> params, Class<T> clazz) {
        WebTarget webTarget = getWebTarget(url);

        String method = getMethod(params);
        MultivaluedMap<String, Object> headers = getHeaders(params);

        String type = MediaType.APPLICATION_FORM_URLENCODED;
        if (headers != null && headers.containsKey("Content-Type")) {
            type = String.valueOf(headers.getFirst("Content-Type"));
        }
        MultivaluedMap<String, String> data = getData(params.get("data"));

        Entity entity = null;
        if ("GET".equals(method) || "DELETE".equals(method) || "OPTIONS".equals(method)) {
            for (Map.Entry<String, List<String>> entry : data.entrySet()) {
                webTarget = webTarget.queryParam(entry.getKey(), entry.getValue().get(0));
            }
        } else {
            entity = Entity.entity(data, type);
        }

        return CompletableFuture.completedFuture(
                webTarget
                        .request()
                        .headers(headers)
                        .method(method, entity, clazz)
        );
    }

    private String getMethod(Map<String, Object> params) {
        String method = "GET";
        if (params.containsKey("method")) {
            Object m = params.get("method");
            if (m instanceof String && ALLOWED_METHODS.contains(((String) m).toUpperCase())) {
                method = ((String) m).toUpperCase();
            }
        }
        return method;
    }

    private MultivaluedMap<String, Object> getHeaders(Map<String, Object> params) {
        MultivaluedMap<String, Object> res = new MultivaluedHashMap<>();
        if (!params.containsKey("headers")) {
            return res;
        }

        Map<String, Object> headerMap = (Map<String, Object>) params.get("headers");

        headerMap.entrySet().stream()
                 .filter(e -> {
                     String name = e.getKey();
                     return e.getValue() != null
                             && name.indexOf("sec-") != 0
                             && name.indexOf("proxy-") != 0
                             && !Arrays.asList(PROHIBITED_HEADERS).contains(name.toLowerCase());
                 })
                 .forEach(e -> res.putSingle(e.getKey(), e.getValue()));
        return res;
    }

    private MultivaluedMap<String, String> getData(Object data) {
        MultivaluedMap<String, String> res = new MultivaluedHashMap<>();
        if (data != null) {
            if (data instanceof List) {
                int i = 0;
                for (Object obj : (List) data) {
                    res.putSingle(i++ + "", String.valueOf(obj));
                }
            } else if (data instanceof Map) {
                for (Map.Entry<String, Object> entry : ((Map<String, Object>) data).entrySet()) {
                    res.putSingle(entry.getKey(), String.valueOf(entry.getValue()));
                }
            } else {
                res.putSingle("0", String.valueOf(data));
            }
        }
        return res;
    }

    protected WebTarget getWebTarget(URI url) {
        int connectTimeout = 2000;
        int readTimeout = 4000;

        return buildClient(connectTimeout, readTimeout)
                .target(url);
    }


    protected Client buildClient(int connectionTimeout, int readTimeout) {
        final Client client = ClientBuilder.newClient();

        client.property(CONNECT_TIMEOUT, connectionTimeout);
        client.property(READ_TIMEOUT, readTimeout);

        client.register(FormEncodingProvider.class);
        client.register(JacksonJsonProvider.class);
        return client;
    }
}

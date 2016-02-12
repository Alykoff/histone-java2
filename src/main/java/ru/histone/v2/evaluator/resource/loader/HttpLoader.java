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

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.rx.RxClient;
import org.glassfish.jersey.client.rx.RxWebTarget;
import org.glassfish.jersey.client.rx.java8.RxCompletionStage;
import org.glassfish.jersey.client.rx.java8.RxCompletionStageInvoker;
import org.glassfish.jersey.message.internal.FormMultivaluedMapProvider;
import ru.histone.v2.evaluator.resource.ContentType;
import ru.histone.v2.evaluator.resource.HistoneStringResource;
import ru.histone.v2.evaluator.resource.Resource;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * @author alexey.nevinsky
 */
public class HttpLoader implements Loader {

    private static final String[] PROHIBITED_HEADERS = {"accept-charset", "accept-encoding", "access-control-request-headers",
            "access-control-request-method", "connection", "content-length", "cookie", "cookie2",
            "content-transfer-encoding", "date", "expect", "host", "keep-alive", "origin", "referer", "te", "trailer",
            "transfer-encoding", "upgrade", "user-agent", "via"};

    private static final List<String> ALLOWED_METHODS = Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD");

    private final ExecutorService jerseyExecutor;

    public HttpLoader(ExecutorService jerseyExecutor) {
        this.jerseyExecutor = jerseyExecutor;
    }

    @Override
    public CompletableFuture<Resource> loadResource(URI url, Map<String, Object> params) {
        return doRequest(url, params, String.class)
                .thenApply(s -> {
                    Resource res = new HistoneStringResource(s, url.toString(), ContentType.TEXT.getId());
                    return res;
                });
    }

    protected <T> CompletableFuture<T> doRequest(URI url, Map<String, Object> params, Class<T> clazz) {
        RxWebTarget webTarget = getWebTarget(url);

        String method = getMethod(params);
        MultivaluedMap<String, Object> headers = getHeaders(params);

        MediaType type = MediaType.APPLICATION_FORM_URLENCODED_TYPE;
        if (headers != null && headers.containsKey("Content-Type")) {
            type = MediaType.valueOf(String.valueOf(headers.containsKey("Content-Type")));
        }
        Entity entity = Entity.entity(params.get("data"), type);

        return (CompletableFuture<T>) webTarget

                .request()
                .headers(headers)
                .rx()
                .method(method, entity, clazz);
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
        if (!params.containsKey("headers")) {
            return null;
        }
        MultivaluedMap<String, Object> res = new MultivaluedHashMap<>();
        ((Map<String, Object>) params.get("headers")).forEach(res::putSingle);
        return res;
    }
//        URI newLocation = URI.create(location.toString().replace("#fragment", ""));
//
//        StringEvalNode methodNode = (StringEvalNode) args.getProperty("method");
//        final String method = methodNode != null ? methodNode.getValue() : "GET";
//
//        final Map<String, String> headers = new HashMap<>();
////        if (args.getProperty("headers") != null) {
////            for (Map.Entry<Object, Node> en : requestMap.get("headers").getAsObject().getElements().entrySet()) {
////                String value = null;
////                if (en.getValue().isUndefined())
////                    value = "undefined";
////                else
////                    value = en.getValue().getAsString().getValue();
////                headers.put(en.getKey().toString(), value);
////            }
////        }
//
//        final Map<String, String> filteredHeaders = filterRequestHeaders(headers);
//        final EvalNode data = args.getProperty("data") != null ? args.getProperty("data") : null;
//
//        // Prepare request
//        HttpRequestBase request = new HttpGet(newLocation);
//        if ("POST".equalsIgnoreCase(method)) {
//            request = new HttpPost(newLocation);
//        } else if ("PUT".equalsIgnoreCase(method)) {
//            request = new HttpPut(newLocation);
//        } else if ("DELETE".equalsIgnoreCase(method)) {
//            request = new HttpDelete(newLocation);
//        } else if ("TRACE".equalsIgnoreCase(method)) {
//            request = new HttpTrace(newLocation);
//        } else if ("OPTIONS".equalsIgnoreCase(method)) {
//            request = new HttpOptions(newLocation);
//        } else if ("PATCH".equalsIgnoreCase(method)) {
//            request = new HttpPatch(newLocation);
//        } else if ("HEAD".equalsIgnoreCase(method)) {
//            request = new HttpHead(newLocation);
//        } else if (method != null && !"GET".equalsIgnoreCase(method)) {
//            Resource res = new HistoneStreamResource(null, location.toString(), ContentType.TEXT.getId());
//            return CompletableFuture.completedFuture(res);
//        }
//
//        for (Map.Entry<String, String> en : filteredHeaders.entrySet()) {
//            request.setHeader(en.getKey(), en.getValue());
//        }
//
//        if (("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) && data != null) {
//            String contentType = filteredHeaders.get("content-type") == null ? "" : filteredHeaders.get("content-type");
//            String stringData;
//            if (data.getType() == HistoneType.T_ARRAY) {
//                stringData = ToQueryString.toQueryString(data.getAsObject(), null, "&");
//                contentType = "application/x-www-form-urlencoded";
//            } else {
//                stringData = data.getValue() + "";
//            }
//            if (stringData != null) {
//                StringEntity se;
//                try {
//                    se = new StringEntity(stringData);
//                } catch (UnsupportedEncodingException e) {
//                    throw new ResourceLoadException(String.format("Can't encode data '%s'", stringData));
//                }
//                ((HttpEntityEnclosingRequestBase) request).setEntity(se);
//            }
//            request.setHeader("Content-Type", contentType);
//        }
//
//        if (request.getHeaders("content-Type").length == 0) {
//            request.setHeader("Content-Type", "");
//        }
//
//        AsyncHttpClient client = new AsyncHttpClient();
//        client.executeRequest(request);
//
//        // Execute request
//        HttpClient client = new DefaultHttpClient(httpClientConnectionManager);
//        ((AbstractHttpClient) client).setRedirectStrategy(new RedirectStrategy());
//        InputStream input = null;
//        try {
//            HttpResponse response = client.execute(request);
//            input = response.getEntity() == null ? null : response.getEntity().getContent();
//        } catch (IOException e) {
//            throw new ResourceLoadException(String.format("Can't load resource '%s'", location.toString()));
//        } finally {
//        }
//        Resource res = new HistoneStreamResource(input, location.toString(), ContentType.TEXT.getId());
//        return CompletableFuture.completedFuture(res);

//    private CompletableFuture<Resource> loadHttpResource(URI location, MapEvalNode args) {
//        URI newLocation = URI.create(location.toString().replace("#fragment", ""));
//
//        StringEvalNode methodNode = (StringEvalNode) args.getProperty("method");
//        final String method = methodNode != null ? methodNode.getValue() : "GET";
//
//        final Map<String, String> headers = new HashMap<>();
////        if (args.getProperty("headers") != null) {
////            for (Map.Entry<Object, Node> en : requestMap.get("headers").getAsObject().getElements().entrySet()) {
////                String value = null;
////                if (en.getValue().isUndefined())
////                    value = "undefined";
////                else
////                    value = en.getValue().getAsString().getValue();
////                headers.put(en.getKey().toString(), value);
////            }
////        }
//
//        final Map<String, String> filteredHeaders = filterRequestHeaders(headers);
//        final EvalNode data = args.getProperty("data") != null ? args.getProperty("data") : null;
//
//        // Prepare request
//        HttpRequestBase request = new HttpGet(newLocation);
//        if ("POST".equalsIgnoreCase(method)) {
//            request = new HttpPost(newLocation);
//        } else if ("PUT".equalsIgnoreCase(method)) {
//            request = new HttpPut(newLocation);
//        } else if ("DELETE".equalsIgnoreCase(method)) {
//            request = new HttpDelete(newLocation);
//        } else if ("TRACE".equalsIgnoreCase(method)) {
//            request = new HttpTrace(newLocation);
//        } else if ("OPTIONS".equalsIgnoreCase(method)) {
//            request = new HttpOptions(newLocation);
//        } else if ("PATCH".equalsIgnoreCase(method)) {
//            request = new HttpPatch(newLocation);
//        } else if ("HEAD".equalsIgnoreCase(method)) {
//            request = new HttpHead(newLocation);
//        } else if (method != null && !"GET".equalsIgnoreCase(method)) {
//            Resource res = new HistoneStreamResource(null, location.toString(), ContentType.TEXT.getId());
//            return CompletableFuture.completedFuture(res);
//        }
//
//        for (Map.Entry<String, String> en : filteredHeaders.entrySet()) {
//            request.setHeader(en.getKey(), en.getValue());
//        }
//
//        if (("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) && data != null) {
//            String contentType = filteredHeaders.get("content-type") == null ? "" : filteredHeaders.get("content-type");
//            String stringData;
//            if (data.getType() == HistoneType.T_ARRAY) {
//                stringData = ToQueryString.toQueryString(data.getAsObject(), null, "&");
//                contentType = "application/x-www-form-urlencoded";
//            } else {
//                stringData = data.getValue() + "";
//            }
//            if (stringData != null) {
//                StringEntity se;
//                try {
//                    se = new StringEntity(stringData);
//                } catch (UnsupportedEncodingException e) {
//                    throw new ResourceLoadException(String.format("Can't encode data '%s'", stringData));
//                }
//                ((HttpEntityEnclosingRequestBase) request).setEntity(se);
//            }
//            request.setHeader("Content-Type", contentType);
//        }
//
//        if (request.getHeaders("content-Type").length == 0) {
//            request.setHeader("Content-Type", "");
//        }
//
//        AsyncHttpClient client = new AsyncHttpClient();
//        client.executeRequest(request);
//
//        // Execute request
//        HttpClient client = new DefaultHttpClient(httpClientConnectionManager);
//        ((AbstractHttpClient) client).setRedirectStrategy(new RedirectStrategy());
//        InputStream input = null;
//        try {
//            HttpResponse response = client.execute(request);
//            input = response.getEntity() == null ? null : response.getEntity().getContent();
//        } catch (IOException e) {
//            throw new ResourceLoadException(String.format("Can't load resource '%s'", location.toString()));
//        } finally {
//        }
//        Resource res = new HistoneStreamResource(input, location.toString(), ContentType.TEXT.getId());
//        return CompletableFuture.completedFuture(res);
//    }

    protected RxWebTarget<RxCompletionStageInvoker> getWebTarget(URI url) {
        //todo get default and from config
        int connectTimeout = 2000;
        int readTimeout = 4000;

        return buildClient(connectTimeout, readTimeout)
                .target(url);
    }


    protected RxClient<RxCompletionStageInvoker> buildClient(int connectionTimeout, int readTimeout) {
        RxClient<RxCompletionStageInvoker> client = RxCompletionStage.newClient(jerseyExecutor);

        client.property(ClientProperties.CONNECT_TIMEOUT, connectionTimeout);
        client.property(ClientProperties.READ_TIMEOUT, readTimeout);

        client.register(FormMultivaluedMapProvider.class);
        return client;
    }

    private Map<String, String> filterRequestHeaders(Map<String, String> requestHeaders) {
        return requestHeaders.entrySet().stream()
                .filter(e -> {
                    String name = e.getKey().toLowerCase();
                    return e.getValue() != null
                            && name.indexOf("sec-") != 0
                            && name.indexOf("proxy-") != 0
                            && !Arrays.asList(PROHIBITED_HEADERS).contains(name);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}

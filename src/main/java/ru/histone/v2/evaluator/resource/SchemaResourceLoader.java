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
package ru.histone.v2.evaluator.resource;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.evaluator.resource.loader.Loader;
import ru.histone.v2.exceptions.ResourceLoadException;
import ru.histone.v2.utils.BOMInputStream;
import ru.histone.v2.utils.PathUtils;

import java.io.*;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * @author alexey.nevinsky
 */
public class SchemaResourceLoader implements HistoneResourceLoader {
    public static final String HTTP_SCHEME = "http";
    public static final String FILE_SCHEME = "file";
    public static final String DATA_SCHEME = "data";
    private static final Logger log = LoggerFactory.getLogger(SchemaResourceLoader.class);
    private final Executor executor;
    private final Map<String, Loader> loaders;

    public SchemaResourceLoader(Executor executor) {
        this.executor = executor;
        loaders = new HashMap<>();
    }

    public void addLoader(String scheme, Loader loader) {
        loaders.put(scheme, loader);
    }

    @Override
    public String resolveFullPath(String location, String baseLocation) throws ResourceLoadException {
        log.debug("Trying to load resource from location={}, with baseLocation={}", new Object[]{location, baseLocation});

        try {
            URI fullLocation = makeFullLocation(location, baseLocation);
            return fullLocation.toString();
        } catch (IllegalAccessException e) {
            throw new ResourceLoadException(e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture<Resource> load(String href, String baseHref, MapEvalNode args) throws ResourceLoadException {
        log.debug("Trying to load resource from location={}, with baseLocation={}", new Object[]{href, baseHref});

        return CompletableFuture.completedFuture(null)
                .thenCompose(x -> {
                    String fullLocation = PathUtils.resolveUrl(href, baseHref);
                    ru.histone.evaluator.functions.global.URI uri = PathUtils.parseURI(fullLocation);

                    if (baseHref == null && uri.getScheme() == null) {
                        throw new ResourceLoadException("Base HREF is empty and resource location is not absolute!");
                    }

                    URI loadUri;
                    Map<String, Object> params = new HashMap<>();

                    try {
                        if (!DATA_SCHEME.equals(uri.getScheme())) {
                            loadUri = makeFullLocation(href, baseHref);
                            //todo put params to map
                        } else {
                            loadUri = makeFullLocation(href, "");
                        }
                    } catch (IllegalAccessException e) {
                        throw new ResourceLoadException(e.getMessage(), e);
                    }
                    Loader loader = loaders.get(uri.getScheme());
                    if (loader != null) {
                        return loader.loadResource(loadUri, params);
                    }

                    throw new ResourceLoadException(String.format("Unsupported scheme for resource loading: '%s'", uri.getScheme()));
                });
    }

    private CompletableFuture<Resource> loadFileResource(URI location) {
        InputStream stream = readFile(location);
        BOMInputStream bomStream = readBomStream(location, stream);
        Resource res = new HistoneStreamResource(bomStream, location.toString(), ContentType.TEXT.getId());
        return CompletableFuture.completedFuture(res);
    }

    private BOMInputStream readBomStream(URI location, InputStream stream) {
        BOMInputStream bomStream;
        try {
            bomStream = new BOMInputStream(stream);
            if (bomStream.getBOM() != BOMInputStream.BOM.NONE) {
                bomStream.skipBOM();
            }
        } catch (IOException e) {
            throw new ResourceLoadException(String.format("Error with BOMInputStream for file '%s'", location.toString()));
        }
        return bomStream;
    }

    private InputStream readFile(URI location) {
        InputStream stream;
        File file = new File(location);
        if (file.exists() && file.isFile() && file.canRead()) {
            try {
                stream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new ResourceLoadException("File not found", e);
            }
        } else {
            throw new ResourceLoadException(String.format("Can't read file '%s'", location.toString()));
        }
        return stream;
    }

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

//    private String getQueryString(MapEvalNode node) {
//        Map<String, EvalNode>
//    }

    private Map<String, String> filterRequestHeaders(Map<String, String> requestHeaders) {
        String[] prohibited = {"accept-charset", "accept-encoding", "access-control-request-headers", "access-control-request-method",
                "connection", "content-length", "cookie", "cookie2", "content-transfer-encoding", "date", "expect", "host", "keep-alive",
                "origin", "referer", "te", "trailer", "transfer-encoding", "upgrade", "user-agent", "via"};
        Map<String, String> headers = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
            if (entry.getValue() == null)
                continue;
            String name = entry.getKey().toLowerCase();
            if (name.indexOf("sec-") == 0)
                continue;
            if (name.indexOf("proxy-") == 0)
                continue;
            if (Arrays.asList(prohibited).contains(name))
                continue;
            headers.put(entry.getKey(), entry.getValue());
        }
        return headers;
    }

    public URI makeFullLocation(String location, String baseLocation) throws IllegalAccessException {
        if (location == null) {
            throw new ResourceLoadException("Resource location is undefined!");
        }

        URI locationURI = URI.create(location);

        if (baseLocation == null && !locationURI.isAbsolute()) {
            throw new ResourceLoadException("Base HREF is empty and resource location is not absolute!");
        }

        if (baseLocation != null) {
            baseLocation = baseLocation.replace("\\", "/");
            baseLocation = baseLocation.replace("file://", "file:/");
        }
        URI baseLocationURI = (baseLocation != null) ? URI.create(baseLocation) : null;

        if (!locationURI.isAbsolute() && baseLocation != null) {
            locationURI = baseLocationURI.resolve(locationURI.normalize());
        }

        if (!locationURI.isAbsolute()) {
            throw new ResourceLoadException("Resource location is not absolute!");
        }

        return locationURI;
    }

    private class RedirectStrategy extends DefaultRedirectStrategy {
        @Override
        public boolean isRedirected(
                final HttpRequest request,
                final HttpResponse response,
                final HttpContext context) throws ProtocolException {
            if (request == null) {
                throw new IllegalArgumentException("HTTP request may not be null");
            }
            if (response == null) {
                throw new IllegalArgumentException("HTTP response may not be null");
            }

            int statusCode = response.getStatusLine().getStatusCode();
            String method = request.getRequestLine().getMethod();
            Header locationHeader = response.getFirstHeader("location");
            if (301 <= statusCode && statusCode <= 399) {
                return true;
            } else {
                return false;
            }
        }
    }

}

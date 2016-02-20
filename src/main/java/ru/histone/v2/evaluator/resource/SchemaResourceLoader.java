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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.histone.v2.evaluator.resource.loader.Loader;
import ru.histone.v2.exceptions.ResourceLoadException;
import ru.histone.v2.utils.PathUtils;

import java.net.URI;
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
    public CompletableFuture<Resource> load(String href, String baseHref, Map<String, Object> args) throws ResourceLoadException {
        log.debug("Trying to load resource from location={}, with baseLocation={}", new Object[]{href, baseHref});

        return CompletableFuture.completedFuture(null)
                .thenCompose(x -> {
                    String fullLocation = PathUtils.resolveUrl(href, baseHref);
                    ru.histone.evaluator.functions.global.URI uri = PathUtils.parseURI(fullLocation);

                    if (baseHref == null && uri.getScheme() == null) {
                        throw new ResourceLoadException("Base HREF is empty and resource location is not absolute!");
                    }

                    URI loadUri;
                    try {
                        if (!DATA_SCHEME.equals(uri.getScheme())) {
                            loadUri = makeFullLocation(href, baseHref);
                        } else {
                            loadUri = makeFullLocation(href, "");
                        }
                    } catch (IllegalAccessException e) {
                        throw new ResourceLoadException(e.getMessage(), e);
                    }

                    Loader loader = loaders.get(uri.getScheme());
                    if (loader != null) {
                        return loader.loadResource(loadUri, args);
                    }

                    throw new ResourceLoadException(String.format("Unsupported scheme for resource loading: '%s'", uri.getScheme()));
                });
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
//            baseLocation = baseLocation.replace("file://", "file:/");
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

//    private class RedirectStrategy extends DefaultRedirectStrategy {
//        @Override
//        public boolean isRedirected(
//                final HttpRequest request,
//                final HttpResponse response,
//                final HttpContext context) throws ProtocolException {
//            if (request == null) {
//                throw new IllegalArgumentException("HTTP request may not be null");
//            }
//            if (response == null) {
//                throw new IllegalArgumentException("HTTP response may not be null");
//            }
//
//            int statusCode = response.getStatusLine().getStatusCode();
//            String method = request.getRequestLine().getMethod();
//            Header locationHeader = response.getFirstHeader("location");
//            if (301 <= statusCode && statusCode <= 399) {
//                return true;
//            } else {
//                return false;
//            }
//        }
//    }

}

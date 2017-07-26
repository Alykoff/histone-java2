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
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.resource.loader.Loader;
import ru.histone.v2.exceptions.ResourceLoadException;
import ru.histone.v2.utils.AsyncUtils;
import ru.histone.v2.utils.PathUtils;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static ru.histone.v2.evaluator.resource.loader.DataLoader.DATA_SCHEME;

/**
 * @author Alexey Nevinsky
 */
public class SchemaResourceLoader implements HistoneResourceLoader {

    private static final Logger log = LoggerFactory.getLogger(SchemaResourceLoader.class);
    protected final Map<String, Loader> loaders;

    public SchemaResourceLoader() {
        loaders = new HashMap<>();
    }

    public void addLoader(String scheme, Loader loader) {
        loaders.put(scheme, loader);
    }

    public void addLoader(Loader loader) {
        loaders.put(loader.getScheme(), loader);
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
    public CompletableFuture<Resource> load(Context ctx, String href, String baseHref, Map<String, Object> args)
            throws ResourceLoadException {
        log.debug("Trying to load resource from location={}, with baseLocation={}", new Object[]{href, baseHref});

        return AsyncUtils.initFuture().thenCompose(ignore -> {
            String fullLocation = PathUtils.resolveUrl(href, baseHref);
            ru.histone.v2.utils.URI uri = PathUtils.parseURI(fullLocation);

            if (baseHref == null && uri.getScheme() == null) {
                throw new ResourceLoadException("Base HREF is empty and resource location is not absolute!");
            }

            URI loadUri;
            try {
                if (uri.getScheme() == null) {
                    loadUri = URI.create("file://" + fullLocation);
                    uri.setScheme("file");
                } else {
                    switch (uri.getScheme()) {
                        case DATA_SCHEME:
                            loadUri = makeFullLocation(href, "");
                            break;
                        default:
                            loadUri = makeFullLocation(href, baseHref);
                            break;
                    }
                }
            } catch (IllegalAccessException e) {
                throw new ResourceLoadException(e.getMessage(), e);
            }

            Loader loader = loaders.get(uri.getScheme());
            if (loader != null) {
                return loader.loadResource(ctx, loadUri, args);
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
        }
        URI baseLocationURI = (baseLocation != null) ? URI.create(baseLocation) : null;

        if (!locationURI.isAbsolute() && baseLocation != null) {
            locationURI = URI.create(PathUtils.resolveUrl(locationURI.toString(), baseLocationURI.toString()));
        }

        if (!locationURI.isAbsolute()) {
            throw new ResourceLoadException("Resource location is not absolute!");
        }

        return locationURI;
    }

}

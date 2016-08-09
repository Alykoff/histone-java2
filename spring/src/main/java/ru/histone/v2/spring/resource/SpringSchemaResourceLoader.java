package ru.histone.v2.spring.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.histone.v2.evaluator.resource.Resource;
import ru.histone.v2.evaluator.resource.SchemaResourceLoader;
import ru.histone.v2.evaluator.resource.loader.Loader;
import ru.histone.v2.exceptions.ResourceLoadException;
import ru.histone.v2.utils.AsyncUtils;
import ru.histone.v2.utils.PathUtils;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Aleksander Melnichnikov
 */
public class SpringSchemaResourceLoader extends SchemaResourceLoader {

    public static final String DEFAULT_LOADER = "default";

    private static final Logger log = LoggerFactory.getLogger(SpringSchemaResourceLoader.class);

    public SpringSchemaResourceLoader() {
        super();
    }

    @Override
    public CompletableFuture<Resource> load(String href, String baseHref, Map<String, Object> args) throws ResourceLoadException {
        log.debug("Trying to load resource from location={}, with baseLocation={}", new Object[]{href, baseHref});

        return AsyncUtils.initFuture().thenCompose(ignore -> {
            String fullLocation = PathUtils.resolveUrl(href, baseHref);
            ru.histone.v2.utils.URI uri = PathUtils.parseURI(fullLocation);

            if (baseHref == null && uri.getScheme() == null) {
                throw new ResourceLoadException("Base HREF is empty and resource location is not absolute!");
            }
            URI loadUri;
            Loader loader;
            if (uri.getScheme() != null) {
                try {
                    switch (uri.getScheme()) {
                        case DATA_SCHEME:
                            loadUri = makeFullLocation(href, "");
                            break;
                        default:
                            loadUri = makeFullLocation(href, baseHref);
                            break;
                    }
                    loader = loaders.get(uri.getScheme());
                } catch (IllegalAccessException e) {
                    throw new ResourceLoadException(e.getMessage(), e);
                }
            } else {
                loadUri = URI.create(baseHref).resolve(href);
                loader = loaders.get(DEFAULT_LOADER);
            }
            if (loader != null) {
                return loader.loadResource(loadUri, args);
            }

            throw new ResourceLoadException(String.format("Unsupported scheme for resource loading: '%s'", uri.getScheme()));
        });
    }


}

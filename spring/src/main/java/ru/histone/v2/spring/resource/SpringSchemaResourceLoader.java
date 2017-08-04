package ru.histone.v2.spring.resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.resource.Resource;
import ru.histone.v2.evaluator.resource.SchemaResourceLoader;
import ru.histone.v2.evaluator.resource.loader.Loader;
import ru.histone.v2.exceptions.ResourceLoadException;
import ru.histone.v2.utils.AsyncUtils;
import ru.histone.v2.utils.PathUtils;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static ru.histone.v2.evaluator.resource.loader.DataLoader.DATA_SCHEME;
import static ru.histone.v2.spring.resource.loader.ServletContextLoader.SERVLET_CONTEXT_SCHEME;

/**
 * @author Aleksander Melnichnikov
 */
public class SpringSchemaResourceLoader extends SchemaResourceLoader {

    private static final Logger log = LoggerFactory.getLogger(SpringSchemaResourceLoader.class);

    public SpringSchemaResourceLoader() {
        super();
    }

    @Override
    public CompletableFuture<Resource> load(Context ctx, String href, String baseHref, Map<String, Object> args)
            throws ResourceLoadException {
        log.debug("Trying to load resource from location={}, with baseLocation={}", new Object[]{href, baseHref});

        return AsyncUtils.initFuture().thenCompose(ignore -> {
            if (href != null) {
                if (CollectionUtils.isEmpty(loaders.entrySet())) {
                    throw new ResourceLoadException("No loaders find, register loader");
                }
                String fullLocation = PathUtils.resolveUrl(href, baseHref != null ? baseHref : StringUtils.EMPTY);
                ru.histone.v2.utils.URI uri = PathUtils.parseURI(fullLocation);

                try {
                    URI loadUri;
                    String scheme = uri.getScheme() == null ? SERVLET_CONTEXT_SCHEME : uri.getScheme();
                    switch (scheme) {
                        case SERVLET_CONTEXT_SCHEME:
                            loadUri = baseHref != null ? URI.create(baseHref).resolve(href) : URI.create(href);
                            break;
                        case DATA_SCHEME:
                            loadUri = makeFullLocation(href, "");
                            break;
                        default:
                            loadUri = makeFullLocation(href, baseHref);
                            break;
                    }
                    Loader loader = loaders.get(scheme);
                    if (loader != null) {
                        return loader.loadResource(ctx, loadUri, args);
                    }
                } catch (IllegalAccessException e) {
                    throw new ResourceLoadException(e.getMessage(), e);
                }
                throw new ResourceLoadException(String.format("Unsupported scheme for resource loading: '%s'", uri.getScheme()));
            }
            throw new ResourceLoadException("HREF is null. Unsupported value");
        });
    }


}

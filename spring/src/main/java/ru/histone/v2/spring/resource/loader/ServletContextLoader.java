package ru.histone.v2.spring.resource.loader;

import org.apache.commons.io.IOUtils;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.resource.ContentType;
import ru.histone.v2.evaluator.resource.HistoneStringResource;
import ru.histone.v2.evaluator.resource.Resource;
import ru.histone.v2.evaluator.resource.loader.Loader;
import ru.histone.v2.exceptions.ResourceLoadException;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Aleksander Melnichnikov
 */
public class ServletContextLoader implements Loader {

    public static final String SERVLET_CONTEXT_SCHEME = "servletContext";
    private static final Pattern SPLIT_SLASH_PATTERN = Pattern.compile("^/*(.*)");
    private ServletContext servletContext;

    public ServletContextLoader(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public CompletableFuture<Resource> loadResource(Context ctx, URI url, Map<String, Object> params) {
        Matcher matcher = SPLIT_SLASH_PATTERN.matcher(url.getSchemeSpecificPart());
        if (matcher.find() && url.getScheme() == null) {
            String location = matcher.group(1);
            try (InputStream resourceStream = getResourceStream(location)) {
                if (resourceStream == null) {
                    throw new ResourceLoadException(String.format("Error with ResourceInputStream for file '%s'", location));
                }
                String data = IOUtils.toString(resourceStream);
                return CompletableFuture.completedFuture(new HistoneStringResource(data, url.toString(), ContentType.TEXT.getId()));
            } catch (IOException e) {
                throw new ResourceLoadException(String.format("Error with ResourceInputStream for file '%s'", location));
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getScheme() {
        return SERVLET_CONTEXT_SCHEME;
    }

    protected InputStream getResourceStream(String location) {
        return servletContext.getResourceAsStream(location);
    }
}



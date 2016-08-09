package ru.histone.v2.spring.resource.loader;

import org.apache.commons.io.IOUtils;
import ru.histone.v2.evaluator.resource.ContentType;
import ru.histone.v2.evaluator.resource.HistoneStringResource;
import ru.histone.v2.evaluator.resource.Resource;
import ru.histone.v2.evaluator.resource.loader.Loader;
import ru.histone.v2.exceptions.ResourceLoadException;

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
public class ClassPathLoader implements Loader {

    protected static final Pattern SPLIT_SLASH_PATTERN = Pattern.compile("^/*(.*)");

    @Override
    public CompletableFuture<Resource> loadResource(URI url, Map<String, Object> params) {
        if (!url.toString().matches("classpath:/*(.*)")) {
            return CompletableFuture.completedFuture(null);
        }
        Matcher matcher = SPLIT_SLASH_PATTERN.matcher(url.getSchemeSpecificPart());
        if (matcher.find()) {
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

    protected InputStream getResourceStream(String location) {
        return ClassPathLoader.class.getClassLoader().getResourceAsStream(location);
    }
}

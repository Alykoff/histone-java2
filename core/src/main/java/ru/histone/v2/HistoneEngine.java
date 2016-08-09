package ru.histone.v2;

import ru.histone.v2.evaluator.resource.loader.Loader;

import java.io.IOException;
import java.util.Map;

/**
 * @author Aleksander Melnichnikov
 */
public interface HistoneEngine {

    String processTemplate(String templateBaseDir, String baseUri, Map<String, Object> params, String encoding) throws IOException;

    HistoneEngine addResourceLoader(String name, Loader loader);
}

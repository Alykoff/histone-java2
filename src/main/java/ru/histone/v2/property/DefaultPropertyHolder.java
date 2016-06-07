package ru.histone.v2.property;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Aleksander Melnichnikov
 *
 * Default implementation load histone version and server type
 */
public class DefaultPropertyHolder implements PropertyHolder<String> {

    private final Map<String, String> propertyMap = new LinkedHashMap<>();

    public DefaultPropertyHolder() {
        propertyMap.put("server", "java");
        Properties properties = new Properties();
        try (InputStream resourceStream =
                     Thread.currentThread().getContextClassLoader().getResourceAsStream("version.properties")) {
            properties.load(resourceStream);
            propertyMap.put("version",
                    properties.getProperty("histone.version"));
        } catch (IOException ignore) {
        }
    }

    @Override
    public String getProperty(String key) {
        return propertyMap.get(key);
    }

    @Override
    public Map<String, String> getPropertyMap() {
        return Collections.unmodifiableMap(propertyMap);
    }
}

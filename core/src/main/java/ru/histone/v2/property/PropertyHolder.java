package ru.histone.v2.property;

import java.util.Map;

/**
 * @author Aleksander Melnichnikov
 *         <p>
 *         Load properties for global objects.
 */
public interface PropertyHolder<T> {

    T getProperty(String key);

    Map<String, T> getPropertyMap();
}

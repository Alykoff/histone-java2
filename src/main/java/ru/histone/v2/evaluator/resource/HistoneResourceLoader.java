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

import ru.histone.v2.exceptions.ResourceLoadException;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Resource loader interface<br/>
 * Default Histone resource loader implements this interface and supports 'file://' and 'http://' protocols<br/>
 * If you want to implement your own custom resource loader, then implement this interface. If you want to extend default
 * resource loaer, then you should override DefaultResourceLoader class and make calls to super methods.
 *
 * @author alexey.nevinsky
 */
public interface HistoneResourceLoader {
    /**
     * Load resource using specified href, baseHref and arguments
     *
     * @param href     resource location
     * @param baseHref base href for loading
     * @param args     additional custom arguments for resource loader
     * @return resource object
     * @throws ResourceLoadException if errors occur
     */
    CompletableFuture<Resource> load(String href, String baseHref, Map<String, Object> args) throws ResourceLoadException;

    /**
     * Return full path for specified resource href and base href
     *
     * @param href     resource href
     * @param baseHref base href
     * @return full path to resource
     * @throws ResourceLoadException in case of any errors
     */
    String resolveFullPath(String href, String baseHref) throws ResourceLoadException;
}

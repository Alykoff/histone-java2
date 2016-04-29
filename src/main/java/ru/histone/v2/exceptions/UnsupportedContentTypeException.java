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
package ru.histone.v2.exceptions;

import org.apache.commons.lang3.StringUtils;

/**
 * Occurs, when ResourceLoader does not support particular contentType.
 *
 * @author sazonovkirill@gmail.com
 */
public class UnsupportedContentTypeException extends ResourceLoadException {
    private final String[] unsupportedContentTypes;
    private final Class resourceLoaderClass;

    public UnsupportedContentTypeException(String unsupportedContentType, Class resourceLoaderClass) {
        super("ResourceLoader " + resourceLoaderClass.getSimpleName() + " does not support content type '" + unsupportedContentType + "'");
        this.unsupportedContentTypes = new String[]{unsupportedContentType};
        this.resourceLoaderClass = resourceLoaderClass;
    }

    public UnsupportedContentTypeException(String[] unsupportedContentTypes, Class resourceLoaderClass) {
        super("ResourceLoader " + resourceLoaderClass.getSimpleName() + " supports none of these content types: " + StringUtils.join(unsupportedContentTypes, "/"));
        this.unsupportedContentTypes = unsupportedContentTypes;
        this.resourceLoaderClass = resourceLoaderClass;
    }

    public String[] getUnsupportedContentTypes() {
        return unsupportedContentTypes;
    }

    public Class getResourceLoaderClass() {
        return resourceLoaderClass;
    }
}

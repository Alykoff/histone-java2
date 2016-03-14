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
package ru.histone.v2.utils;

import ru.histone.v2.evaluator.resource.HistoneStreamResource;
import ru.histone.v2.evaluator.resource.HistoneStringResource;
import ru.histone.v2.evaluator.resource.Resource;
import ru.histone.v2.exceptions.ResourceLoadException;

import java.io.IOException;
import java.text.MessageFormat;

/**
 * @author alexey.nevinsky
 */
public class IOUtils {
    public static String readStringFromResource(Resource resource, String path) throws ResourceLoadException {
        try {
            if (resource == null) {
                throw new ResourceLoadException(String.format("Can't load resource by path: %s.", path));
            }

            String content;
            if (resource instanceof HistoneStringResource) {
                content = ((HistoneStringResource) resource).getContent();
            } else if (resource instanceof HistoneStreamResource) {
                content = org.apache.commons.io.IOUtils.toString(((HistoneStreamResource) resource).getContent());
            } else {
                throw new ResourceLoadException(MessageFormat.format("Unsupported resource class: {0}", resource.getClass()));
            }

            if (content == null) {
                throw new ResourceLoadException(MessageFormat.format("Can't load resource by path: {0}. Resource is unreadable.", path));
            }
            return content;
        } catch (IOException e) {
            throw new ResourceLoadException("Resource import failed! Resource reading error.", e);
        }
    }
}

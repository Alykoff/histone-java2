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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.resource.HistoneStreamResource;
import ru.histone.v2.evaluator.resource.HistoneStringResource;
import ru.histone.v2.evaluator.resource.Resource;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.exceptions.ResourceLoadException;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.LinkedHashMap;

/**
 * @author Alexey Nevinsky
 */
public class IOUtils {
    public static String readStringFromResource(Resource resource, String path) throws ResourceLoadException {
        return readStringFromResource(resource, path, "UTF-8");
    }

    public static String readStringFromResource(Resource resource, String path, String encoding) throws ResourceLoadException {
        try {
            if (resource == null) {
                throw new ResourceLoadException(String.format("Can't load resource by path: %s.", path));
            }

            String content;
            if (resource instanceof HistoneStringResource) {
                content = ((HistoneStringResource) resource).getContent();
            } else if (resource instanceof HistoneStreamResource) {
                content = org.apache.commons.io.IOUtils.toString(((HistoneStreamResource) resource).getContent(), encoding);
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

    public static BOMInputStream readBomStream(java.net.URI location, InputStream stream) {
        BOMInputStream bomStream;
        try {
            bomStream = new BOMInputStream(stream);
            if (bomStream.getBOM() != BOMInputStream.BOM.NONE) {
                bomStream.skipBOM();
            }
        } catch (IOException e) {
            throw new ResourceLoadException(String.format("Error with BOMInputStream for file '%s'", location.toString()));
        }
        return bomStream;
    }

    public static EvalNode convertToJson(EvalNode res) {
        String str = (String) res.getValue();
        Object json;
        if (StringUtils.isEmpty(str)) {
            return EvalUtils.createEvalNode(null);
        } else if (StringUtils.isNotEmpty(str)) {
            json = fromJSON(str);
        } else {
            json = new LinkedHashMap<String, EvalNode>();
        }

        return EvalUtils.constructFromObject(json);
    }

    public static Object fromJSON(String json) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.readValue(json, Object.class);
        } catch (IOException e) {
            throw new FunctionExecutionException(e.getMessage(), e);
        }
    }
}

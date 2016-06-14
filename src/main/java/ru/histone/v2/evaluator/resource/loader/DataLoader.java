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
package ru.histone.v2.evaluator.resource.loader;

import org.apache.commons.io.Charsets;
import ru.histone.v2.evaluator.resource.ContentType;
import ru.histone.v2.evaluator.resource.HistoneStreamResource;
import ru.histone.v2.evaluator.resource.HistoneStringResource;
import ru.histone.v2.evaluator.resource.Resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Alexey Nevinsky
 */
public class DataLoader implements Loader {
    @Override
    public CompletableFuture<Resource> loadResource(URI url, Map<String, Object> params) {
        if (!url.toString().matches("data:(.*);(.*),(.*)")) {
            return CompletableFuture.completedFuture(null);
        }
        String[] stringUri = url.getSchemeSpecificPart().split(",");

        Resource resource;
        if (stringUri.length > 1) {
            String toEncode = stringUri[1];
            if (stringUri[0].contains("base64")) {
                final InputStream stream = new ByteArrayInputStream(
                        org.apache.commons.codec.binary.Base64.decodeBase64(
                                toEncode.getBytes(Charsets.UTF_8)
                        )
                );
                resource = new HistoneStreamResource(stream, url.toString(), ContentType.TEXT.getId());
            } else {
                resource = new HistoneStringResource(toEncode, url.toString(), ContentType.TEXT.getId());
            }
        } else {
            resource = new HistoneStringResource("", url.toString(), ContentType.TEXT.getId());
        }

        return CompletableFuture.completedFuture(resource);
    }
}

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

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Alexey Nevinsky
 */
public class HistoneStreamResource implements Resource<InputStream> {
    private final InputStream stream;
    private final String baseHref;
    private final String contentType;

    public HistoneStreamResource(InputStream stream, String baseHref, String contentType) {
        this.stream = stream;
        this.baseHref = baseHref;
        this.contentType = contentType;
    }

    @Override
    public InputStream getContent() throws IOException {
        return stream;
    }

    @Override
    public String getBaseHref() {
        return baseHref;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(stream);
    }
}

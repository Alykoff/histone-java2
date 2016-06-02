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

import java.io.IOException;
import java.util.Map;

/**
 * @author Alexey Nevinsky
 */
public class HistoneStringResource implements Resource<String> {
    private final String baseHref;
    private final String contentType;
    private final String content;

    public HistoneStringResource(String content, String baseHref, String contentType) {
        this.baseHref = baseHref;
        this.contentType = contentType;
        this.content = content;
    }

    @Deprecated
    public HistoneStringResource(String baseHref, String contentType, String content, Map<String, Object> additionalParams) {
        this(baseHref, content, contentType);
    }

    @Override
    public String getContent() throws IOException {
        return content;
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
    }
}

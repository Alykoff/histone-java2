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

package ru.histone.v2.java_compiler.java_evaluator;

import ru.histone.v2.evaluator.resource.Resource;

import java.io.IOException;

/**
 * @author Alexey Nevinsky
 */
public class JavaHistoneClassResource implements Resource<Class> {

    private final Class templateClass;
    private final String baseHref;
    private final String contentType;

    public JavaHistoneClassResource(Class templateClass, String baseHref, String contentType) {
        this.templateClass = templateClass;
        this.baseHref = baseHref;
        this.contentType = contentType;
    }

    @Override
    public Class getContent() throws IOException {
        return templateClass;
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

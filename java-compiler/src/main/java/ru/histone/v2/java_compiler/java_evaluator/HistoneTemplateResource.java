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
import ru.histone.v2.java_compiler.bcompiler.data.Template;

import java.io.IOException;

/**
 * @author Alexey Nevinsky
 */
public class HistoneTemplateResource implements Resource<Template> {

    public static final String CONTENT_TYPE = "template";

    private final Template instance;
    private final String baseHref;

    public HistoneTemplateResource(Template instance, String baseHref) {
        this.instance = instance;
        this.baseHref = baseHref;
    }

    @Override
    public Template getContent() throws IOException {
        return instance;
    }

    @Override
    public String getBaseHref() {
        return baseHref;
    }

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    @Override
    public void close() throws IOException {
        //do nothing
    }
}

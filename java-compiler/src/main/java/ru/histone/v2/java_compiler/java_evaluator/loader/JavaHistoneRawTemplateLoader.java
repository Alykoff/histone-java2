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

package ru.histone.v2.java_compiler.java_evaluator.loader;

import ru.histone.v2.evaluator.resource.Resource;
import ru.histone.v2.evaluator.resource.loader.Loader;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.java_compiler.bcompiler.data.Template;
import ru.histone.v2.java_compiler.java_evaluator.HistoneClassRegistry;
import ru.histone.v2.java_compiler.java_evaluator.HistoneTemplateResource;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Alexey Nevinsky
 */
public class JavaHistoneRawTemplateLoader implements Loader {
    public static final String RAW_TPL_SCHEME = "rawTpl";
    public static final String PARAM_KEY = "rawTpl";

    protected HistoneClassRegistry classRegistry;

    public JavaHistoneRawTemplateLoader(HistoneClassRegistry classRegistry) {
        this.classRegistry = classRegistry;
    }

    @Override
    public CompletableFuture<Resource> loadResource(URI uri, Map<String, Object> params) {
        String className = uri.toString().replace(RAW_TPL_SCHEME + ":", "");
        String templatePath = "";
        String tpl = (String) params.get(PARAM_KEY);

        Template t = classRegistry.loadInstanceFromTpl(className, tpl);
        if (t == null) {
            throw new FunctionExecutionException("Failed to load template '" + className + "'");
        }

        HistoneTemplateResource resource = new HistoneTemplateResource(t, templatePath);
        return CompletableFuture.completedFuture(resource);
    }

    @Override
    public String getScheme() {
        return RAW_TPL_SCHEME;
    }
}

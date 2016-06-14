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

package ru.histone.v2;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.Evaluator;
import ru.histone.v2.evaluator.resource.SchemaResourceLoader;
import ru.histone.v2.parser.Parser;
import ru.histone.v2.parser.node.ExpAstNode;
import ru.histone.v2.property.DefaultPropertyHolder;
import ru.histone.v2.property.PropertyHolder;
import ru.histone.v2.rtti.RunTimeTypeInfo;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * @author Alexey Nevinsky
 */
public class Histone {

    private Evaluator evaluator;
    private RunTimeTypeInfo runTimeTypeInfo;
    private Parser parser;
    private SchemaResourceLoader resourceLoader;
    private Executor executor;
    private Locale locale;
    private PropertyHolder propertyHolder;

    public Histone() {
        evaluator = new Evaluator();
        parser = new Parser();
        executor = new ForkJoinPool();
        propertyHolder = new DefaultPropertyHolder();
        resourceLoader = new SchemaResourceLoader(executor);
        locale = Locale.getDefault();
        runTimeTypeInfo = new RunTimeTypeInfo(executor, resourceLoader, evaluator, parser);
    }

    public Histone(Locale locale) {
        this();
        this.locale = locale;
    }

    public String process(String template, String baseUri, Map<String, Object> params) {
        ExpAstNode tree = parser.process(template, baseUri);
        return evaluator.process(tree, createContext(baseUri, params));
    }

    public String process(String template) {
        return process(template, "", Collections.emptyMap());
    }

    private Context createContext(String baseUri, Map<String, Object> params) {
        Context ctx = Context.createRoot(baseUri, locale, runTimeTypeInfo, propertyHolder);
        ctx.put("this", CompletableFuture.completedFuture(EvalUtils.constructFromObject(params)));
        return ctx;
    }
}

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

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.Evaluator;
import ru.histone.v2.evaluator.resource.HistoneResourceLoader;
import ru.histone.v2.evaluator.resource.SchemaResourceLoader;
import ru.histone.v2.parser.Parser;
import ru.histone.v2.parser.node.ExpAstNode;
import ru.histone.v2.property.DefaultPropertyHolder;
import ru.histone.v2.rtti.RunTimeTypeInfo;

import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Example {

    public static void main(String[] args) {
        Evaluator evaluator = new Evaluator(); // for evaluating AST-tree
        Parser parser = new Parser(); // for parsing string template

        ExecutorService executorService = Executors.newFixedThreadPool(10); // base executor service

        HistoneResourceLoader loader = new SchemaResourceLoader(executorService); // base resource loader
        RunTimeTypeInfo runTimeTypeInfo = new RunTimeTypeInfo(executorService, loader, evaluator, parser); // singleton with predefined functions

        String baseUri = "http://localhost/";
        ExpAstNode node = parser.process("{{var x = 'Hello world!!'}}{{x}}", baseUri); // parsing template and create AST-tree

        Context ctx = Context.createRoot(baseUri, runTimeTypeInfo, new DefaultPropertyHolder()); // context for evaluating
        String res = evaluator.process(node, ctx); // evaluate AST-tree
        System.out.println(res);
    }
}
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
package ru.histone.v2.evaluator.function.global;

import ru.histone.v2.evaluator.Converter;
import ru.histone.v2.evaluator.Evaluator;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.resource.HistoneResourceLoader;
import ru.histone.v2.parser.Parser;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

/**
 * Async wrapper for loadJSON function
 *
 * @author Alexey Nevinsky
 */
public class AsyncLoadJson extends LoadJson {
    public AsyncLoadJson(Executor executor, HistoneResourceLoader loader, Evaluator evaluator, Parser parser,
                         Converter converter, ConcurrentMap<String, CompletableFuture<EvalNode>> cache) {
        super(executor, loader, evaluator, parser, converter, cache);
    }

    @Override
    public String getName() {
        return "asyncLoadJSON";
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}

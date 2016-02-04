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

package ru.histone.v2.evaluator.function.any;

import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.evaluator.node.StringEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.AsyncUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Created by inv3r on 27/01/16.
 */
public class ToString extends AbstractFunction {
    @Override
    public String getName() {
        return "toString";
    }

    @Override
    public CompletableFuture<EvalNode> execute(String baseUri, Locale locale, List<EvalNode> args) throws FunctionExecutionException {
        return executeHelper(baseUri, locale, args)
                .thenCompose(EvalUtils::getValue);
    }

    private CompletableFuture<String> executeHelper(String baseUri, Locale locale, List<EvalNode> args) throws FunctionExecutionException {
        final EvalNode node = args.get(0);
        final HistoneType nodeType = node.getType();
        switch (nodeType) {
            case T_UNDEFINED: {
                return CompletableFuture.completedFuture("");
            }
            case T_ARRAY: {
                final Map<String, EvalNode> map = ((MapEvalNode) node).getValue();
                return recurseFlattening(baseUri, locale, map);
            }
            case T_NULL: {
                return CompletableFuture.completedFuture("null");
            }
            default: {
                return CompletableFuture.completedFuture(node.getValue() + "");
            }
        }
    }

    private CompletableFuture<String> recurseFlattening(String baseUri, Locale locale, Map<String, EvalNode> map) {
        final List<CompletableFuture<String>> valuesRawListFuture = new ArrayList<>();
        for (EvalNode rawValue : map.values()) {
            if (rawValue != null) {
                if (rawValue instanceof Map) {
                    final Map<String, EvalNode> value = (Map<String, EvalNode>) rawValue;
                    valuesRawListFuture.add(recurseFlattening(baseUri, locale, value));
                } else {
                    final CompletableFuture<EvalNode> executedValue = execute(
                            baseUri, locale, Collections.singletonList(rawValue)
                    );
                    final CompletableFuture<String> value = executedValue
                            .thenApply(x -> ((StringEvalNode)x).getValue());
                    valuesRawListFuture.add(value);
                }
            }
        }
        final CompletableFuture<List<String>> valuesListFuture = AsyncUtils.sequence(valuesRawListFuture);
        return valuesListFuture.thenApply(x ->
                x.stream().collect(Collectors.joining(" "))
        );
    }
}

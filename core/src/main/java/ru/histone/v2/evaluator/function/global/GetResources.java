/*
 * Copyright (c) 2017 MegaFon
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

import org.apache.commons.collections.CollectionUtils;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.Converter;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.utils.AsyncUtils;
import ru.histone.v2.utils.RttiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Accepts the input 1 argument - the type of resource required. When called, the backend does the following:
 * 1. We pass through the array RESOURCES and filter it, depending on the type of resource required.
 * 2. Issue the result - a massive list of resources of the required type, or an empty array, if no such resources were found.
 * It is important that the RESOURCE is cleared every time the page is requested.
 *
 * @author Alexey Nevinsky
 * @since 16-06-2017
 */
public class GetResources extends AbstractFunction {
    public GetResources(Converter converter) {
        super(converter);
    }

    @Override
    public String getName() {
        return "getResources";
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        List<EvalNode> arguments = clearGlobal(args);

        Object arg = getValue(arguments, 0);
        if (arg == null) {
            return converter.getValue(null);
        }

        CompletableFuture<EvalNode> res = AsyncUtils
                .sequence((List<CompletableFuture<EvalNode>>) context.getTemplateVars().get("resources"))
                .thenApply(nodes -> converter.constructFromList(
                        nodes.stream()
                             .filter(n -> ((String) n.getValue()).endsWith(arg + ""))
                             .collect(Collectors.toList())
                ));

        context
                .getTemplateVars()
                .computeIfPresent("resources",
                                  (k, list) -> ((List<CompletableFuture<EvalNode>>) list)
                                          .stream()
                                          .filter(f -> !((String) f.join().getValue()).endsWith(arg + ""))
                                          .collect(Collectors.toList())
                );

        return res;
    }
}

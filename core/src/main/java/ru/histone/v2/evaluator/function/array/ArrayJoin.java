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

package ru.histone.v2.evaluator.function.array;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.evaluator.node.StringEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.utils.AsyncUtils;
import ru.histone.v2.utils.RttiUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Gali Alykoff
 */
public class ArrayJoin extends AbstractFunction implements Serializable {
    public static final String DEFAULT_DELIMITER = "";
    public static final CompletableFuture<String> DEFAULT_DELIMITER_FUTURE = CompletableFuture.completedFuture(DEFAULT_DELIMITER);
    public static final String NAME = "join";
    private static final int MAP_EVAL_INDEX = 0;
    private static final int ARGS_START_INDEX = 1;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        final MapEvalNode mapEvalNode = (MapEvalNode) args.get(MAP_EVAL_INDEX);
        final Optional<EvalNode> separatorOptionalNode = Optional.ofNullable(args.size() > ARGS_START_INDEX
                        ? args.get(ARGS_START_INDEX)
                        : null
        );
        final CompletableFuture<String> separatorFuture = separatorOptionalNode.map(separatorNode ->
                        RttiUtils.callToString(context, separatorNode)
                                .thenApply(x -> ((StringEvalNode) x).getValue())
        ).orElse(DEFAULT_DELIMITER_FUTURE);
        final CompletableFuture<List<EvalNode>> valueNodesFuture = separatorFuture.thenCompose(separator -> {
            final List<CompletableFuture<EvalNode>> nodes = mapEvalNode.getValue()
                    .values()
                    .stream()
                    .map(innerValue ->
                                    RttiUtils.callToString(context, innerValue)
                    ).collect(Collectors.toList());
            return AsyncUtils.sequence(nodes);
        });
        return separatorFuture.thenCompose(separator -> valueNodesFuture.thenApply(valueNodes -> {
            final String values = valueNodes.stream()
                    .map(stringValue -> ((StringEvalNode) stringValue).getValue())
                    .collect(Collectors.joining(separator));
            return new StringEvalNode(values);
        }));
    }
}

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
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.BooleanEvalNode;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MacroEvalNode;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.AsyncUtils;
import ru.histone.v2.utils.Tuple;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


/**
 * @author Gali Alykoff
 */
public class ArrayFilter extends AbstractFunction implements Serializable {
    public static final String NAME = "filter";
    public static final int MAP_EVAL_INDEX = 0;
    public static final int MACRO_INDEX = 1;
    public static final int ARGS_START_INDEX = 2;

    public static CompletableFuture<List<Tuple<EvalNode, Boolean>>> calcByPredicate(Context context, List<EvalNode> args) {
        final MapEvalNode mapEvalNode = (MapEvalNode) args.get(MAP_EVAL_INDEX);
        final EvalNode valueNode = args.get(MACRO_INDEX);
        final EvalNode param = args.size() > ARGS_START_INDEX ? args.get(ARGS_START_INDEX) : null;

        final List<CompletableFuture<Tuple<EvalNode, Boolean>>> mapResultWithPredicate = mapEvalNode.getValue()
                .values().stream()
                .map(arg -> {
                    if (valueNode.getType() == HistoneType.T_MACRO) {
                        final MacroEvalNode macro = (MacroEvalNode) valueNode;

                        final List<EvalNode> arguments = new ArrayList<>(Collections.singletonList(macro));
                        arguments.add(new BooleanEvalNode(false));
                        if (param != null) {
                            arguments.add(param);
                        }
                        arguments.add(arg);
                        final CompletableFuture<EvalNode> predicateFuture = context.macroCall(arguments);
                        return predicateFuture.thenApply(predicateNode -> {
                            final Boolean predicate = EvalUtils.nodeAsBoolean(predicateNode);
                            return Tuple.create(arg, predicate);
                        });
                    } else {
                        final Boolean predicate = EvalUtils.nodeAsBoolean(valueNode);
                        return CompletableFuture.completedFuture(Tuple.create(arg, predicate));
                    }
                }).collect(Collectors.toList());
        return AsyncUtils.sequence(mapResultWithPredicate);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        return calcByPredicate(context, args).thenApply(pairs ->
                pairs.stream()
                        .filter(Tuple::getRight)
                        .map(Tuple::getLeft)
                        .collect(Collectors.toList())
        ).thenApply(MapEvalNode::new);
    }
}

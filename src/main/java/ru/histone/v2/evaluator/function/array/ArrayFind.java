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
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MacroEvalNode;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.RttiUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author gali.alykoff on 11/02/16.
 */
public class ArrayFind extends AbstractFunction implements Serializable {
    public static final String NAME = "find";
    public static final int ARRAY_INDEX = 0;
    public static final int BOOLEAN_FLAG_INDEX = 2;
    public static final int START_BIND_INDEX = 3;
    private static final int MACRO_INDEX = 1;

    public static CompletableFuture<EvalNode> find(
            Context context,
            MacroEvalNode macro,
            MapEvalNode values,
            List<Map.Entry<String, EvalNode>> nodes,
            boolean keyAsResult
    ) {
        if (nodes.size() == 0) {
            return EvalUtils.getValue(null);
        }
        final Map.Entry<String, EvalNode> first = nodes.get(0);
        return RttiUtils.callMacro(context, macro, first.getValue(), EvalUtils.createEvalNode(first.getKey()), values)
                .thenCompose(resultMacro -> RttiUtils.callToBooleanResult(context, resultMacro))
                .thenCompose(predicate -> {
                            if (predicate) {
                                return CompletableFuture.completedFuture(getResultForPredicate(first, keyAsResult));
                            }
                            return find(context, macro, values, nodes.subList(1, nodes.size()), keyAsResult);
                        }
                );
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        // [ARRAY, MACRO, VALUE_OR_KEY_FLAG, MACRO_BINDINGS...]
        final MapEvalNode arrayNode = (MapEvalNode) args.get(ARRAY_INDEX);
        final List<Map.Entry<String, EvalNode>> values = arrayNode
                .getValue()
                .entrySet()
                .stream()
                .collect(Collectors.toList());

        if (args.size() <= MACRO_INDEX || values.size() == 0) {
            return EvalUtils.getValue(null);
        }

        final EvalNode rawMacroNode = args.get(MACRO_INDEX);

        final boolean keyAsResult;
        if (args.size() > 2) {
            keyAsResult = RttiUtils.callToBooleanResult(context, args.get(BOOLEAN_FLAG_INDEX)).join();
        } else {
            keyAsResult = false;
        }

        if (rawMacroNode.getType() != HistoneType.T_MACRO) {
            return RttiUtils.callToBooleanResult(context, rawMacroNode)
                    .thenApply(predicate -> {
                                if (predicate) {
                                    return getResultForPredicate(values.get(0), keyAsResult);
                                }
                                return EvalUtils.createEvalNode(null);
                            }
                    );
        }
        final CompletableFuture<MacroEvalNode> macroFuture = ArrayReduce.getMacroWithBindFuture(context, args, START_BIND_INDEX);
        return macroFuture
                .thenCompose(macro -> find(context, macro, arrayNode, values, keyAsResult));
    }

    private static EvalNode getResultForPredicate(Map.Entry<String, EvalNode> entry, boolean keyAsResult) {
        if (keyAsResult) {
            return EvalUtils.createEvalNode(entry.getKey());
        }
        return entry.getValue();
    }
}

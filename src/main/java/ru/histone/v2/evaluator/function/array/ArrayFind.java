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

import org.apache.commons.lang.ObjectUtils;
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
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author gali.alykoff on 11/02/16.
 */
public class ArrayFind extends AbstractFunction implements Serializable {
    public static final String NAME = "find";
    public static final int ARRAY_INDEX = 0;
    public static final int START_BIND_INDEX = 2;
    private static final int MACRO_INDEX = 1;

    public static CompletableFuture<EvalNode> find(
            Context context,
            MacroEvalNode macro,
            List<EvalNode> nodes
    ) {
        if (nodes.size() == 0) {
            return EvalUtils.getValue(ObjectUtils.NULL);
        }
        final EvalNode first = nodes.get(0);
        return RttiUtils.callMacro(context, macro, first).thenCompose(resultMacro ->
                RttiUtils.callToBooleanResult(context, resultMacro)
        ).thenCompose(predicate ->
                predicate
                        ? CompletableFuture.completedFuture(first)
                        : find(context, macro, nodes.subList(1, nodes.size()))
        );
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        // [ARRAY, MACRO, MACRO_BINDINGS...]
        final MapEvalNode arrayNode = (MapEvalNode) args.get(ARRAY_INDEX);
        final List<EvalNode> values = arrayNode
                .getValue()
                .values()
                .stream()
                .collect(Collectors.toList());
        if (args.size() <= MACRO_INDEX || values.size() == 0) {
            return EvalUtils.getValue(ObjectUtils.NULL);
        }

        final EvalNode rawMacroNode = args.get(MACRO_INDEX);
        if (rawMacroNode.getType() != HistoneType.T_MACRO) {
            return RttiUtils.callToBooleanResult(context, rawMacroNode)
                    .thenApply(predicate ->
                            predicate ? values.get(0) : EvalUtils.createEvalNode(ObjectUtils.NULL)
                    );
        }
        final CompletableFuture<MacroEvalNode> macroFuture =
                ArrayReduce.getMacroWithBindFuture(context, args, START_BIND_INDEX);
        return macroFuture.thenCompose(macro -> find(context, macro, values));
    }
}

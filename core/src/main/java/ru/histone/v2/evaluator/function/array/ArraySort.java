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
import ru.histone.v2.evaluator.global.StringEvalNodeStrongComparator;
import ru.histone.v2.evaluator.node.*;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.AsyncUtils;
import ru.histone.v2.utils.RttiUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.util.Map.Entry;

/**
 * @author Gali Alykoff
 */
public class ArraySort extends AbstractFunction implements Serializable {
    public static final String NAME = "sort";
    public static final StringEvalNodeStrongComparator STRING_EVAL_NODE_STRONG_COMPARATOR = new StringEvalNodeStrongComparator();
    public static final int START_BIND_INDEX = 2;

    public static CompletableFuture<List<Entry<String, EvalNode>>> sort(
            List<CompletableFuture<Entry<String, EvalNode>>> nodes,
            MacroEvalNode macroNode,
            Context context
    ) {
        int size = nodes.size();
        if (size == 0 || size == 1) {
            return AsyncUtils.sequence(nodes);
        }
        int mid = size / 2;
        List<CompletableFuture<Entry<String, EvalNode>>> left = nodes.subList(0, mid);
        List<CompletableFuture<Entry<String, EvalNode>>> right = nodes.subList(mid, size);

        return sort(left, macroNode, context).thenCompose(leftSort ->
                        sort(right, macroNode, context).thenCompose(rightSort ->
                                        merge(leftSort, rightSort, macroNode, context)
                        )
        );
    }

    public static CompletableFuture<List<Entry<String, EvalNode>>> merge(
            List<Entry<String, EvalNode>> left,
            List<Entry<String, EvalNode>> right,
            MacroEvalNode macroNode,
            Context context
    ) {
        final LinkedList<Entry<String, EvalNode>> acc = new LinkedList<>();
        return mergeHelper(left, right, macroNode, context, acc)
                .thenApply(x -> (List<Entry<String, EvalNode>>) x);
    }

    public static CompletableFuture<LinkedList<Entry<String, EvalNode>>> mergeHelper(
            List<Entry<String, EvalNode>> left,
            List<Entry<String, EvalNode>> right,
            MacroEvalNode macroNode,
            Context context,
            LinkedList<Entry<String, EvalNode>> a
    ) {
        if (left.isEmpty() || right.isEmpty()) {
            for (Entry<String, EvalNode> lValue : left) {
                a.addLast(lValue);
            }
            for (Entry<String, EvalNode> rValue : right) {
                a.addLast(rValue);
            }
            return CompletableFuture.completedFuture(a);
        }
        Entry<String, EvalNode> valueL = left.get(0);
        Entry<String, EvalNode> valueR = right.get(0);

        return RttiUtils.callMacro(
                context,
                macroNode, new BooleanEvalNode(false),
                valueL.getValue(),
                valueR.getValue(),
                new StringEvalNode(valueL.getKey()),
                new StringEvalNode(valueR.getKey())
        ).thenCompose(macroReturn ->
                        RttiUtils.callToBoolean(context, macroReturn)
        ).thenCompose(booleanNodeResult -> {
//            boolean isNotReturned = !booleanNodeResult.isReturn();
            boolean isRightLessThenLeft = ((BooleanEvalNode) booleanNodeResult).getValue();
            if (isRightLessThenLeft) {
                List<Entry<String, EvalNode>> newR = right.subList(1, right.size());
                a.addLast(valueR);
                return mergeHelper(left, newR, macroNode, context, a);
            } else {
                List<Entry<String, EvalNode>> newL = left.subList(1, left.size());
                a.addLast(valueL);
                return mergeHelper(newL, right, macroNode, context, a);
            }
        });
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        // [LIST, MACRO_SORT, MACRO_BINDINGS...]
        // MACRO_SORT is function:
        // Function(
        //  left_value:node, right_value:node, left_key:string_node, right_key:string_node
        // )->boolean_node
        final MapEvalNode array = (MapEvalNode) args.get(0);
        final Map<String, EvalNode> values = array.getValue();
        final int argsSize = args.size();
        final boolean isMacroNotPresent = argsSize == 1
                || (argsSize > 1 && args.get(1).getType() != HistoneType.T_MACRO);
        if (isMacroNotPresent) {
            final List<EvalNode> vls = new ArrayList<>(values.values());
            final List<CompletableFuture<EvalNode>> valuesStringFutures = vls.stream()
                    .map(value -> RttiUtils.callToString(context, value))
                    .collect(Collectors.toList());
            return AsyncUtils.sequence(valuesStringFutures).thenApply(unsortedResult ->
                            unsortedResult.stream()
                                    .map(x -> (StringEvalNode) x)
                                    .sorted(STRING_EVAL_NODE_STRONG_COMPARATOR)
                                    .map(x -> (EvalNode) x)
                                    .collect(Collectors.toList())
            ).thenApply(MapEvalNode::new);
        }
        final List<CompletableFuture<Entry<String, EvalNode>>> nodes =
                values.entrySet()
                        .stream()
                        .map(CompletableFuture::completedFuture)
                        .collect(Collectors.toList());

        final CompletableFuture<MacroEvalNode> macroNodeFuture = ArrayReduce.getMacroWithBindFuture(context, args, START_BIND_INDEX);
        return macroNodeFuture.thenCompose(macroNode ->
                        sort(nodes, macroNode, context)
        ).thenApply(sortedPairs ->
                new MapEvalNode(sortedPairs.stream()
                        .map(Entry::getValue)
                        .collect(Collectors.toList())
                ));
    }
}

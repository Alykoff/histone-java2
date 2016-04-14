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
import ru.histone.v2.evaluator.node.StringEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.ParserUtils;
import ru.histone.v2.utils.RttiUtils;
import ru.histone.v2.utils.Tuple;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author gali.alykoff on 11/02/16.
 */
public class ArrayGroup extends AbstractFunction implements Serializable {
    public static final String NAME = "group";
    public static final int ARRAY_INDEX = 0;
    public static final int START_BIND_INDEX = 2;
    public static final Comparator<Tuple<Integer, EvalNode>> HISTONE_ARRAY_GROUP_COMPARATOR = (x, y) -> {
        final Integer xKey = x.getLeft();
        final Integer yKey = y.getLeft();
        return xKey.compareTo(yKey);
    };
    private static final int MACRO_INDEX = 1;

    private static CompletableFuture<Map<String, List<EvalNode>>> groupBy(
            Context context,
            MacroEvalNode macro,
            List<Map.Entry<String, EvalNode>> nodes,
            Map<String, List<EvalNode>> accNumber
    ) {
        if (nodes.isEmpty()) {
            return CompletableFuture.completedFuture(accNumber);
        }
        final Map.Entry<String, EvalNode> first = nodes.get(0);
        final EvalNode firstValue = first.getValue();
        final EvalNode firstKey = new StringEvalNode(first.getKey());
        return RttiUtils.callMacro(context, macro, firstValue, firstKey).thenCompose(macroResult ->
                RttiUtils.callToStringResult(context, macroResult).thenCompose(group -> {
                    final List<EvalNode> listOfGroup = accNumber.get(group);
                    if (listOfGroup == null) {
                        List<EvalNode> list = new ArrayList<>();
                        list.add(firstValue);
                        accNumber.put(group, list);
                    } else {
                        listOfGroup.add(firstValue);
                    }
                    return groupBy(
                            context, macro, nodes.subList(1, nodes.size()), accNumber
                    );
                }));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        // [ARRAY, MACRO, MACRO_BINDINGS...]
        final MapEvalNode arrayNode = (MapEvalNode) args.get(ARRAY_INDEX);
        final List<Map.Entry<String, EvalNode>> values = arrayNode
                .getValue()
                .entrySet()
                .stream()
                .collect(Collectors.toList());
        if (args.size() <= MACRO_INDEX || values.size() == 0) {
            return CompletableFuture.completedFuture(new MapEvalNode(Collections.emptyList()));
        }

        final EvalNode rawMacroNode = args.get(MACRO_INDEX);
        if (rawMacroNode.getType() != HistoneType.T_MACRO) {
            String stringKey = RttiUtils.callToStringResult(context, rawMacroNode).join();
            Map<String, EvalNode> resMap = new HashMap<>();
            resMap.put(stringKey, arrayNode);
            return EvalUtils.getValue(resMap);
        }
        final CompletableFuture<MacroEvalNode> macroFuture =
                ArrayReduce.getMacroWithBindFuture(context, args, START_BIND_INDEX);
        return macroFuture
                .thenCompose(macro -> groupBy(context, macro, values, new LinkedHashMap<>()))
                .thenApply(acc -> {
                    final Map<String, EvalNode> res = new LinkedHashMap<>();
                    final List<Tuple<String, EvalNode>> entrySimpleKeys = new ArrayList<>();
                    final Set<Tuple<Integer, EvalNode>> entryWithPositiveNumKeys = new TreeSet<>(HISTONE_ARRAY_GROUP_COMPARATOR);

                    for (Map.Entry<String, List<EvalNode>> groups : acc.entrySet()) {
                        final String key = groups.getKey();
                        final EvalNode groupsObjs = new MapEvalNode(groups.getValue());
                        final Optional<Integer> intKey = ParserUtils.tryInt(key);
                        if (intKey.isPresent() && intKey.get() > -1) {
                            entryWithPositiveNumKeys.add(new Tuple<>(intKey.get(), groupsObjs));
                        } else {
                            entrySimpleKeys.add(new Tuple<>(key, groupsObjs));
                        }
                    }

                    for (Tuple<Integer, EvalNode> key : entryWithPositiveNumKeys) {
                        res.put(key.getLeft().toString(), key.getRight());
                    }
                    for (Tuple<String, EvalNode> key : entrySimpleKeys) {
                        res.put(key.getLeft(), key.getRight());
                    }

                    return new MapEvalNode(res);
                });
    }
}

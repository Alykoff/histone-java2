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
import ru.histone.v2.Constants;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.function.macro.MacroCall;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MacroEvalNode;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.AsyncUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author gali.alykoff on 08/02/16.
 */
public class ArrayMap extends AbstractFunction implements Serializable {
    public static final String NAME = "map";
    public static final int MAP_EVAL_INDEX = 0;
    public static final int MACRO_INDEX = 1;
    public static final int ARGS_START_INDEX = 2;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        // [MAP, MACROS, ARGS...]
        final MapEvalNode mapEvalNode = (MapEvalNode) args.get(MAP_EVAL_INDEX);
        final EvalNode node = args.size() > MACRO_INDEX ? args.get(MACRO_INDEX) : null;
        final EvalNode param = args.size() > ARGS_START_INDEX ? args.get(ARGS_START_INDEX) : null;

        final List<CompletableFuture<EvalNode>> mapResultRaw = mapEvalNode.getValue()
                .values().stream()
                .map(arg -> {
                    if (node == null) {
                        return EvalUtils.getValue(ObjectUtils.NULL);
                    }

                    if (node.getType() != HistoneType.T_MACRO) {
                        return CompletableFuture.completedFuture(node);
                    }

                    MacroEvalNode macro = (MacroEvalNode) node;
                    final List<EvalNode> arguments = new ArrayList<>(Collections.singletonList(macro));
                    if (param != null) {
                        arguments.add(param);
                    }
                    arguments.add(arg);
                    return MacroCall.staticExecute(context, arguments);
                })
                .collect(Collectors.toList());
        return AsyncUtils.sequence(mapResultRaw).thenApply(nodes -> {
            Object[] keys = mapEvalNode.getValue().keySet().toArray();
            Map<String, EvalNode> map = new LinkedHashMap<>();
            for (int i = 0; i < nodes.size(); i++) {
                map.put((String) keys[i], nodes.get(i));
            }
            return new MapEvalNode(map);
        });
    }

    private static CompletableFuture<EvalNode> createSelfObject(MacroEvalNode macro, String baseURI, List<EvalNode> args) {
        Map<String, EvalNode> res = new HashMap<>();
        res.put(Constants.SELF_CONTEXT_CALLEE, macro);
        res.put(Constants.SELF_CONTEXT_CALLER, EvalUtils.createEvalNode(baseURI));
        res.put(Constants.SELF_CONTEXT_ARGUMENTS, EvalUtils.constructFromObject(args));

        return EvalUtils.getValue(res);
    }
}

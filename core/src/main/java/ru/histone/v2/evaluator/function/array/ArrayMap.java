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

import org.apache.commons.lang3.ObjectUtils;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.Converter;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.BooleanEvalNode;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.AsyncUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author Gali Alykoff
 */
public class ArrayMap extends AbstractFunction implements Serializable {
    public static final String NAME = "map";
    public static final int MAP_EVAL_INDEX = 0;
    public static final int MACRO_INDEX = 1;
    public static final int ARGS_START_INDEX = 2;

    public ArrayMap(Converter converter) {
        super(converter);
    }

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

        final List<CompletableFuture<EvalNode>> mapResultRaw = new ArrayList<>(mapEvalNode.getValue().size());
        for (Map.Entry<String, EvalNode> entry : mapEvalNode.getValue().entrySet()) {
            if (node == null) {
                mapResultRaw.add(converter.getValue(ObjectUtils.NULL));
                continue;
            }

            if (node.getType() != HistoneType.T_MACRO) {
                mapResultRaw.add(CompletableFuture.completedFuture(node));
                continue;
            }

            final List<EvalNode> arguments = new ArrayList<>(Collections.singletonList(node));
            arguments.add(new BooleanEvalNode(false)); //do not unwrap arguments
            if (param != null) {
                arguments.add(param);
            }
            arguments.add(entry.getValue());
            arguments.add(converter.createEvalNode(entry.getKey()));
            arguments.add(mapEvalNode);
            mapResultRaw.add(context.macroCall(arguments));
        }
        return AsyncUtils.sequence(mapResultRaw).thenApply(nodes -> {
            Object[] keys = mapEvalNode.getValue().keySet().toArray();
            Map<String, EvalNode> map = new LinkedHashMap<>();
            for (int i = 0; i < nodes.size(); i++) {
                map.put((String) keys[i], nodes.get(i));
            }
            return new MapEvalNode(map);
        });
    }
}

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
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.utils.RttiUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Gali Alykoff
 */
public class ArrayChunk extends AbstractFunction implements Serializable {
    public static final String NAME = "chunk";
    private static final int MAP_EVAL_INDEX = 0;
    private static final int SPLIT_SIZE_INDEX = 1;
    private static final int DEFAULT_CHUNK_SIZE = 1;

    @SuppressWarnings("unchecked")
    private static List<Map<String, EvalNode>> chunk(Map<String, EvalNode> original, int n) {
        if (n < 1) {
            throw new IllegalArgumentException("Chunk size should be non zero positive value");
        }
        int i = -1;
        Map<String, EvalNode> partition = Collections.EMPTY_MAP;
        boolean isArray = EvalUtils.isArray(original.keySet());
        final List<Map<String, EvalNode>> partitions = new ArrayList<>();
        for (Map.Entry<String, EvalNode> entry : original.entrySet()) {
            if (++i % n == 0) {
                i = 0;
                partition = new LinkedHashMap<>();
                partitions.add(partition);
            }
            partition.put(isArray ? String.valueOf(i) : entry.getKey(), entry.getValue());
        }
        return partitions;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        final MapEvalNode mapEvalNode = (MapEvalNode) args.get(MAP_EVAL_INDEX);
        CompletableFuture<Optional<Integer>> splitSizeValue = CompletableFuture.completedFuture(Optional.of(DEFAULT_CHUNK_SIZE));
        if (!(args.size() <= SPLIT_SIZE_INDEX)) {
            splitSizeValue = RttiUtils
                    .callToNumber(context, args.get(SPLIT_SIZE_INDEX))
                    .thenApply(x -> {
                        Optional<Integer> result = EvalUtils.tryPureIntegerValue(x).filter(v -> v > 0);
                        return !result.isPresent() ? Optional.of(DEFAULT_CHUNK_SIZE) : result;
                    });
        }
        return splitSizeValue.thenApply(sizeOptional ->
                        sizeOptional.map(size -> {
                            final List<EvalNode> chunkedValues = chunk(mapEvalNode.getValue(), size)
                                    .stream()
                                    .map(MapEvalNode::new)
                                    .collect(Collectors.toList());
                            return new MapEvalNode(chunkedValues);
                        }).orElse(mapEvalNode)
        );
    }
}

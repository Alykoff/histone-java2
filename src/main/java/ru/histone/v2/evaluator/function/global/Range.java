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

package ru.histone.v2.evaluator.function.global;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Alexey Nevinsky
 */
public class Range extends AbstractFunction {
    public static final String NAME = "range";
    public static final int STEP_BY_DEFAULT = 1;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        if (args.get(0).getType() == HistoneType.T_GLOBAL) {
            List<EvalNode> localArgs = args.subList(1, args.size());
            return processRange(localArgs);
        }

        return processRange(args);
    }

    protected CompletableFuture<EvalNode> processRange(List<EvalNode> args) {
        final int size = args.size();
        if (args.size() == 0) {
            return getEmptyMapNodeFuture();
        }

        for (int i = 0; i < size && i < 2; i++) {
            final EvalNode node = args.get(i);
            if (!EvalUtils.tryPureIntegerValue(node).isPresent()) {
                return getEmptyMapNodeFuture();
            }
        }

        long from = EvalUtils.tryPureIntegerValue(args.get(0)).get();
        long step = STEP_BY_DEFAULT;
        Long to = null;
        if (size > 2) {
            to = getValue(args.get(1));
            step = EvalUtils.tryPureIntegerValue(args.get(2))
                    .filter(s -> s > 0)
                    .orElse(STEP_BY_DEFAULT);

        } else if (size > 1) {
            to = getValue(args.get(1));
        } else if (from < 0) {
            to = 0L;
            from = from + 1;
        } else if (from > 0) {
            to = from - 1;
            from = 0L;
        }

        final List<EvalNode> res = new ArrayList<>();
        if (to == null) {
            return CompletableFuture.completedFuture(new MapEvalNode(res));
        }
        if (from < to) {
            while (from <= to) {
                res.add(EvalUtils.createEvalNode(from));
                from += step;
            }
        } else {
            while (from >= to) {
                res.add(EvalUtils.createEvalNode(from));
                from -= step;
            }
        }
        return CompletableFuture.completedFuture(new MapEvalNode(res));
    }

    private CompletableFuture<EvalNode> getEmptyMapNodeFuture() {
        return CompletableFuture.completedFuture(new MapEvalNode(Collections.emptyList()));
    }

    private long getValue(EvalNode node) {
        Number n = EvalUtils.getNumberValue(node);
        if (n instanceof Double) {
            return n.longValue();
        }
        return (long) n;
    }
}

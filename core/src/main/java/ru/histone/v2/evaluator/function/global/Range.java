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
import ru.histone.v2.evaluator.Converter;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

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

    public Range(Converter converter) {
        super(converter);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        return processRange(clearGlobal(args));
    }

    protected CompletableFuture<EvalNode> processRange(List<EvalNode> args) {
        final int size = args.size();
        if (args.size() == 0) {
            return getEmptyMapNodeFuture();
        }

        for (int i = 0; i < size && i < 2; i++) {
            final EvalNode node = args.get(i);
            if (!converter.tryPureIntegerValue(node).isPresent()) {
                return getEmptyMapNodeFuture();
            }
        }

        long from = converter.tryPureIntegerValue(args.get(0)).get();
        long step = STEP_BY_DEFAULT;
        Long to = null;
        if (size > 2) {
            to = getValue(args.get(1));
            step = converter.tryPureIntegerValue(args.get(2))
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
                res.add(converter.createEvalNode(from));
                from += step;
            }
        } else {
            while (from >= to) {
                res.add(converter.createEvalNode(from));
                from -= step;
            }
        }
        return CompletableFuture.completedFuture(new MapEvalNode(res));
    }

    private CompletableFuture<EvalNode> getEmptyMapNodeFuture() {
        return CompletableFuture.completedFuture(new MapEvalNode(Collections.emptyList()));
    }

    private long getValue(EvalNode node) {
        Number n = converter.getNumberValue(node);
        if (n instanceof Double) {
            return n.longValue();
        }
        return (long) n;
    }
}

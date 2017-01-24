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
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Alexey Nevinsky
 */
public class GetRand extends AbstractFunction {

    private static final long MAX = Integer.toUnsignedLong(Integer.MAX_VALUE);
    private static final long MIN = 0;

    public GetRand(Converter converter) {
        super(converter);
    }

    @Override
    public String getName() {
        return "getRand";
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        if (args.size() == 0) {
            return getRandom(MIN, MAX);
        }

        List<EvalNode> argsToProcess = args;
        if (args.get(0).getType() != HistoneType.T_NUMBER) {
            argsToProcess = args.subList(1, args.size());
        }

        long min = getLongValue(argsToProcess, 0, MIN);
        long max = getLongValue(argsToProcess, 1, MAX);
        if (min > max) {
            return getRandom(max, min);
        }
        return getRandom(min, max);
    }

    private CompletableFuture<EvalNode> getRandom(long min, long max) {
        Double res = Math.floor(Math.random() * (max - min + 1)) + min;
        return converter.getValue(res.longValue());
    }
}

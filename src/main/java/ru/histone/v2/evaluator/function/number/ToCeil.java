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

package ru.histone.v2.evaluator.function.number;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.DoubleEvalNode;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by inv3r on 28/01/16.
 */
public class ToCeil extends AbstractFunction {
    @Override
    public String getName() {
        return "toCeil";
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        if (args.get(0) instanceof DoubleEvalNode) {
            Float v = (float) Math.ceil(((DoubleEvalNode) args.get(0)).getValue());
            if (v % 1 == 0 && v <= Long.MAX_VALUE) {
                return EvalUtils.getValue(v.longValue());
            }
            return EvalUtils.getValue(v);
        }
        return CompletableFuture.completedFuture(args.get(0));
    }
}

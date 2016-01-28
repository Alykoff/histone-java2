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

import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.FloatEvalNode;
import ru.histone.v2.evaluator.node.LongEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by inv3r on 28/01/16.
 */
public class ToFixed extends AbstractFunction {
    @Override
    public String getName() {
        return "toFixed";
    }

    @Override
    public CompletableFuture<EvalNode> execute(String baseUri, List<EvalNode> args) throws FunctionExecutionException {
        if (args.get(0) instanceof LongEvalNode) {
            return CompletableFuture.completedFuture(args.get(0));
        }
        long count = ((LongEvalNode) args.get(1)).getValue();
        Float v = ((FloatEvalNode) args.get(0)).getValue();

        return EvalUtils.getValue(new DecimalFormat(format(count)).format(v));
    }

    private String format(long count) {
        StringBuilder sb = new StringBuilder("#");
        for (int i = 0; i < count; i++) {
            if (i == 0) {
                sb.append(".");
            }
            sb.append("#");
        }
        return sb.toString();
    }
}

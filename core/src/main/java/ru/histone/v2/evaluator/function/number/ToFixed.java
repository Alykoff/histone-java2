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
import ru.histone.v2.evaluator.node.LongEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author Alexey Nevinsky
 */
public class ToFixed extends AbstractFunction {

    public static final int DEFAULT_INDEX_OF_CUT = 0;

    @Override
    public String getName() {
        return "toFixed";
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        if (args.get(0) instanceof LongEvalNode) {
            return CompletableFuture.completedFuture(args.get(0));
        }
        double input = ((DoubleEvalNode) args.get(0)).getValue();

        long count = Optional.ofNullable(args.size() > 1 ? args.get(1) : null)
                .flatMap(EvalUtils::tryPureIntegerValue)
                .filter(x -> x > 0)
                .orElse(DEFAULT_INDEX_OF_CUT);
        final double result = Double.parseDouble(new DecimalFormat(format(count),
                new DecimalFormatSymbols(context.getLocale())).format(input));
        return EvalUtils.getNumberFuture(result);
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

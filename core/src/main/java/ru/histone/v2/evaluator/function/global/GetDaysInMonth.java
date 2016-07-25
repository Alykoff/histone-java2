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
import ru.histone.v2.evaluator.node.EmptyEvalNode;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.utils.DateUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static ru.histone.v2.utils.DateUtils.*;
import static ru.histone.v2.utils.ParserUtils.tryIntNumber;

/**
 * @author Alexey Nevinsky
 */
public class GetDaysInMonth extends AbstractFunction {
    @Override
    public String getName() {
        return "getDaysInMonth";
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        return doExecute(clearGlobal(args));
    }

    protected CompletableFuture<EvalNode> doExecute(List<EvalNode> args) {
        if (args.size() < 2) {
            return EvalUtils.getValue(null);
        }

        final Optional<Integer> yearOptional = tryIntNumber(args.get(0).getValue())
                .filter(year -> year >= JS_MIN_BOUND_OF_YEAR && year <= JS_MAX_BOUND_OF_YEAR);
        final Optional<Integer> monthOptional = tryIntNumber(args.get(1).getValue())
                .filter(month -> month >= MIN_MONTH && month <= MAX_MONTH)
                .map(month -> month - 1);

        if (!yearOptional.isPresent() || !monthOptional.isPresent()) {
            return CompletableFuture.completedFuture(new EmptyEvalNode());
        }

        try {
            int dayOfWeek = DateUtils.getDaysInMonth(yearOptional.get(), monthOptional.get());
            if (dayOfWeek == 0) {
                dayOfWeek = 7;
            }
            return EvalUtils.getValue(dayOfWeek);
        } catch (IllegalArgumentException e) {
            return EvalUtils.getValue(null);
        }
    }
}

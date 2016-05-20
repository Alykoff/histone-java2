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

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static ru.histone.v2.utils.DateUtils.*;
import static ru.histone.v2.utils.ParserUtils.tryIntNumber;

/**
 * @author Alexey Nevinsky
 */
public class GetDayOfWeek extends AbstractFunction {
    @Override
    public String getName() {
        return "getDayOfWeek";
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        return doExecute(clearGlobal(args));
    }

    private CompletableFuture<EvalNode> doExecute(List<EvalNode> args) {
        if (args.size() < 3) {
            return EvalUtils.getValue(null);
        }

        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.setLenient(false);
        final Optional<Integer> yearOptional = tryIntNumber(args.get(0).getValue())
                .filter(year -> year >= JS_MIN_BOUND_OF_YEAR && year <= JS_MAX_BOUND_OF_YEAR);
        final Optional<Integer> monthOptional = tryIntNumber(args.get(1).getValue())
                .filter(month -> month >= MIN_MONTH && month <= MAX_MONTH)
                .map(month -> month - 1);
        final Optional<Integer> dayOptional = yearOptional.flatMap(year ->
                monthOptional.flatMap(month -> {
                    final int daysInMonth;
                    try {
                        daysInMonth = DateUtils.getDaysInMonth(year, month);
                    } catch (IllegalArgumentException e) {
                        return Optional.empty();
                    }
                    return tryIntNumber(args.get(2).getValue())
                            .filter(day -> day <= daysInMonth && day >= MIN_DAY);
                })
        );

        if (!yearOptional.isPresent() || !monthOptional.isPresent() || !dayOptional.isPresent()) {
            return CompletableFuture.completedFuture(new EmptyEvalNode());
        }
        c.set(Calendar.YEAR, yearOptional.get());
        c.set(Calendar.MONTH, monthOptional.get());
        c.set(Calendar.DAY_OF_MONTH, dayOptional.get());

        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
        if (dayOfWeek == 0) {
            dayOfWeek = 7;
        }

        return EvalUtils.getValue(dayOfWeek);
    }
}

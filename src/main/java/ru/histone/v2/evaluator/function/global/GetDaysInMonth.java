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
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
        checkMinArgsLength(args, 2);
        checkMaxArgsLength(args, 2);
        checkTypes(args.get(0), 0, Arrays.asList(HistoneType.T_NUMBER, HistoneType.T_STRING), Arrays.asList(String.class, Long.class));


        Calendar c = new GregorianCalendar();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.setLenient(false);
        c.set(Calendar.YEAR, EvalUtils.getNumberValue(args.get(0)).intValue());
        c.set(Calendar.MONTH, EvalUtils.getNumberValue(args.get(1)).intValue() - 1);

        try {
            c.getTimeInMillis();
        } catch (IllegalArgumentException e) {
            return EvalUtils.getValue(null);
        }

        int dayOfWeek = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (dayOfWeek == 0) {
            dayOfWeek = 7;
        }

        return EvalUtils.getValue(dayOfWeek);
    }
}

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

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Method returns current date
 *
 * @author alexey.nevinsky
 */
public class GetDate extends AbstractFunction {
    private static final Pattern PATTERN_DELTA_DATE = Pattern.compile("([+-])(\\d+)([dmyDMY])");
    private static final String NEGATIVE_SIGN = "-";
    private static final String DAY_SYMBOL = "d";
    private static final String MONTH_SYMBOL = "m";
    private static final String YEAR_SYMBOL = "y";

    @Override
    public String getName() {
        return "getDate";
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        return doExecute(clearGlobal(args));
    }

    private CompletableFuture<EvalNode> doExecute(List<EvalNode> args) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        if (args.size() >= 1 && args.get(0).getType() == HistoneType.T_STRING) {
            final String value = (String) args.get(0).getValue();
            final Matcher matcher = PATTERN_DELTA_DATE.matcher(value);
            while(matcher.find()) {
                final String sign = matcher.group(1);
                final Integer num = Integer.parseInt(matcher.group(2)) * (sign.equals(NEGATIVE_SIGN) ? -1 : 1);
                final String period = matcher.group(3).toLowerCase();
                switch (period) {
                    case DAY_SYMBOL:
                        calendar.add(Calendar.DAY_OF_MONTH, num);
                        break;
                    case MONTH_SYMBOL:
                        calendar.add(Calendar.MONTH, num);
                        break;
                    case YEAR_SYMBOL:
                        calendar.add(Calendar.YEAR, num);
                        break;
                }
            }
        }
        final Map<String, EvalNode> res = new LinkedHashMap<>();
        res.put("day", EvalUtils.createEvalNode((long) calendar.get(Calendar.DAY_OF_MONTH)));
        res.put("month", EvalUtils.createEvalNode((long) calendar.get(Calendar.MONTH) + 1));
        res.put("year", EvalUtils.createEvalNode((long) calendar.get(Calendar.YEAR)));

        return EvalUtils.getValue(res);
    }

}

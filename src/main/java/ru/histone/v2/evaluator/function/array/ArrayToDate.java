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

package ru.histone.v2.evaluator.function.array;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.DateUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author Alexey Nevinsky
 */
public class ArrayToDate extends AbstractFunction {
    @Override
    public String getName() {
        return "toDate";
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        Map<String, EvalNode> map = getValue(args, 0);

        LocalDateTime dateTime = createDate(map);
        if (dateTime == null) {
            return EvalUtils.getValue(null);
        }

        if (args.size() >= 2 && args.get(1).getType() == HistoneType.T_STRING) {
            final String value = (String) args.get(1).getValue();
            dateTime = DateUtils.applyOffset(dateTime, value);
        }

        EvalNode node = EvalUtils.createEvalNode(DateUtils.createMapFromDate(dateTime), true);
        return CompletableFuture.completedFuture(node);
    }

    private LocalDateTime createDate(Map<String, EvalNode> map) {
        EvalNode yearNode = map.get("year");
        final int yearValue;
        if (yearNode != null) {
            Optional<Integer> year = EvalUtils.tryPureIntegerValue(yearNode);
            if (year.isPresent()) {
                yearValue = year.get();
            } else {
                return null;
            }
        } else {
            return null;
        }

        EvalNode monthNode = map.get("month");
        final int monthValue;
        if (monthNode != null) {
            ProcessResult res = processValue(monthNode, 0, 13);
            if (res.future != null) {
                return null;
            }
            monthValue = res.value;
        } else {
            return constructDate(yearValue, -1, -1, -1, -1, -1);
        }

        LocalDate currentDate = LocalDate.of(yearValue, monthValue, 1);
        int daysCount = currentDate.getMonth().length(currentDate.isLeapYear());

        EvalNode dayNode = map.get("day");
        final int dayValue;
        if (dayNode != null) {
            ProcessResult res = processValue(dayNode, 0, daysCount + 1);
            if (res.future != null) {
                return null;
            }
            dayValue = res.value;
        } else {
            return constructDate(yearValue, monthValue, -1, -1, -1, -1);
        }

        EvalNode hourNode = map.get("hour");
        final int hourValue;
        if (hourNode != null) {
            ProcessResult res = processValue(hourNode, -1, 24);
            if (res.future != null) {
                return null;
            }
            hourValue = res.value;
        } else {
            return constructDate(yearValue, monthValue, dayValue, -1, -1, -1);
        }

        EvalNode minuteNode = map.get("minute");
        final int minuteValue;
        if (minuteNode != null) {
            ProcessResult res = processValue(minuteNode, -1, 60);
            if (res.future != null) {
                return null;
            }
            minuteValue = res.value;
        } else {
            return constructDate(yearValue, monthValue, dayValue, hourValue, -1, -1);
        }

        EvalNode secondNode = map.get("second");
        final int secondValue;
        if (secondNode != null) {
            ProcessResult res = processValue(secondNode, -1, 60);
            if (res.future != null) {
                return null;
            }
            secondValue = res.value;
        } else {
            return constructDate(yearValue, monthValue, dayValue, hourValue, minuteValue, -1);
        }


        return constructDate(yearValue, monthValue, dayValue, hourValue, minuteValue, secondValue);
    }

    private ProcessResult processValue(EvalNode node, int minValue, int maxValue) {
        Optional<Integer> value = EvalUtils.tryPureIntegerValue(node);
        if (value.isPresent() && value.get() > minValue && value.get() < maxValue) {
            return new ProcessResult(value.get());
        } else {
            return new ProcessResult(EvalUtils.getValue(null));
        }
    }

    private LocalDateTime constructDate(int year, int month, int day, int hour, int minute, int second) {
        int monthValue = month < 1 || month > 12 ? 1 : month;
        LocalDate currentDate = LocalDate.of(year, monthValue, 1);
        int dayValue = day < 1 || day > currentDate.getMonth().length(currentDate.isLeapYear()) ? 1 : day;

        int hourValue = hour >= 0 && hour <= 23 ? hour : 0;
        int minuteValue = minute >= 0 && minute <= 59 ? minute : 0;
        int secondValue = second >= 0 && second <= 59 ? second : 0;

        return LocalDateTime.of(year, monthValue, dayValue, hourValue, minuteValue, secondValue);
    }

    private class ProcessResult {
        CompletableFuture<EvalNode> future = null;
        int value = Integer.MIN_VALUE;

        ProcessResult(CompletableFuture<EvalNode> future) {
            this.future = future;
        }

        ProcessResult(int value) {
            this.value = value;
        }
    }
}

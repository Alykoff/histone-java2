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

package ru.histone.v2.evaluator.function.string;

import org.apache.commons.lang3.tuple.Pair;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.DateUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.histone.v2.utils.DateUtils.*;

/**
 * @author Alexey Nevinsky
 */
public class StringToDate extends AbstractFunction {
    @Override
    public String getName() {
        return "toDate";
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        if (args.size() < 2) {
            return EvalUtils.getValue(null);
        }

        String value = getValue(args, 0);
        String format = getValue(args, 1);

        LocalDateTime dateTime = parseDate(value, format);
        if (dateTime == null) {
            return EvalUtils.getValue(null);
        }
        if (args.size() >= 3 && args.get(2).getType() == HistoneType.T_STRING) {
            final String offset = (String) args.get(2).getValue();
            dateTime = DateUtils.applyOffset(dateTime, offset);
        }

        EvalNode node = EvalUtils.createEvalNode(DateUtils.createMapFromDate(dateTime), true);
        return CompletableFuture.completedFuture(node);
    }

//    private LocalDateTime parseDate(String value, String format) {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
//        if (StringUtils.containsAny(format, "Hms")) {
//            return LocalDateTime.parse(value, formatter);
//        }
//        return LocalDate.parse(value, formatter).atStartOfDay();
//    }

    private LocalDateTime parseDate(String value, String format) {
        Pair<String, List<String>> regexpObject = createRegexpString(format);
        Pattern pattern = Pattern.compile(regexpObject.getKey());
        Matcher m = pattern.matcher(value);

        Map<String, Integer> map = new HashMap<>();
        map.put(YEAR_SYMBOL, 0);
        map.put(MONTH_SYMBOL, 1);
        map.put(DAY_SYMBOL, 1);
        map.put(HOUR_SYMBOL, 0);
        map.put(MINUTE_SYMBOL, 0);
        map.put(SECOND_SYMBOL, 0);
        if (m.matches()) {
            for (int i = 1; i <= m.groupCount(); i++) {
                map.put(regexpObject.getValue().get(i - 1), Integer.valueOf(m.group(i)));
            }
        } else {
            return null;
        }

        return LocalDateTime.of(map.get("Y"), map.get("M"), map.get("D"), map.get("h"), map.get("m"), map.get("s"));
    }

    private Pair<String, List<String>> createRegexpString(String format) {
        Pattern pattern = Pattern.compile("([YMDhms])");
        Matcher m = pattern.matcher(format);
        List<String> keys = new ArrayList<>();
        int start = 0;
        while (m.find(start) || start > format.length()) {
            String key = format.substring(m.start(), m.end());
            keys.add(key);
            start = m.end();
        }

        String res = "^" + format.replaceAll("([YMDhms])", "([0-9]{1,4})") + "$";
        return Pair.of(res, keys);
    }
}

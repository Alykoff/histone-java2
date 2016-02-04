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

import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.function.LocaleFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;

/**
 * @author alexey.nevinsky
 */
public class GetWeekDayName extends LocaleFunction {

    private boolean isShort;
    private ConcurrentMap<String, Properties> props;

    public GetWeekDayName(boolean isShort) {
        super();
        this.isShort = isShort;
    }

    @Override
    public String getName() {
        return isShort ? "getWeekDayNameShort" : "getWeekDayNameLong";
    }

    @Override
    public CompletableFuture<EvalNode> execute(String baseUri, Locale locale, List<EvalNode> args) throws FunctionExecutionException {
        Properties properties = getCurrentProperties(locale);

        long id = getValue(args, 0);

        StringBuilder sb = new StringBuilder("WEEK_DAYS_");
        if (isShort) {
            sb.append("SHORT");
        } else {
            sb.append("LONG");
        }
        sb.append("_").append(id);
        return EvalUtils.getValue(properties.getProperty(sb.toString()));
    }
}

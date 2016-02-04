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
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by inv3r on 22/01/16.
 */
public class Range extends AbstractFunction{

    public static final String NAME = "range";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(String baseUri, Locale locale, List<EvalNode> args) throws FunctionExecutionException {
        if (args.size() != 2) {
            throw new IllegalArgumentException("Wrong count of arguments. Actual is " + args.size() + ", but expected is 2");
        }
        long from = (long) args.get(0).getValue();
        long to = (long) args.get(1).getValue();

        Map<String, Object> res = new LinkedHashMap<>();
        for (int i = 0; i < to - from + 1; i++) {
            res.put(i + "", from + i);
        }
        return EvalUtils.getValue(res);
    }
}

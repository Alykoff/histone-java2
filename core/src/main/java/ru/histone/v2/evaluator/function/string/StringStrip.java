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

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.Converter;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.StringEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Gali Alykoff
 */
public class StringStrip extends AbstractFunction {
    public static final String NAME = "strip";

    public StringStrip(Converter converter) {
        super(converter);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        String value = ((StringEvalNode) args.get(0)).getValue();

        List<String> charsToRemove = Arrays.asList(" ", "\r", "\n", "\t");
        if (args.size() == 2) {
            charsToRemove = Arrays.asList(((StringEvalNode) args.get(1)).getValue().split(""));
        }

        if (value.length() == 0) {
            return converter.getValue(value);
        }
        int head = 0;
        int tail = value.length();

        //строка может быть пустой
        while (head < tail && charsToRemove.contains(value.charAt(head) + "")) {
            head++;
        }
        while (tail > head && charsToRemove.contains(value.charAt(tail - 1) + "")) {
            tail--;
        }

        return converter.getValue(value.substring(head, tail));
    }
}

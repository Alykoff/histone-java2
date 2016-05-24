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

package ru.histone.v2.evaluator.function.regex;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.data.HistoneRegex;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Gali Alykoff
 */
public class Test extends AbstractFunction implements Serializable {
    public static final String NAME = "test";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        try {
            final HistoneRegex regexHistone = (HistoneRegex) args.get(0).getValue();
            if (args.size() < 2) {
                return EvalUtils.getValue(false);
            }
            final EvalNode evalNode = args.get(1);
            final String exp = String.valueOf(evalNode.getValue());
            final Pattern pattern = regexHistone.getPattern();
//            final boolean isGlobal = regexHistone.isGlobal();

            final Matcher matcher = pattern.matcher(exp);
            final boolean result = matcher.find();

            return EvalUtils.getValue(result);
        } catch (Exception e) {
            throw new FunctionExecutionException(e);
        }
    }
}

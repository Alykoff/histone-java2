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
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.LongEvalNode;
import ru.histone.v2.evaluator.node.StringEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author Gali Alykoff
 */
public class StringCharCodeAt extends AbstractFunction {
    public static final String NAME = "charCodeAt";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        final Optional<Integer> indexOptional = EvalUtils.tryPureIntegerValue(args.get(1));
        if (!indexOptional.isPresent()) {
            return EvalUtils.getValue(null);
        }
        final String value = ((StringEvalNode) args.get(0)).getValue();
        int index = indexOptional.get();
        int lenght = value.length();
        if (index < 0) index = lenght + index;
        if (index >= 0 && index < lenght) {
            final int character = (int) value.charAt(index);
            return CompletableFuture.completedFuture(new LongEvalNode(character));
        }
        return EvalUtils.getValue(null);
    }
}

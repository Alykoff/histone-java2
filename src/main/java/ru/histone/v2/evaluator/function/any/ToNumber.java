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

package ru.histone.v2.evaluator.function.any;

import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EmptyEvalNode;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.StringEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * Created by inv3r on 28/01/16.
 */
public class ToNumber extends AbstractFunction {
    @Override
    public String getName() {
        return "toNumber";
    }

    @Override
    public CompletableFuture<EvalNode> execute(String baseUri, Locale locale, List<EvalNode> args) throws FunctionExecutionException {
        EvalNode node = args.get(0);
        if (EvalUtils.isNumberNode(node)) {
            return CompletableFuture.completedFuture(node);
        } else if (node instanceof StringEvalNode && EvalUtils.isNumeric((StringEvalNode) node)) {
            Float v = Float.parseFloat(((StringEvalNode) node).getValue());
            if (v % 1 == 0 && v <= Long.MAX_VALUE) {
                return EvalUtils.getValue(v.longValue());
            } else {
                return EvalUtils.getValue(v);
            }
        } else if (args.size() > 1) {
            return CompletableFuture.completedFuture(args.get(1));
        }
        return CompletableFuture.completedFuture(EmptyEvalNode.INSTANCE);
    }
}

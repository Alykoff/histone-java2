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
import ru.histone.v2.evaluator.global.NumberComparator;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * @author alexey.nevinsky
 */
public class GetMax extends AbstractFunction {

    private static final NumberComparator comparator = new NumberComparator();

    @Override
    public String getName() {
        return "getMax";
    }

    @Override
    public CompletableFuture<EvalNode> execute(String baseUri, Locale locale, List<EvalNode> args) throws FunctionExecutionException {
        if (args.isEmpty()) {
            return EvalUtils.getValue(null);
        }

        Number max = null;
        for (EvalNode arg : args) {
            if (arg.getType() == HistoneType.T_NUMBER) {
                Number next = (Number) arg.getValue();
                max = (max == null || comparator.compare(max, next) < 0) ? next : max;
            } else {
                max = findMax(max, arg);
            }
        }
        if (max == null) {
            return EvalUtils.getValue(null);
        }

        return EvalUtils.getValue(max);
    }

    private Number findMax(Number max, EvalNode values) {
        if (values.getType() == HistoneType.T_ARRAY) {
            for (EvalNode node : ((MapEvalNode) values).getValue().values()) {
                if (node.getType() == HistoneType.T_NUMBER) {
                    Number next = (Number) node.getValue();
                    max = (max == null || comparator.compare(max, next) < 0) ? next : max;
                } else if (node.getType() == HistoneType.T_ARRAY) {
                    max = findMax(max, node);
                }
            }
        }
        return max;
    }

}

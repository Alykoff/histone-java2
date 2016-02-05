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

import ru.histone.evaluator.functions.global.GlobalFunctionExecutionException;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.LongEvalNode;
import ru.histone.v2.evaluator.node.StringEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by inv3r on 22/01/16.
 */
public class Range extends AbstractFunction {

    public static final String NAME = "range";

    private static boolean checkArg(EvalNode node) throws GlobalFunctionExecutionException {
        if (node.getType() == HistoneType.T_STRING && EvalUtils.isNumeric((StringEvalNode) node)) {
            return true;
        }

        if (!(node instanceof LongEvalNode)) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(String baseUri, Locale locale, List<EvalNode> args) throws FunctionExecutionException {
        if (args.size() < 2) {
//            throw new FunctionExecutionException("Function range() needs to have two arguments, but you provided '" + args.size() + "' arguments");
            return EvalUtils.getValue(null);
        }

        if (args.size() > 3) {
            throw new FunctionExecutionException("Function range() has only two arguments, but you provided '" + args.size() + "' arguments");
        }

        for (EvalNode node : args) {
            if (!checkArg(node)) {
                return EvalUtils.getValue(null);
            }
        }

        long from = getValue(args.get(0));
        long to = getValue(args.get(1));

        Long step = getValue(args, 2);
        Map<String, EvalNode> res = new LinkedHashMap<>();
        if (from <= to) {
            for (long i = from; i <= to; i++) {
                if (step != null) {
                    res.put((i - from) + "", EvalUtils.createEvalNode(step * i));
                } else {
                    res.put((i - from) + "", EvalUtils.createEvalNode(i));
                }
            }
        } else {
            for (long i = from; i >= to; i--) {
                if (step != null) {
                    res.put((from - i) + "", EvalUtils.createEvalNode(step * i));
                } else {
                    res.put((from - i) + "", EvalUtils.createEvalNode(i));
                }
            }
        }
        return EvalUtils.getValue(res);
    }

    private long getValue(EvalNode node) {
        Number n = EvalUtils.getNumberValue(node);
        if (n instanceof Double) {
            return n.longValue();
        }
        return (long) n;
    }
}

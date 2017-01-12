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

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.Converter;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.global.NumberComparator;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Alexey Nevinsky
 */
public class GetMinMax extends AbstractFunction {

    private static final NumberComparator comparator = new NumberComparator();

    private final boolean isMin;

    public GetMinMax(Converter converter, boolean isMin) {
        super(converter);
        this.isMin = isMin;
    }

    @Override
    public String getName() {
        return isMin ? "getMin" : "getMax";
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        return doExecute(clearGlobal(args));
    }

    private CompletableFuture<EvalNode> doExecute(List<EvalNode> args) {
        if (args.isEmpty()) {
            return converter.getValue(null);
        }

        Number max = null;
        for (EvalNode arg : args) {
            if (arg.getType() == HistoneType.T_NUMBER) {
                Number next = (Number) arg.getValue();
                if (max == null) {
                    max = next;
                } else {
                    if (isMin) {
                        if (comparator.compare(max, next) > 0) {
                            max = next;
                        }
                    } else {
                        if (comparator.compare(max, next) < 0) {
                            max = next;
                        }
                    }
                }
            } else {
                max = findMax(max, arg);
            }
        }
        if (max == null) {
            return converter.getValue(null);
        }

        return converter.getValue(max);
    }

    private Number findMax(Number max, EvalNode values) {
        if (values.getType() == HistoneType.T_ARRAY) {
            for (EvalNode node : ((MapEvalNode) values).getValue().values()) {
                if (node.getType() == HistoneType.T_NUMBER) {
                    Number next = (Number) node.getValue();
                    if (max == null) {
                        max = next;
                    } else {
                        if (isMin && comparator.compare(max, next) > 0) {
                            max = next;
                        } else if (!isMin && comparator.compare(max, next) < 0) {
                            max = next;
                        }
                    }
                } else if (node.getType() == HistoneType.T_ARRAY) {
                    max = findMax(max, node);
                }
            }
        }
        return max;
    }

}

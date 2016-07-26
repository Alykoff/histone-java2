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

import org.apache.commons.collections.CollectionUtils;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.GlobalEvalNode;
import ru.histone.v2.evaluator.node.HasProperties;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.RttiMethod;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Alexey Nevinsky
 */
public class GetFunction extends AbstractFunction {
    @Override
    public String getName() {
        return RttiMethod.RTTI_M_GET.getId();
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        EvalNode value = args.get(0);
        if (value instanceof GlobalEvalNode) {
            if (CollectionUtils.isNotEmpty(args) && args.size() == 2) {
                return EvalUtils.getValue(args.get(1).getValue() instanceof String
                        ? context.getGlobalProperties().get(args.get(1).getValue()) : null);
            }
        }

        if (value instanceof HasProperties) {
            Object propName = args.get(1).getValue();
            EvalNode res = ((HasProperties) value).getProperty(propName);
            if (res != null) {
                return CompletableFuture.completedFuture(res);
            }

            return EvalUtils.getValue(null);
        }

        return EvalUtils.getValue(null);
    }
}

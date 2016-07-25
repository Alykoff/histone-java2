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

package ru.histone.v2.evaluator.function.array;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.DateUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Alexey Nevinsky
 */
public class ArrayToDate extends AbstractFunction {
    @Override
    public String getName() {
        return "toDate";
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        if (args.get(0).hasAdditionalType(HistoneType.T_DATE) && (args.size() < 2 || args.get(1).getType() != HistoneType.T_STRING)) {
            return CompletableFuture.completedFuture(args.get(0));
        }

        Map<String, EvalNode> map = getValue(args, 0);

        LocalDateTime dateTime = DateUtils.createDate(map);
        if (dateTime == null) {
            return EvalUtils.getValue(null);
        }

        if (args.size() >= 2 && args.get(1).getType() == HistoneType.T_STRING) {
            final String value = (String) args.get(1).getValue();
            dateTime = DateUtils.applyOffset(dateTime, value);
        }

        EvalNode node = EvalUtils.createEvalNode(DateUtils.createMapFromDate(dateTime), true);
        return CompletableFuture.completedFuture(node);
    }

}

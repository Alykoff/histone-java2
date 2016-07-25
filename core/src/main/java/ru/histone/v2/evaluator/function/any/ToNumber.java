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

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.DateEvalNode;
import ru.histone.v2.evaluator.node.DoubleEvalNode;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.StringEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.DateUtils;

import java.time.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Alexey Nevinsky
 */
public class ToNumber extends AbstractFunction {

    public static final String NAME = "toNumber";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        EvalNode node = args.get(0);
        if (EvalUtils.isNumberNode(node)) {
            return getFromNumberNode(node);
        } else if (node.getType() == HistoneType.T_STRING && EvalUtils.isNumeric((StringEvalNode) node)) {
            Double v = Double.parseDouble(((StringEvalNode) node).getValue());
            return EvalUtils.getNumberFuture(v);
        } else if (node.hasAdditionalType(HistoneType.T_DATE)) {
            DateEvalNode dateNode = (DateEvalNode) node;
            LocalDateTime dateTime = DateUtils.createDate(dateNode.getValue());
            ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.systemDefault());
            ZoneOffset offset = zonedDateTime.getOffset();
            Instant instant = dateTime.toInstant(offset);
            Long res = instant.getEpochSecond() * 1000 + instant.getNano() / 1000;
            return EvalUtils.getValue(res);
        } else if (node.getType() == HistoneType.T_BOOLEAN) {
            long res = ((Boolean) node.getValue()) ? 1L : 0L;
            return EvalUtils.getValue(res);
        } else if (args.size() > 1) {
            return CompletableFuture.completedFuture(args.get(1));
        }
        return EvalUtils.getValue(null);
    }

    private CompletableFuture<EvalNode> getFromNumberNode(EvalNode node) {
        if (node instanceof DoubleEvalNode) {
            final Double value = ((DoubleEvalNode) node).getValue();
            return EvalUtils.getNumberFuture(value);
        }
        return CompletableFuture.completedFuture(node);
    }
}

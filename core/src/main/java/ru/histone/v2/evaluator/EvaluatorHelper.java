/*
 * Copyright (c) 2017 MegaFon
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

package ru.histone.v2.evaluator;

import ru.histone.v2.evaluator.node.*;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.AsyncUtils;
import ru.histone.v2.utils.ParserUtils;
import ru.histone.v2.utils.RttiUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static ru.histone.v2.utils.ParserUtils.tryDouble;
import static ru.histone.v2.utils.ParserUtils.tryLongNumber;

/**
 * Contains common methods for Evaluator and StdLibrary
 *
 * @author Alexey Nevinsky
 * @since 23-06-2017
 */
public class EvaluatorHelper {

    protected final Converter converter;

    public EvaluatorHelper(Converter converter) {
        this.converter = converter;
    }

    public EvalNode processUnaryMinus(EvalNode n) {
        if (n instanceof LongEvalNode) {
            final Long value = ((LongEvalNode) n).getValue();
            return new LongEvalNode(-value);
        } else if (n instanceof DoubleEvalNode) {
            final Double value = ((DoubleEvalNode) n).getValue();
            return new DoubleEvalNode(-value);
        } else if (n instanceof StringEvalNode) {
            final String stringValue = ((StringEvalNode) n).getValue();
            final Optional<Long> longOptional = tryLongNumber(stringValue);
            if (longOptional.isPresent()) {
                return new LongEvalNode(-longOptional.get());
            }

            final Optional<Double> doubleOptional = tryDouble(stringValue);
            if (doubleOptional.isPresent()) {
                return new DoubleEvalNode(-doubleOptional.get());
            }
        }
        return converter.createEvalNode(null);
    }

    public CompletableFuture<EvalNode> processAddNodes(Context context, List<EvalNode> lr) {
        EvalNode left = lr.get(0);
        EvalNode right = lr.get(1);
        if (!(left.getType() == HistoneType.T_STRING || right.getType() == HistoneType.T_STRING)) {
            final boolean isLeftNumberNode = converter.isNumberNode(left);
            final boolean isRightNumberNode = converter.isNumberNode(right);
            if (isLeftNumberNode && isRightNumberNode) {
                final Double res = getValue(left).orElse(null) + getValue(right).orElse(null);
                return converter.getNumberFuture(res);
            } else if (isLeftNumberNode || isRightNumberNode) {
                return converter.getValue(null);
            }

            if (left.getType() == HistoneType.T_ARRAY && right.getType() == HistoneType.T_ARRAY) {
                final MapEvalNode result = new MapEvalNode(new LinkedHashMap<>());
                result.append((MapEvalNode) left);
                result.append((MapEvalNode) right);
                return completedFuture(result);
            }
        }

        return AsyncUtils.sequence(RttiUtils.callToString(context, left), RttiUtils.callToString(context, right))
                         .thenCompose(futures -> {
                             StringEvalNode l = (StringEvalNode) futures.get(0);
                             StringEvalNode r = (StringEvalNode) futures.get(1);
                             return converter.getValue(l.getValue() + r.getValue());
                         });
    }

    public Optional<Double> getValue(EvalNode node) {
        if (node.getType() == HistoneType.T_STRING) {
            return ParserUtils.tryDouble(((StringEvalNode) node).getValue());
        } else {
            return Optional.of(Double.valueOf(node.getValue() + ""));
        }
    }
}

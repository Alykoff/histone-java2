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

import org.apache.commons.lang3.StringUtils;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.data.HistoneRegex;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.*;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.AsyncUtils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Alexey Nevinsky
 */
public class ToString extends AbstractFunction {
    public static final String NAME = "toString";
    public static final String ARRAY_HISTONE_VIEW_DELIMITER = " ";
    public static final String GLOBAL_OBJECT_STRING_REPRESENTATION = "(Global)";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        return executeHelper(context, args).thenCompose(EvalUtils::getValue);
    }

    private CompletableFuture<String> executeHelper(Context context, List<EvalNode> args) throws FunctionExecutionException {
        //toString called as {{toString}}
        if (args.size() == 0) {
            return CompletableFuture.completedFuture(GLOBAL_OBJECT_STRING_REPRESENTATION);
        }

        final EvalNode node = args.get(0);
        final HistoneType nodeType = node.getType();
        switch (nodeType) {
            case T_UNDEFINED: {
                return CompletableFuture.completedFuture(EmptyEvalNode.HISTONE_VIEW);
            }
            case T_ARRAY: {
                final Map<String, EvalNode> map = ((MapEvalNode) node).getValue();
                return recurseFlattening(context, map);
            }
            case T_NULL: {
                return CompletableFuture.completedFuture(NullEvalNode.HISTONE_VIEW);
            }
            case T_NUMBER: {
                final Number numberValue = (Number) node.getValue();
                if (numberValue instanceof Double) {
                    final Double doubleValue = (Double) numberValue;
                    final String stringValue = EvalUtils.canBeLong(doubleValue)
                            ? String.valueOf(doubleValue.longValue())
                            : processPureDouble(doubleValue);
                    return CompletableFuture.completedFuture(stringValue);
                }
                return CompletableFuture.completedFuture(String.valueOf(numberValue));
            }
            case T_MACRO: {
                return CompletableFuture.completedFuture(EmptyEvalNode.HISTONE_VIEW);
            }
            case T_GLOBAL: {
                return CompletableFuture.completedFuture(GLOBAL_OBJECT_STRING_REPRESENTATION);
            }
            case T_REGEXP: {
                HistoneRegex regex = (HistoneRegex) node.getValue();
                return CompletableFuture.completedFuture(regex.toString());
            }
            default: {
                return CompletableFuture.completedFuture(node.getValue() + "");
            }
        }
    }

    private String processPureDouble(Double doubleValue) {
        final String stringValue = doubleValue.toString();
        final String[] value = stringValue.split("(e|E)");
        if (value.length == 1) {
            return Double.toString(doubleValue);
        }
        final StringBuilder builder = new StringBuilder();
        final boolean isMinus = doubleValue < 0;
        final String sign = isMinus
                ? "-"
                : "";
        String mantissa = value[0]
                .replaceAll("\\.0$", "")
                .replaceAll("^-", "")
                .replace(".", "");
        Long exponent = Long.parseLong(value[1]) + 1;
        if (exponent < 0) {
            builder.append(sign).append("0.");
            while (exponent++ < 0) {
                builder.append('0');
            }
            return builder.append(mantissa).toString();
        } else if (exponent < mantissa.length()) {
            final String[] mantissaArray = mantissa.split("");
            int i = 0;
            while (i != mantissaArray.length) {
                builder.append(mantissaArray[i]);
                exponent--;
                i++;
                if (exponent == 0) {
                    builder.append(".");
                }
            }
            return sign + builder.toString();
        } else {
            exponent -= mantissa.length();
            while (exponent-- > 0) {
                builder.append("0");
            }
            return sign + mantissa + builder.toString();
        }
    }

    private CompletableFuture<String> recurseFlattening(Context context, Map<String, EvalNode> map) {
        final List<CompletableFuture<String>> valuesRawListFuture = new ArrayList<>();
        for (EvalNode rawValue : map.values()) {
            if (rawValue != null) {
                if (rawValue instanceof Map) {
                    final Map<String, EvalNode> value = (Map<String, EvalNode>) rawValue;
                    valuesRawListFuture.add(recurseFlattening(context, value));
                } else {
                    final CompletableFuture<EvalNode> executedValue = execute(
                            context, Collections.singletonList(rawValue)
                    );
                    final CompletableFuture<String> value = executedValue
                            .thenApply(x -> ((StringEvalNode) x).getValue());
                    valuesRawListFuture.add(value);
                }
            }
        }
        final CompletableFuture<List<String>> valuesListFuture = AsyncUtils.sequence(valuesRawListFuture);
        return valuesListFuture.thenApply(x ->
                x.stream()
                        .filter(StringUtils::isNotEmpty)
                        .collect(Collectors.joining(ARRAY_HISTONE_VIEW_DELIMITER))
        );
    }
}

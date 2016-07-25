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
import ru.histone.v2.evaluator.node.RegexEvalNode;
import ru.histone.v2.evaluator.node.StringEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * @author Gali Alykoff
 */
public class StringSplit extends AbstractFunction {
    public static final String NAME = "split";
    public static final String DEFAULT_SEPARATOR = "";
    public static final int ELEMENT_INDEX = 0;
    public static final int SEPARATOR_INDEX = 1;

    private static String[] getSplitArray(List<EvalNode> args, String value) {
        return Optional.of(args)
                .filter(a -> a.size() > SEPARATOR_INDEX)
                .map(a -> a.get(SEPARATOR_INDEX))
                .map(separatorNode -> {
                    final HistoneType type = separatorNode.getType();
                    switch (type) {
                        case T_STRING:
                            final String separator = Pattern.quote(
                                    ((StringEvalNode) separatorNode).getValue()
                            );
                            return value.split(separator);
                        case T_REGEXP:
                            final Pattern pattern = ((RegexEvalNode) separatorNode)
                                    .getValue()
                                    .getPattern();
                            return pattern.split(value);
                        default:
                            return value.split(DEFAULT_SEPARATOR);
                    }
                }).orElse(value.split(DEFAULT_SEPARATOR));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        final String value = ((StringEvalNode) args.get(ELEMENT_INDEX)).getValue();
        final String[] separator = getSplitArray(args, value);
        return CompletableFuture.completedFuture(
                EvalUtils.constructFromList(
                        Arrays.asList(separator)
                )
        );
    }
}

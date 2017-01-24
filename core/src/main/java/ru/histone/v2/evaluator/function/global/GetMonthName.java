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

import org.apache.commons.lang3.tuple.Pair;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.Converter;
import ru.histone.v2.evaluator.function.LocaleFunction;
import ru.histone.v2.evaluator.node.EmptyEvalNode;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.utils.ParserUtils;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

/**
 * @author Alexey Nevinsky
 */
public class GetMonthName extends LocaleFunction {

    private boolean isShort;

    public GetMonthName(Converter converter, boolean isShort) {
        super(converter);
        this.isShort = isShort;
    }

    @Override
    public String getName() {
        return isShort ? "getMonthNameShort" : "getMonthNameLong";
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        return process(context, clearGlobal(args));
    }

    protected CompletableFuture<EvalNode> process(Context context, List<EvalNode> args) {
        final Properties properties = getCurrentProperties(context.getLocale());

        final Optional<Pair<Optional<Integer>, Optional<Integer>>> nameTypeMonth = Optional.of(args)
                .filter(a -> !a.isEmpty())
                .map(a -> {
                    Optional<Integer> month = ParserUtils.tryIntNumber(a.get(0).getValue());
                    Optional<Integer> type = Optional.empty();
                    if (args.size() > 1) {
                        type = ParserUtils.tryIntNumber(a.get(1).getValue());
                    }
                    return Pair.of(month, type);
                });
        if (!nameTypeMonth.isPresent() || !nameTypeMonth.get().getLeft().isPresent()) {
            return CompletableFuture.completedFuture(new EmptyEvalNode());
        }
        int id = nameTypeMonth.get().getLeft().get();
        int type = nameTypeMonth.get().getRight().orElse(0);
        final StringBuilder sb = new StringBuilder("MONTH_NAMES_");

        sb.append(isShort ? "SHORT" : "LONG").append("_").append(id).append("_");
        String defaultValue = properties.getProperty(sb.toString() + "0");
        String value = properties.getProperty(sb.toString() + type);
        return converter.getValue(value != null ? value : defaultValue);
    }
}

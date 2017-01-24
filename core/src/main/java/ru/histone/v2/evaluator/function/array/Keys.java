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
import ru.histone.v2.evaluator.Converter;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.LongEvalNode;
import ru.histone.v2.evaluator.node.StringEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.utils.ParserUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Alexey Nevinsky
 */
public class Keys extends AbstractFunction {

    private boolean isKeys;

    public Keys(Converter converter, boolean isKeys) {
        super(converter);
        this.isKeys = isKeys;
    }

    @Override
    public String getName() {
        return isKeys ? "keys" : "values";
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        Map<String, Object> map = (Map<String, Object>) args.get(0).getValue();
        Collection set = isKeys ? map.keySet() : map.values();

        Map<String, Object> res = new LinkedHashMap<>(set.size());
        int i = 0;
        for (Object key : set) {
            if (ParserUtils.isInt(key.toString())) {
                res.put(i + "", new LongEvalNode(Integer.valueOf(key.toString())));
            } else if (ParserUtils.isStrongString(key)) {
                res.put(i + "", new StringEvalNode((String) key));
            } else {
                res.put(i + "", key);
            }
            i++;
        }

        return converter.getValue(res);
    }
}

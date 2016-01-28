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

import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.Function;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.utils.ParserUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by inv3r on 25/01/16.
 */
public class Keys implements Function {

    private boolean isKeys;

    public Keys(boolean isKeys) {
        this.isKeys = isKeys;
    }

    @Override
    public String getName() {
        return isKeys ? "keys" : "values";
    }

    @Override
    public CompletableFuture<EvalNode> execute(String baseUri, List<EvalNode> args) throws FunctionExecutionException {
        Map<String, Object> map = (Map<String, Object>) args.get(0).getValue();
        Collection set = isKeys ? map.keySet() : map.values();

        Map<String, Object> res = new LinkedHashMap<>(set.size());
        int i = 0;
        for (Object key : set) {
            if (ParserUtils.isInt(key.toString())) {
                res.put(i + "", Integer.valueOf(key.toString()));
            } else {
                res.put(i + "", key);
            }
            i++;
        }

        return EvalUtils.getValue(res);
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isClear() {
        return true;
    }
}

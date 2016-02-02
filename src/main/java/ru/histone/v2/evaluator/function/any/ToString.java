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

import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.Function;
import ru.histone.v2.evaluator.node.EmptyEvalNode;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.evaluator.node.NullEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Created by inv3r on 27/01/16.
 */
public class ToString implements Function {
    @Override
    public String getName() {
        return "toString";
    }

    @Override
    public CompletableFuture<EvalNode> execute(String baseUri, List<EvalNode> args) throws FunctionExecutionException {
        EvalNode node = args.get(0);
        if (node instanceof EmptyEvalNode) {
            return EvalUtils.getValue("");
        } else if (node instanceof MapEvalNode) {
            return EvalUtils.getValue(getMapString((MapEvalNode) node));
        } else if (node instanceof NullEvalNode) {
            return EvalUtils.getValue("null");
        }
        return EvalUtils.getValue(node.getValue() + "");
    }

    private String getMapString(MapEvalNode node) {
        Map<String, Object> map = node.getValue();

        return recurseFlattening(map).stream().map(x -> x + "").collect(Collectors.joining(" "));
    }

    private List<Object> recurseFlattening(Map<String, Object> map) {
        List<Object> res = new ArrayList<>();
        for (Object v : map.values()) {
            if (v != null) {
                if (v instanceof Map) {
                    res.addAll(recurseFlattening((Map<String, Object>) v));
                } else {
                    res.add(v);
                }
            }
        }
        return res;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isClear() {
        return false;
    }
}

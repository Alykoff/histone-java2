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

import org.apache.commons.lang.StringUtils;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.Evaluator;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.resource.HistoneResourceLoader;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.parser.Parser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * @author Alexey Nevinsky
 */
public class LoadJson extends LoadText {

    public LoadJson(Executor executor, HistoneResourceLoader loader, Evaluator evaluator, Parser parser) {
        super(executor, loader, evaluator, parser);
    }

    @Override
    public String getName() {
        return "loadJSON";
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        return super.execute(context, clearGlobal(args))
                .thenApply(res -> {
                    String str = (String) res.getValue();
                    Object json;
                    if (StringUtils.isEmpty(str)) {
                        return EvalUtils.constructFromObject(null);
                    } else if (StringUtils.isNotEmpty(str)) {
                        json = fromJSON(str);
                    } else {
                        json = new LinkedHashMap<String, EvalNode>();
                    }

                    return EvalUtils.constructFromObject(json);
                })
                .exceptionally(ex -> {
                    logger.error(ex.getMessage(), ex);
                    return EvalUtils.createEvalNode(null);
                });
    }
}

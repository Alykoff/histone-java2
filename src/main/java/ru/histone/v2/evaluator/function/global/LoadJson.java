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

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.LongEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * @author alexey.nevinsky
 */
public class LoadJson extends AbstractFunction {

    public LoadJson(Executor executor) {
        super(executor);
    }

    @Override
    public String getName() {
        return "loadJson";
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        return CompletableFuture
                .completedFuture(null)
                .thenComposeAsync(x -> {
                    System.out.println("Started on " + new Date() + "ms");
                    LongEvalNode node = (LongEvalNode) args.get(0);
                    try {
                        Thread.sleep(node.getValue());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Wake up after " + node.getValue() + "ms");
                    return EvalUtils.getValue(node.getValue());
                }, executor);
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public boolean isClear() {
        return false;
    }
}

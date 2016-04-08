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
import ru.histone.v2.evaluator.Evaluator;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.resource.HistoneResourceLoader;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.parser.Parser;
import ru.histone.v2.parser.node.ExpAstNode;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.IOUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Function loads histone template from specified file and evaluating it.
 *
 * @author alexey.nevinsky
 */
public class Require extends AbstractFunction {
    public Require(Executor executor, HistoneResourceLoader resourceLoader) {
        super(executor, resourceLoader);
    }

    @Override
    public String getName() {
        return "require";
    }

    @Override
    public CompletableFuture<EvalNode> execute(final Context context, List<EvalNode> args) throws FunctionExecutionException {
        return doExecute(context, clearGlobal(args));
    }

    private CompletableFuture<EvalNode> doExecute(Context context, List<EvalNode> args) {
        checkMinArgsLength(args, 1);
        checkMaxArgsLength(args, 2);
        checkTypes(args.get(0), 0, Collections.singletonList(HistoneType.T_STRING), Collections.singletonList(String.class));

        final String url = getValue(args, 0);

        final Object params = getValue(args, 1, null);

        return resourceLoader.load(url, context.getBaseUri(), Collections.emptyMap())
                .thenCompose(res -> {
                    String template = IOUtils.readStringFromResource(res, url);

                    //todo get parser and evaluator from context
                    Parser p = new Parser();
                    ExpAstNode root = p.process(template, context.getBaseUri());
                    Evaluator evaluator = new Evaluator();

                    Context macroCtx = createCtx(context, params);

                    CompletableFuture<EvalNode> nodeFuture = evaluator.evaluateNode(root, macroCtx); // we evaluated template and add all macros and variables to context

                    EvalNode rNode = nodeFuture.join();
                    return CompletableFuture.completedFuture(rNode.clearReturned());
                });
    }

    private Context createCtx(Context baseContext, Object params) {
        Context macroCtx = baseContext.cloneEmpty();

        if (params == null) {
            return macroCtx;
        }

        EvalNode node = EvalUtils.constructFromObject(params);

        if (node.getType() != HistoneType.T_ARRAY) {
            macroCtx.getThisVars().put("this", CompletableFuture.completedFuture(node));
        } else {
            macroCtx.getThisVars().put("this", CompletableFuture.completedFuture(node));
        }
        return macroCtx;
    }
}

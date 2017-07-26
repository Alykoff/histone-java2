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
import ru.histone.v2.evaluator.Converter;
import ru.histone.v2.evaluator.Evaluator;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.resource.HistoneResourceLoader;
import ru.histone.v2.evaluator.resource.Resource;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.exceptions.StopExecutionException;
import ru.histone.v2.parser.Parser;
import ru.histone.v2.parser.SsaOptimizer;
import ru.histone.v2.parser.node.ExpAstNode;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.AstJsonProcessor;
import ru.histone.v2.utils.IOUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static ru.histone.v2.utils.ParserUtils.isAst;

/**
 * Function loads histone template from specified file and evaluating it.
 *
 * @author Alexey Nevinsky
 */
public class Require extends AbstractFunction {
    public Require(Executor executor, HistoneResourceLoader resourceLoader, Evaluator evaluator, Parser parser, Converter converter) {
        super(executor, resourceLoader, evaluator, parser, converter);
    }

    @Override
    public String getName() {
        return "require";
    }

    @Override
    public CompletableFuture<EvalNode> execute(final Context context, List<EvalNode> args) throws FunctionExecutionException {
        return doExecute(context, clearGlobal(args));
    }

    protected CompletableFuture<EvalNode> doExecute(Context context, List<EvalNode> args) {
        checkMinArgsLength(args, 1);
        checkMaxArgsLength(args, 2);
        checkTypes(args.get(0), 0, Collections.singletonList(HistoneType.T_STRING), Collections.singletonList(String.class));

        final String url = getValue(args, 0);

        final Object params = getValue(args, 1, null);

        return resourceLoader.load(context, url, context.getBaseUri(), Collections.emptyMap())
                             .thenCompose(res -> {
                                 String template = IOUtils.readStringFromResource(res, url);

                                 ExpAstNode root = processTemplate(template, res);

                                 Context macroCtx = createCtx(context, res.getBaseHref(), params);

                                 CompletableFuture<EvalNode> nodeFuture = evaluator.evaluateNode(root, macroCtx); // we evaluated template and add all macros and variables to context

                                 return nodeFuture.thenApply(EvalNode::clearReturned);
                             })
                             .exceptionally(e -> {
                                 if (e.getCause() instanceof StopExecutionException) {
                                     throw (StopExecutionException) e.getCause();
                                 }
                                 logger.error(e.getMessage(), e);
                                 return converter.createEvalNode(null);
                             });
    }

    protected ExpAstNode processTemplate(String template, Resource res) {
        if (isAst(template)) {
            try {
                ExpAstNode root = AstJsonProcessor.read(template);
                SsaOptimizer optimizer = new SsaOptimizer();
                optimizer.process(root);
                return root;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return parser.process(template, res.getBaseHref());
    }
}

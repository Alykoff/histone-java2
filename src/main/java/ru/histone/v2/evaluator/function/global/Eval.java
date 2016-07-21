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
import ru.histone.v2.parser.SsaOptimizer;
import ru.histone.v2.parser.node.AstNode;
import ru.histone.v2.parser.node.ExpAstNode;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.AstJsonProcessor;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * @author Alexey Nevinsky
 */
public class Eval extends AbstractFunction {

    public Eval(Executor executor, HistoneResourceLoader resourceLoader, Evaluator evaluator, Parser parser) {
        super(executor, resourceLoader, evaluator, parser);
    }

    @Override
    public String getName() {
        return "eval";
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        return doExecute(context, clearGlobal(args));
    }

    private CompletableFuture<EvalNode> doExecute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        EvalNode templateNode = args.get(0);
        if (templateNode.getType() != HistoneType.T_STRING) {
            return EvalUtils.getValue(null);
        }

        String template = (String) templateNode.getValue();

        EvalNode params = EvalUtils.createEvalNode(null);
        if (args.size() >= 2) {
            params = args.get(1);
        }

        String baseUri = context.getBaseUri();
        if (args.size() > 2 && args.get(2).getType() == HistoneType.T_STRING) {
            baseUri = (String) args.get(2).getValue();
        }

        AstNode ast = processTemplate(template, baseUri);

        Context evalCtx = createCtx(context, baseUri, params);

        return evaluator.evaluateNode(ast, evalCtx);
    }

    protected ExpAstNode processTemplate(String template, String baseURI) {
        if (EvalUtils.isAst(template)) {
            try {
                ExpAstNode root = AstJsonProcessor.read(template);
                SsaOptimizer optimizer = new SsaOptimizer();
                optimizer.process(root);
                return root;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return parser.process(template, baseURI);
    }
}

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

package ru.histone.v2.evaluator.function.macro;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.Evaluator;
import ru.histone.v2.evaluator.data.HistoneMacro;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EmptyEvalNode;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MacroEvalNode;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.parser.node.AstNode;
import ru.histone.v2.utils.RttiUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author gali.alykoff
 */
public class MacroCall extends AbstractFunction implements Serializable {
    public final static String NAME = "call";
    public static final int MACRO_NODE_INDEX = 0;
    public static final boolean IS_UNWRAP_ARGS_ARRAYS = true;

    public static CompletableFuture<EvalNode> staticExecute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        final HistoneMacro histoneMacro = getMacro(args);
        return processMacro(args, histoneMacro, MACRO_NODE_INDEX, IS_UNWRAP_ARGS_ARRAYS);
    }

    public static HistoneMacro getMacro(List<EvalNode> args) {
        final MacroEvalNode macroNode = (MacroEvalNode) args.get(MACRO_NODE_INDEX);
        return macroNode.getValue();
    }

    public static CompletableFuture<EvalNode> processMacro(
            List<EvalNode> args, HistoneMacro histoneMacro,
            int startIndex,
            boolean isUnwrapArgsArrays
    ) {
        final AstNode body = histoneMacro.getBody();
        final List<String> namesOfVars = histoneMacro.getArgs();
        final Evaluator evaluator = histoneMacro.getEvaluator();
        final Context contextInner = histoneMacro.getContext();

        final List<EvalNode> bindArgs = histoneMacro.getBindArgs();

        final List<EvalNode> paramsInput = getParams(args, startIndex, isUnwrapArgsArrays);
        final List<EvalNode> params = new ArrayList<>(bindArgs.size() + paramsInput.size());
        params.addAll(bindArgs);
        params.addAll(paramsInput);

        final Context currentContext = contextInner.createNew();
        for (int i = 0; i < namesOfVars.size(); i++) {
            final String argName = namesOfVars.get(i);
            final EvalNode param = (i < params.size())
                    ? params.get(i)
                    : EmptyEvalNode.INSTANCE;
            currentContext.put(argName, CompletableFuture.completedFuture(param));
        }
        return evaluator.evaluateNode(body, currentContext).thenCompose(res -> {
            if (res.isReturn()) {
                return CompletableFuture.completedFuture(res);
            } else {
                return RttiUtils.callToString(contextInner, res);
            }
        });
    }

    private static List<EvalNode> getParams(List<EvalNode> args, int startIndex, boolean isUnwrapArgsArrays) {
        final List<EvalNode> params = new ArrayList<>();
        for (int i = startIndex + 1; i < args.size(); i++) {
            final EvalNode rawNode = args.get(i);
            if (isUnwrapArgsArrays && rawNode instanceof MapEvalNode) {
                final MapEvalNode node = (MapEvalNode) rawNode;
                final List<EvalNode> innerArgs = node.getValue()
                        .values()
                        .stream()
                        .map(EvalUtils::createEvalNode)
                        .collect(Collectors.toList());
                params.addAll(innerArgs);
            } else {
                params.add(rawNode);
            }
        }
        return params;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(
            Context context, List<EvalNode> args
    ) throws FunctionExecutionException {
        return staticExecute(context, args);
    }
}

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author gali.alykoff
 */
public class MacroCall extends AbstractFunction implements Serializable {
    public final static String NAME = "call";
    public static final int MACRO_NODE_INDEX = 0;
    public static final int SUB_MACRO_NODE_INDEX = 1;

    public static CompletableFuture<EvalNode> staticExecute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        final MacroEvalNode macroNode = (MacroEvalNode) args.get(MACRO_NODE_INDEX);
        final HistoneMacro histoneMacro = getMainMacro(macroNode, args);
        if (histoneMacro == null) {
            return EmptyEvalNode.FUTURE_INSTANCE;
        }

        final AstNode body = histoneMacro.getBody();
        final List<String> namesOfVars = histoneMacro.getArgs();
        final Evaluator evaluator = histoneMacro.getEvaluator();
        final Context contextInner = histoneMacro.getContext();

        final List<EvalNode> bindArgs = histoneMacro.getBindArgs();

        final List<EvalNode> paramsInput = getParams(args, macroNode.isRequired());
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
        return evaluator.evaluateNode(body, currentContext);
    }

    private static HistoneMacro getMainMacro(MacroEvalNode macroNode, List<EvalNode> args) {
        if (macroNode.isRequired()) {
            if (args.size() > 1) {
                String macroName = (String) args.get(1).getValue();
                return macroNode.getValue().get(macroName);
            }
            return null;
        }

        return macroNode.getMacro();
    }

    private static List<EvalNode> getParams(List<EvalNode> args, boolean isRequired) {
        final List<EvalNode> params = new ArrayList<>();
        for (int i = 0; i < args.size(); i++) {
            if (i == MACRO_NODE_INDEX) {
                continue;
            } else if (i == SUB_MACRO_NODE_INDEX && isRequired) {
                continue;
            }

            final EvalNode rawNode = args.get(i);
            if (rawNode instanceof MapEvalNode) {
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
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        return staticExecute(context, args);
    }
}

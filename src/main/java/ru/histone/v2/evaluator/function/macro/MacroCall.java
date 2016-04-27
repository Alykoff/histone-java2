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

import ru.histone.v2.Constants;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.Evaluator;
import ru.histone.v2.evaluator.data.HistoneMacro;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MacroEvalNode;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.evaluator.resource.HistoneResourceLoader;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.parser.Parser;
import ru.histone.v2.parser.node.AstNode;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.RttiUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @author Gali Alykoff
 */
public class MacroCall extends AbstractFunction implements Serializable {
    public final static String NAME = "call";
    private static final int MACRO_NODE_INDEX = 0;

    public MacroCall(Executor executor, HistoneResourceLoader resourceLoader, Evaluator evaluator, Parser parser) {
        super(executor, resourceLoader, evaluator, parser);
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        final HistoneMacro histoneMacro = getMacro(args);

        //if result was set, we return it immediately
        if (histoneMacro.getResult() != null) {
            return CompletableFuture.completedFuture(histoneMacro.getResult());
        }

        final AstNode body = histoneMacro.getBody();
        final List<String> namesOfVars = histoneMacro.getArgs();
        final Context contextInner = histoneMacro.getContext();
        final Map<String, CompletableFuture<EvalNode>> defaultsVars = histoneMacro.getDefaultValues();
        final List<EvalNode> bindArgs = histoneMacro.getBindArgs();
        final List<EvalNode> paramsInput = getParams(args);
        final List<EvalNode> params = new ArrayList<>(bindArgs.size() + paramsInput.size());
        params.addAll(bindArgs);
        params.addAll(paramsInput);

        final Context currentContext = contextInner.createNew();
        final List<CompletableFuture<EvalNode>> argumentsFutures = new ArrayList<>();
        for (int i = 0; i < namesOfVars.size(); i++) {
            final String argName = namesOfVars.get(i);
            final CompletableFuture<EvalNode> param;
            if (i < params.size() && params.get(i).getType() != HistoneType.T_UNDEFINED) {
                param = CompletableFuture.completedFuture(params.get(i));
            } else if (defaultsVars.containsKey(argName)) {
                param = defaultsVars.get(argName);
            } else {
                param = EvalUtils.getValue(null);
            }
            argumentsFutures.add(param);
            currentContext.put(argName, param);
        }
        final CompletableFuture<EvalNode> selfObject = createSelfObject(new MacroEvalNode(histoneMacro), context.getBaseUri(), params);
        currentContext.put(Constants.SELF_CONTEXT_NAME, selfObject);
        return evaluator.evaluateNode(body, currentContext).thenCompose(res -> {
            if (res.isReturn()) {
                return CompletableFuture.completedFuture(res.clearReturned());
            } else {
                return RttiUtils.callToString(contextInner, res);
            }
        });
    }

    private static HistoneMacro getMacro(List<EvalNode> args) {
        final MacroEvalNode macroNode = (MacroEvalNode) args.get(MACRO_NODE_INDEX);
        return macroNode.getValue();
    }

    private static CompletableFuture<EvalNode> createSelfObject(MacroEvalNode macro, String baseURI, List<EvalNode> args) {
        Map<String, EvalNode> res = new LinkedHashMap<>();
        res.put(Constants.SELF_CONTEXT_CALLEE, macro);
        res.put(Constants.SELF_CONTEXT_CALLER, EvalUtils.createEvalNode(baseURI));
        res.put(Constants.SELF_CONTEXT_ARGUMENTS, EvalUtils.constructFromObject(args));

        return EvalUtils.getValue(res);
    }

    private static List<EvalNode> getParams(List<EvalNode> args) {
        final List<EvalNode> params = new ArrayList<>();
        int start = 1;
        boolean isUnwrapArgsArrays = true;
        for (int i = 0; i < args.size(); i++) {
            if (args.get(i).getType() == HistoneType.T_BOOLEAN) {
                start = i + 1;
                isUnwrapArgsArrays = (boolean) args.get(i).getValue();
                break;
            }
        }

        for (int i = start; i < args.size(); i++) {
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
}

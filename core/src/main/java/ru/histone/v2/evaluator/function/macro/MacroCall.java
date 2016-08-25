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
import ru.histone.v2.parser.node.ExpAstNode;
import ru.histone.v2.parser.node.StringAstNode;
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
    private static final int METHOD_NAME_INDEX_IN_WRAPPED_MACRO_BODY = 1;

    public MacroCall(Executor executor, HistoneResourceLoader resourceLoader, Evaluator evaluator, Parser parser) {
        super(executor, resourceLoader, evaluator, parser);
    }

    private HistoneMacro getMacro(List<EvalNode> args) {
        final MacroEvalNode macroNode = (MacroEvalNode) args.get(MACRO_NODE_INDEX);
        return macroNode.getValue();
    }

    protected CompletableFuture<EvalNode> createSelfObject(EvalNode macro, String baseURI, List<EvalNode> args) {
        Map<String, EvalNode> res = new LinkedHashMap<>();
        res.put(Constants.SELF_CONTEXT_CALLEE, macro);
        res.put(Constants.SELF_CONTEXT_CALLER, EvalUtils.createEvalNode(baseURI));
        res.put(Constants.SELF_CONTEXT_ARGUMENTS, EvalUtils.constructFromObject(args));

        return EvalUtils.getValue(res);
    }

    protected static List<EvalNode> getParams(List<EvalNode> args) {
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
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        final HistoneMacro histoneMacro = getMacro(args);

        //if result was set and macros is not wrapper, we return it immediately
        if (histoneMacro.getResult() != null && histoneMacro.getWrappingType() == HistoneMacro.WrappingType.NONE) {
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

        if (histoneMacro.getWrappingType() == HistoneMacro.WrappingType.GLOBAL) {
            return callWrappedGlobalFunction(context, paramsInput, body);
        } else if (histoneMacro.getWrappingType() == HistoneMacro.WrappingType.VALUE) {
            return callWrappedValueFunction(context, paramsInput, (ExpAstNode) body, histoneMacro.getResult());
        }

        final Context currentContext = contextInner.createNew();
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
            currentContext.put(argName, param);
        }

        final CompletableFuture<EvalNode> selfObject = createSelfObject(
                new MacroEvalNode(histoneMacro), context.getBaseUri(), params
        );
        currentContext.put("0", selfObject);
        return evaluator.evaluateNode(body, currentContext).thenCompose(res -> {
            if (res.isReturn()) {
                return CompletableFuture.completedFuture(res.clearReturned());
            } else {
                return RttiUtils.callToString(contextInner, res);
            }
        });
    }

    // if macro is wrapped global function:
    // body := [AST_CALL, [AST_GLOBAL], METHOD_NAME],
    // body := [22, [4], METHOD_NAME]
    protected CompletableFuture<EvalNode> callWrappedGlobalFunction(Context context, List<EvalNode> args, AstNode body) {
        final ExpAstNode bodyExt = (ExpAstNode) body;
        final StringAstNode methodNameNode = bodyExt.getNode(METHOD_NAME_INDEX_IN_WRAPPED_MACRO_BODY);
        final String methodName = methodNameNode.getValue();
        return context.call(methodName, args);
    }

    protected CompletableFuture<EvalNode> callWrappedValueFunction(Context context, List<EvalNode> args, ExpAstNode body,
                                                                   EvalNode result) {
        final String functionName = ((StringAstNode) body.getNode(METHOD_NAME_INDEX_IN_WRAPPED_MACRO_BODY)).getValue();
        List<EvalNode> arguments = new ArrayList<>();
        arguments.add(result);
        arguments.addAll(args);
        return context.call(result, functionName, arguments);
    }

    @Override
    public String getName() {
        return NAME;
    }
}

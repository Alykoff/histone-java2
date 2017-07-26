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

package ru.histone.v2.java_compiler.java_evaluator.function;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.Converter;
import ru.histone.v2.evaluator.Evaluator;
import ru.histone.v2.evaluator.data.HistoneMacro;
import ru.histone.v2.evaluator.function.macro.MacroCall;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MacroEvalNode;
import ru.histone.v2.evaluator.resource.HistoneResourceLoader;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.java_compiler.bcompiler.data.MacroFunction;
import ru.histone.v2.parser.Parser;
import ru.histone.v2.rtti.HistoneType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * @author Alexey Nevinsky
 */
public class JavaMacroCall extends MacroCall implements Serializable {
    public JavaMacroCall(Executor executor, HistoneResourceLoader resourceLoader, Evaluator evaluator, Parser parser,
                         Converter converter) {
        super(executor, resourceLoader, evaluator, parser, converter);
    }

    @Override
    public String getName() {
        return "call";
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        MacroEvalNode node = (MacroEvalNode) args.get(0);

        HistoneMacro histoneMacro = node.getValue();

        List<CompletableFuture<EvalNode>> macroArgs = new ArrayList<>();


        //if result was set and macros is not wrapper, we return it immediately
        if (histoneMacro.getResult() != null && histoneMacro.getWrappingType() == HistoneMacro.WrappingType.NONE) {
            return CompletableFuture.completedFuture(histoneMacro.getResult());
        }

        final List<String> namesOfVars = histoneMacro.getArgs();
        final Map<String, CompletableFuture<EvalNode>> defaultsVars = histoneMacro.getDefaultValues();
        final List<EvalNode> bindArgs = histoneMacro.getBindArgs();
        final List<EvalNode> paramsInput = getParams(args);
        final List<EvalNode> params = new ArrayList<>(bindArgs.size() + paramsInput.size());
        params.addAll(bindArgs);
        params.addAll(paramsInput);

        if (histoneMacro.getWrappingType() == HistoneMacro.WrappingType.GLOBAL) {
            return callWrappedGlobalFunction(context, paramsInput, histoneMacro.getBody());
        } else if (histoneMacro.getWrappingType() == HistoneMacro.WrappingType.VALUE) {
            return callWrappedValueFunction(context, paramsInput, histoneMacro.getBody(), histoneMacro.getResult());
        }

        macroArgs.add(createSelfObject(
                node, context.getBaseUri(), params
        ));

//        final Context currentContext = contextInner.createNew();
        for (int i = 0; i < histoneMacro.getArgs().size(); i++) {
            final String argName = namesOfVars.get(i);
            final CompletableFuture<EvalNode> param;
            if (i < params.size() && params.get(i).getType() != HistoneType.T_UNDEFINED) {
                param = CompletableFuture.completedFuture(params.get(i));
            } else if (defaultsVars.containsKey(argName)) {
                param = defaultsVars.get(argName);
            } else {
                param = converter.getValue(null);
            }
            macroArgs.add(param);
//            currentContext.put(argName, param);
        }

        return ((MacroFunction) histoneMacro.getBody()).apply(macroArgs);
    }
}

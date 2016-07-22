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

package ru.histone.v2.utils;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.function.any.ToBoolean;
import ru.histone.v2.evaluator.function.any.ToJson;
import ru.histone.v2.evaluator.function.any.ToNumber;
import ru.histone.v2.evaluator.function.any.ToString;
import ru.histone.v2.evaluator.function.macro.MacroBind;
import ru.histone.v2.evaluator.node.BooleanEvalNode;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MacroEvalNode;
import ru.histone.v2.evaluator.node.StringEvalNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Gali Alykoff
 */
public class RttiUtils implements Serializable {
    public static CompletableFuture<EvalNode> callToString(Context context, EvalNode node) {
        return context.call(ToString.NAME, Collections.singletonList(node));
    }

    public static CompletableFuture<String> callToStringResult(Context context, EvalNode node) {
        return context.call(ToString.NAME, Collections.singletonList(node))
                .thenApply(n -> ((StringEvalNode) n).getValue());
    }

    public static CompletableFuture<EvalNode> callToJSON(Context context, EvalNode node) {
        return context.call(ToJson.NAME, Collections.singletonList(node));
    }

    public static CompletableFuture<EvalNode> callToNumber(Context context, EvalNode node) {
        return context.call(node, ToNumber.NAME, Collections.singletonList(node));
    }

    public static CompletableFuture<EvalNode> callToBoolean(Context context, EvalNode node) {
        return context.call(ToBoolean.NAME, Collections.singletonList(node));
    }

    public static CompletableFuture<Boolean> callToBooleanResult(Context context, EvalNode node) {
        return context.call(ToBoolean.NAME, Collections.singletonList(node))
                .thenApply(toBooleanResult -> ((BooleanEvalNode) toBooleanResult).getValue());
    }

    public static CompletableFuture<EvalNode> callMacro(
            Context context, EvalNode macroNode, List<EvalNode> argsNodes
    ) {
        final List<EvalNode> macroArgs = new ArrayList<>();
        macroArgs.add(macroNode);
        macroArgs.addAll(argsNodes);
        return context.macroCall(macroArgs);
    }

    public static CompletableFuture<EvalNode> callMacro(Context context, EvalNode macroNode, EvalNode... argsNodes) {
        final List<EvalNode> macroArgs = new ArrayList<>();
        macroArgs.add(macroNode);
        Collections.addAll(macroArgs, argsNodes);
        return context.macroCall(macroArgs);
    }

    public static CompletableFuture<EvalNode> callMacroBind(
            Context context, MacroEvalNode macroNode, List<EvalNode> argsNodes
    ) {
        final List<EvalNode> macroArgs = new ArrayList<>();
        macroArgs.add(macroNode);
        macroArgs.addAll(argsNodes);
        return context.call(macroNode, MacroBind.NAME, macroArgs);
    }
}

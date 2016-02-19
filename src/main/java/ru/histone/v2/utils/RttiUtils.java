package ru.histone.v2.utils;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.function.any.*;
import ru.histone.v2.evaluator.function.macro.MacroBind;
import ru.histone.v2.evaluator.function.macro.MacroCall;
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
 * @author gali.alykoff on 11/02/16.
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
        return context.call(ToNumber.NAME, Collections.singletonList(node));
    }

    public static CompletableFuture<EvalNode> callToBoolean(Context context, EvalNode node) {
        return context.call(ToBoolean.NAME, Collections.singletonList(node));
    }

    public static CompletableFuture<Boolean> callToBooleanResult(Context context, EvalNode node) {
        return context.call(ToBoolean.NAME, Collections.singletonList(node))
                .thenApply(toBooleanResult -> ((BooleanEvalNode)toBooleanResult).getValue());
    }

    public static CompletableFuture<EvalNode> callMacro(
            Context context, EvalNode macroNode, List<EvalNode> argsNodes
    ) {
        final List<EvalNode> macroArgs = new ArrayList<>();
        macroArgs.add(macroNode);
        macroArgs.addAll(argsNodes);
        return context.call(macroNode, MacroCall.NAME, macroArgs);
    }

    public static CompletableFuture<EvalNode> callMacro(
            Context context, EvalNode macroNode, EvalNode... argsNodes
    ) {
        final List<EvalNode> macroArgs = new ArrayList<>();
        macroArgs.add(macroNode);
        Collections.addAll(macroArgs, argsNodes);
        return context.call(macroNode, MacroCall.NAME, macroArgs);
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

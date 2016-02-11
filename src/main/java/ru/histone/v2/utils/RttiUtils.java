package ru.histone.v2.utils;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.function.any.ToString;
import ru.histone.v2.evaluator.node.EvalNode;

import java.io.Serializable;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * @author gali.alykoff on 11/02/16.
 */
public class RttiUtils implements Serializable {
    public static CompletableFuture<EvalNode> callToString(Context context, EvalNode node) {
        return context.call(ToString.NAME, Collections.singletonList(node));
    }
}

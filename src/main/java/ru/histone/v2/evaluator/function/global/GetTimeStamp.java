package ru.histone.v2.evaluator.function.global;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author gali.alykoff on 17/05/16.
 */
public class GetTimeStamp extends AbstractFunction {
    public static final String NAME = "getTimeStamp";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        return doExecute(clearGlobal(args));
    }

    private CompletableFuture<EvalNode> doExecute(List<EvalNode> args) {
        final long timestamp = System.currentTimeMillis();
        return EvalUtils.getValue(timestamp);
    }
}
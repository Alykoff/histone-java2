package ru.histone.v2.evaluator.function;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ThrowExceptionFunction extends AbstractFunction {
    @Override
    public String getName() {
        return "throwExceptionFunction";
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        throw new FunctionExecutionException("Exception thrown");
    }
}

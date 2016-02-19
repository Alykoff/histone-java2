package ru.histone.v2.evaluator.function.string;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.StringEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author gali.alykoff on 19/02/16.
 */
public class StringStrip extends AbstractFunction {
    public static final String NAME = "strip";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        String value = ((StringEvalNode) args.get(0)).getValue();
        return CompletableFuture.completedFuture(new StringEvalNode(value.trim()));
    }
}

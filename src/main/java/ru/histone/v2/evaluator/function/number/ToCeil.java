package ru.histone.v2.evaluator.function.number;

import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.FloatEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by inv3r on 28/01/16.
 */
public class ToCeil extends AbstractFunction {
    @Override
    public String getName() {
        return "toCeil";
    }

    @Override
    public CompletableFuture<EvalNode> execute(String baseUri, List<EvalNode> args) throws FunctionExecutionException {
        if (args.get(0) instanceof FloatEvalNode) {
            Float v = (float) Math.ceil(((FloatEvalNode) args.get(0)).getValue());
            if (v % 1 == 0 && v <= Long.MAX_VALUE) {
                return EvalUtils.getValue(v.longValue());
            }
            return EvalUtils.getValue(v);
        }
        return CompletableFuture.completedFuture(args.get(0));
    }
}

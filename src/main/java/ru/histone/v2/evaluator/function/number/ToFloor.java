package ru.histone.v2.evaluator.function.number;

import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.FloatEvalNode;
import ru.histone.v2.evaluator.node.LongEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by inv3r on 28/01/16.
 */
public class ToFloor extends AbstractFunction {
    @Override
    public String getName() {
        return "toFloor";
    }

    @Override
    public CompletableFuture<EvalNode> execute(String baseUri, List<EvalNode> args) throws FunctionExecutionException {
        if (args.get(0) instanceof LongEvalNode) {
            return CompletableFuture.completedFuture(args.get(0));
        } else {
            float value = ((FloatEvalNode) args.get(0)).getValue();
            return EvalUtils.getValue((long) Math.floor(value));
        }
    }
}

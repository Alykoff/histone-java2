package ru.histone.v2.evaluator.function.any;

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
public class IsInt extends AbstractFunction {
    @Override
    public String getName() {
        return "isInt";
    }

    @Override
    public CompletableFuture<EvalNode> execute(String baseUri, List<EvalNode> args) throws FunctionExecutionException {
        EvalNode node = args.get(0);
        if (node instanceof LongEvalNode) {
            return EvalUtils.getValue(true);
        } else if (node instanceof FloatEvalNode) {
            if (((FloatEvalNode) node).getValue() % 1 == 0 && ((FloatEvalNode) node).getValue() <= Long.MAX_VALUE) {
                return EvalUtils.getValue(true);
            }
        }
        return EvalUtils.getValue(false);
    }
}

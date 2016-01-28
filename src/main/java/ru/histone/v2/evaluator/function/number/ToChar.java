package ru.histone.v2.evaluator.function.number;

import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.FloatEvalNode;
import ru.histone.v2.evaluator.node.LongEvalNode;
import ru.histone.v2.evaluator.node.StringEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by inv3r on 28/01/16.
 */
public class ToChar extends AbstractFunction {
    @Override
    public String getName() {
        return "toChar";
    }

    @Override
    public CompletableFuture<EvalNode> execute(String baseUri, List<EvalNode> args) throws FunctionExecutionException {
        final int value;
        if (args.get(0) instanceof LongEvalNode) {
            value = ((LongEvalNode) args.get(0)).getValue().intValue();
        } else {
            value = ((FloatEvalNode) args.get(0)).getValue().intValue();
        }
        char ch = (char) value;
        return CompletableFuture.completedFuture(new StringEvalNode(ch + ""));
    }
}

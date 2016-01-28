package ru.histone.v2.evaluator.function.any;

import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EmptyEvalNode;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.StringEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by inv3r on 28/01/16.
 */
public class ToNumber extends AbstractFunction {
    @Override
    public String getName() {
        return "toNumber";
    }

    @Override
    public CompletableFuture<EvalNode> execute(String baseUri, List<EvalNode> args) throws FunctionExecutionException {
        EvalNode node = args.get(0);
        if (EvalUtils.isNumberNode(node)) {
            return CompletableFuture.completedFuture(node);
        } else if (node instanceof StringEvalNode && EvalUtils.isNumeric((StringEvalNode) node)) {
            Float v = Float.parseFloat(((StringEvalNode) node).getValue());
            if (v % 1 == 0 && v <= Long.MAX_VALUE) {
                return EvalUtils.getValue(v.longValue());
            } else {
                return EvalUtils.getValue(v);
            }
        } else if (args.size() > 1) {
            return CompletableFuture.completedFuture(args.get(1));
        }
        return CompletableFuture.completedFuture(EmptyEvalNode.INSTANCE);
    }
}

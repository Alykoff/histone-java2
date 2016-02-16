package ru.histone.v2.evaluator.function.any;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.BooleanEvalNode;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author gali.alykoff on 16/02/16.
 */
public class IsMacro extends AbstractFunction {
    public static final String NAME = "isMacro";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        final EvalNode node = args.get(0);
        return CompletableFuture.completedFuture(
                new BooleanEvalNode(node.getType() == HistoneType.T_MACRO)
        );
    }
}

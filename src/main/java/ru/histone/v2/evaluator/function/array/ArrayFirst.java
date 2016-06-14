package ru.histone.v2.evaluator.function.array;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Aleksander Melnichnikov
 */
public class ArrayFirst extends ArrayIndexAware {
    private static final String NAME = "first";
    private static final int ARRAY_INDEX = 0;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        return getNodeByIndex((MapEvalNode) args.get(ARRAY_INDEX), 0);
    }
}

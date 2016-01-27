package ru.histone.v2.evaluator.function.array;

import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.Function;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by inv3r on 25/01/16.
 */
public class Size implements Function {
    @Override
    public String getName() {
        return "size";
    }

    @Override
    public CompletableFuture<EvalNode> execute(List<EvalNode> args) throws FunctionExecutionException {
        Map<String, Object> map = (Map<String, Object>) args.get(0).getValue();
        return EvalUtils.getValue(map.size());
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isClear() {
        return true;
    }
}

package ru.histone.v2.evaluator.function.global;

import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.Function;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by inv3r on 22/01/16.
 */
public class Range implements Function {

    public static final String NAME = "range";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(List<EvalNode> args) throws FunctionExecutionException {
        if (args.size() != 2) {
            throw new IllegalArgumentException("Wrong count of arguments. Actual is " + args.size() + ", but expected is 2");
        }
        long from = (long) args.get(0).getValue();
        long to = (long) args.get(1).getValue();

        Map<String, Object> res = new LinkedHashMap<>();
        for (int i = 0; i < to - from + 1; i++) {
            res.put(i + "", from + i);
        }
        return EvalUtils.getValue(res);
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

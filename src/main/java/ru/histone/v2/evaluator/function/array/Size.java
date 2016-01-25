package ru.histone.v2.evaluator.function.array;

import ru.histone.v2.evaluator.Function;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.LongEvalNode;
import ru.histone.v2.exceptions.GlobalFunctionExecutionException;

import java.util.List;
import java.util.Map;

/**
 * Created by inv3r on 25/01/16.
 */
public class Size implements Function {
    @Override
    public String getName() {
        return "size";
    }

    @Override
    public EvalNode execute(List<EvalNode> args) throws GlobalFunctionExecutionException {
        Map<String, Object> map = (Map<String, Object>) args.get(0).getValue();
        return new LongEvalNode((long) map.size());
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

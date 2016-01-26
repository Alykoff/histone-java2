package ru.histone.v2.evaluator.function.global;

import ru.histone.v2.evaluator.Function;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.StringEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.util.List;

/**
 * Created by inv3r on 25/01/16.
 */
public class LoadJson implements Function {
    @Override
    public String getName() {
        return "loadJson";
    }

    @Override
    public EvalNode execute(List<EvalNode> args) throws FunctionExecutionException {
        return new StringEvalNode("Executed " + System.currentTimeMillis());
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public boolean isClear() {
        return false;
    }
}

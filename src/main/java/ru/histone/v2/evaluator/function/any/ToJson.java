package ru.histone.v2.evaluator.function.any;

import com.google.gson.Gson;
import ru.histone.v2.evaluator.Function;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.StringEvalNode;
import ru.histone.v2.exceptions.GlobalFunctionExecutionException;

import java.util.List;

/**
 * Created by inv3r on 22/01/16.
 */
public class ToJSON implements Function {
    @Override
    public String getName() {
        return "toJSON";
    }

    @Override
    public EvalNode execute(List<EvalNode> args) throws GlobalFunctionExecutionException {
        Gson gson = new Gson();
        String res = gson.toJson(args.get(0).getValue());

        return new StringEvalNode(res);
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

package ru.histone.v2.evaluator.global;

import ru.histone.v2.evaluator.Function;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.exceptions.GlobalFunctionExecutionException;
import ru.histone.v2.parser.node.AstNode;
import ru.histone.v2.parser.node.LongAstNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public EvalNode execute(List<AstNode> args) throws GlobalFunctionExecutionException {
        if (args.size() != 2) {
            throw new IllegalArgumentException("Wrong count of arguments. Actual is " + args.size() + ", but expected is 2");
        }
        long from = ((LongAstNode) args.get(0)).getValue();
        long to = ((LongAstNode) args.get(1)).getValue();

        Map<String, Object> res = new HashMap<>();
        for (int i = 0; i < to - from + 1; i++) {
            res.put(i + "", from + i);
        }
        return new MapEvalNode(res);
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

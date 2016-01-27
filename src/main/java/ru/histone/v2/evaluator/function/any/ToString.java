package ru.histone.v2.evaluator.function.any;

import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.Function;
import ru.histone.v2.evaluator.node.EmptyEvalNode;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Created by inv3r on 27/01/16.
 */
public class ToString implements Function {
    @Override
    public String getName() {
        return "toString";
    }

    @Override
    public CompletableFuture<EvalNode> execute(List<EvalNode> args) throws FunctionExecutionException {
        EvalNode node = args.get(0);
        if (node instanceof EmptyEvalNode) {
            return EvalUtils.getValue("");
        } else if (node instanceof MapEvalNode) {
            return EvalUtils.getValue(getMapString((MapEvalNode) node));
        }
        return EvalUtils.getValue(node.getValue() + "");
    }

    private String getMapString(MapEvalNode node) {
        Map<String, Object> map = node.getValue();
        return map.values().stream().map(x -> x + "").collect(Collectors.joining(" "));
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isClear() {
        return false;
    }
}

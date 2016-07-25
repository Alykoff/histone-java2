package ru.histone.v2.evaluator.function.array;

import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Aleksander Melnichnikov
 */
public abstract class ArrayIndexAware extends AbstractFunction implements Serializable {


    protected CompletableFuture<EvalNode> getNodeByIndex(MapEvalNode arrayNode, int index) throws FunctionExecutionException {
        final List<Map.Entry<String, EvalNode>> values = arrayNode
                .getValue()
                .entrySet()
                .stream()
                .collect(Collectors.toList());
        if (values.size() == 0) {
            return EvalUtils.getValue(null);
        }

        return CompletableFuture.completedFuture(values.get(index).getValue());
    }

}

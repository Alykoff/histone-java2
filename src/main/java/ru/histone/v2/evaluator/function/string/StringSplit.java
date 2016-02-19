package ru.histone.v2.evaluator.function.string;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.StringEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author gali.alykoff on 19/02/16.
 */
public class StringSplit extends AbstractFunction {
    public static final String NAME = "split";
    public static final String DEFAULT_SEPARATOR = "";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        final String value = ((StringEvalNode) args.get(0)).getValue();
        final String separator;
        if (args.size() > 1 && EvalUtils.isStringNode(args.get(1))) {
            separator = ((StringEvalNode) args.get(1)).getValue();
        } else {
            separator = DEFAULT_SEPARATOR;
        }
        return CompletableFuture.completedFuture(
                EvalUtils.constructFromList(
                        Arrays.asList(value.split(separator))
                )
        );
    }
}

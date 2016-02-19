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
import java.util.regex.Pattern;

/**
 * @author gali.alykoff on 19/02/16.
 */
public class StringSplit extends AbstractFunction {
    public static final String NAME = "split";
    public static final String DEFAULT_SEPARATOR = "";
    public static final int ELEMENT_INDEX = 0;
    public static final int SEPARATOR_INDEX = 1;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        final String value = ((StringEvalNode) args.get(ELEMENT_INDEX)).getValue();
        final String separator;
        if (args.size() > SEPARATOR_INDEX && EvalUtils.isStringNode(args.get(1))) {
            separator = ((StringEvalNode) args.get(SEPARATOR_INDEX)).getValue();
        } else {
            separator = DEFAULT_SEPARATOR;
        }
        return CompletableFuture.completedFuture(
                EvalUtils.constructFromList(
                        Arrays.asList(value.split(Pattern.quote(separator)))
                )
        );
    }
}

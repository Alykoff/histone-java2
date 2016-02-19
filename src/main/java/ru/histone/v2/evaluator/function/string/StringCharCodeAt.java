package ru.histone.v2.evaluator.function.string;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.*;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author gali.alykoff on 19/02/16.
 */
public class StringCharCodeAt extends AbstractFunction {
    public static final String NAME = "charCodeAt";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        final Optional<Integer> indexOptional = EvalUtils.tryPureIntegerValue(args.get(1));
        if (!indexOptional.isPresent()) {
            return CompletableFuture.completedFuture(EmptyEvalNode.INSTANCE);
        }
        final String value = ((StringEvalNode) args.get(0)).getValue();
        int index = indexOptional.get();
        int lenght = value.length();
        if (index < 0) index = lenght + index;
        if (index >= 0 && index < lenght) {
            final int character = (int) value.charAt(index);
            return CompletableFuture.completedFuture(new LongEvalNode(character));
        }
        return CompletableFuture.completedFuture(EmptyEvalNode.INSTANCE);
    }
}

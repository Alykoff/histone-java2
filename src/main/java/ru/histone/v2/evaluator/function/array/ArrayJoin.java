package ru.histone.v2.evaluator.function.array;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.function.any.ToString;
import ru.histone.v2.evaluator.node.*;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.utils.AsyncUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author gali.alykoff on 09/02/16.
 */
public class ArrayJoin extends AbstractFunction implements Serializable {
    public static final String DEFAULT_DELIMITER = "";
    public static final CompletableFuture<String> DEFAULT_DELIMITER_FUTURE = CompletableFuture.completedFuture(DEFAULT_DELIMITER);
    public static final String NAME = "join";
    private static final int MAP_EVAL_INDEX = 0;
    private static final int ARGS_START_INDEX = 1;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        final MapEvalNode mapEvalNode = (MapEvalNode) args.get(MAP_EVAL_INDEX);
        final Optional<EvalNode> separatorOptionalNode = Optional.ofNullable(args.size() > ARGS_START_INDEX
                ? args.get(ARGS_START_INDEX)
                : null
        );
        final CompletableFuture<String> separatorFuture = separatorOptionalNode.map(separatorNode ->
                context.call(ToString.NAME, Collections.singletonList(separatorNode))
                        .thenApply(x -> ((StringEvalNode) x).getValue())
        ).orElse(DEFAULT_DELIMITER_FUTURE);
        final CompletableFuture<List<EvalNode>> valueNodesFuture = separatorFuture.thenCompose(separator -> {
            final List<CompletableFuture<EvalNode>> nodes = mapEvalNode.getValue()
                    .values()
                    .stream()
                    .map(innerValue ->
                            context.call(ToString.NAME, Collections.singletonList(innerValue))
                    ).collect(Collectors.toList());
            return AsyncUtils.sequence(nodes);
        });
        return separatorFuture.thenCompose(separator -> valueNodesFuture.thenApply(valueNodes -> {
            final String values = valueNodes.stream()
                    .map(stringValue -> ((StringEvalNode) stringValue).getValue())
                    .collect(Collectors.joining(separator));
            return new StringEvalNode(values);
        }));
    }
}

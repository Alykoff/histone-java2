package ru.histone.v2.evaluator.function.array;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.function.any.ToNumber;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.LongEvalNode;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.evaluator.node.StringEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.AsyncUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author gali.alykoff on 09/02/16.
 */
public class ArrayChunk extends AbstractFunction implements Serializable {
    public static final String NAME = "chunk";
    private static final int MAP_EVAL_INDEX = 0;
    private static final int SPLIT_SIZE_INDEX = 1;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        final MapEvalNode mapEvalNode = (MapEvalNode) args.get(MAP_EVAL_INDEX);
        if (args.size() <= SPLIT_SIZE_INDEX) {
            return CompletableFuture.completedFuture(mapEvalNode);
        }
        final CompletableFuture<Optional<Integer>> splitSizeValue = context
                .call(ToNumber.NAME, Collections.singletonList(args.get(SPLIT_SIZE_INDEX)))
                .thenApply(ArrayChunk::getChunkSizeAfterToNumber);
        return splitSizeValue.thenApply(sizeOptional ->
            sizeOptional.map(size -> {
                final List<EvalNode> valuesRaw = new ArrayList<>(
                        mapEvalNode.getValue().values()
                );
                final List<EvalNode> innerNewValues = chunk(valuesRaw, size)
                        .stream()
                        .map(MapEvalNode::new)
                        .collect(Collectors.toList());
                return new MapEvalNode(innerNewValues);
            }).orElse(mapEvalNode)
        );
    }

    public static Optional<Integer> getChunkSizeAfterToNumber(EvalNode splitNode) {
        if (splitNode instanceof LongEvalNode) {
            final Long value = ((LongEvalNode) splitNode).getValue();
            if (value > Integer.MAX_VALUE || value <= 0) {
                return Optional.empty();
            } else {
                return Optional.of(value.intValue());
            }
        } else {
            return Optional.empty();
        }
    }

    public static <T> List<List<T>> chunk(List<T> original, int n) {
        final List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < original.size(); i += n) {
            partitions.add(original.subList(i, Math.min(i + n, original.size())));
        }
        return partitions;
    }
}

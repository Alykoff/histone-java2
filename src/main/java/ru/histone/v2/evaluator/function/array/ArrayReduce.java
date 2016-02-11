package ru.histone.v2.evaluator.function.array;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MacroEvalNode;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.evaluator.node.NullEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.utils.RttiUtils;
import ru.histone.v2.utils.Tuple;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author gali.alykoff on 11/02/16.
 */
public class ArrayReduce extends AbstractFunction implements Serializable {
    public static final String NAME = "reduce";
    public static final int CALLABLE_LIST_INDEX = 0;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        // [LIST, MACRO, INIT_EL, MACRO_BINDINGS...]
        final MapEvalNode array = (MapEvalNode) args.get(CALLABLE_LIST_INDEX);
        final List<EvalNode> valuesList = new ArrayList<>(array.getValue().values());
        if (valuesList.isEmpty()) {
            return CompletableFuture.completedFuture(NullEvalNode.INSTANCE);
        }
        final int size = args.size();
        if (size == 1) {
            return CompletableFuture.completedFuture(valuesList.get(0));
        }
        final Queue<EvalNode> values = new LinkedList<>();
        if (size > 2) {
            values.add(args.get(2));
        }
        values.addAll(valuesList);
        if (values.size() == 1) {
            return CompletableFuture.completedFuture(values.poll());
        }

        final CompletableFuture<MacroEvalNode> macroNodeFuture = CompletableFuture
                .completedFuture((MacroEvalNode) args.get(1))
                .thenCompose(macroNode -> {
                    if (size < 3) {
                        return CompletableFuture.completedFuture(macroNode);
                    }
                    final List<EvalNode> bindMacroArgs = args.subList(3, args.size());
                    return RttiUtils.callMacroBind(context, macroNode, bindMacroArgs)
                            .thenApply(x -> (MacroEvalNode) x);
                });
        return macroNodeFuture.thenCompose(macro -> {
            final EvalNode acc = values.poll();
            final CompletableFuture<Tuple<EvalNode, Queue<EvalNode>>> accTuple =
                    CompletableFuture.completedFuture(new Tuple<>(acc, values));
            return reduce(context, macro, accTuple);
        });
    }

    private static CompletableFuture<EvalNode> reduce(
            Context context, MacroEvalNode macro,
            CompletableFuture<Tuple<EvalNode, Queue<EvalNode>>> accTupleFuture
    ) {
        return accTupleFuture.thenCompose(accTuple -> {
            final EvalNode acc = accTuple.getLeft();
            final Queue<EvalNode> vals = accTuple.getRight();
            if (vals.peek() == null) {
                return CompletableFuture.completedFuture(acc);
            }
            final EvalNode curr = vals.poll();
            final CompletableFuture<Tuple<EvalNode, Queue<EvalNode>>> newAccTuple = RttiUtils
                    .callMacro(context, macro, acc, curr)
                    .thenApply(newAcc -> new Tuple<>(newAcc, vals));

            return reduce(context, macro, newAccTuple);
        });
    }
}

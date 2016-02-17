package ru.histone.v2.evaluator.function.array;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.*;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.RttiUtils;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author gali.alykoff on 11/02/16.
 */
public class ArrayFind extends AbstractFunction implements Serializable {
    public static final String NAME = "find";
    public static final int ARRAY_INDEX = 0;
    private static final int MACRO_INDEX = 1;
    public static final int START_BIND_INDEX = 2;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        // [ARRAY, MACRO, MACRO_BINDINGS...]
        final MapEvalNode arrayNode = (MapEvalNode) args.get(ARRAY_INDEX);
        final List<EvalNode> values = arrayNode
                .getValue()
                .values()
                .stream()
                .collect(Collectors.toList());
        if (args.size() <= MACRO_INDEX || values.size() == 0) {
            return CompletableFuture.completedFuture(NullEvalNode.INSTANCE);
        }

        final EvalNode rawMacroNode = args.get(MACRO_INDEX);
        if (rawMacroNode.getType() != HistoneType.T_MACRO) {
            return RttiUtils.callToBooleanResult(context, rawMacroNode)
                    .thenApply(predicate ->
                            predicate ? values.get(0) : NullEvalNode.INSTANCE
                    );
        }
        final CompletableFuture<MacroEvalNode> macroFuture =
                ArrayReduce.getMacroWithBindFuture(context, args, START_BIND_INDEX);
        return macroFuture.thenCompose(macro -> find(context, macro, values));
    }

    public static CompletableFuture<EvalNode> find(
            Context context,
            MacroEvalNode macro,
            List<EvalNode> nodes
    ) {
        if (nodes.size() == 0) {
            return CompletableFuture.completedFuture(NullEvalNode.INSTANCE);
        }
        final EvalNode first = nodes.get(0);
        return RttiUtils.callMacro(context, macro, first).thenCompose(resultMacro ->
            RttiUtils.callToBooleanResult(context, resultMacro)
        ).thenCompose(predicate ->
            predicate
                ? CompletableFuture.completedFuture(first)
                : find(context, macro, nodes.subList(1, nodes.size()))
        );
    }
}

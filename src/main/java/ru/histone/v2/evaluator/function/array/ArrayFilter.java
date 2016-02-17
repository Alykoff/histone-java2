package ru.histone.v2.evaluator.function.array;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.function.macro.MacroCall;
import ru.histone.v2.evaluator.node.BooleanEvalNode;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MacroEvalNode;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.utils.AsyncUtils;
import ru.histone.v2.utils.Tuple;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


/**
 * @author gali.alykoff on 09/02/16.
 */
public class ArrayFilter extends AbstractFunction implements Serializable {
    public static final String NAME = "filter";
    public static final int MAP_EVAL_INDEX = 0;
    public static final int MACRO_INDEX = 1;
    public static final int ARGS_START_INDEX = 2;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        return calcByPredicate(context, args).thenApply(pairs ->
            pairs.stream()
                    .filter(Tuple::getRight)
                    .map(Tuple::getLeft)
                    .collect(Collectors.toList())
        ).thenApply(MapEvalNode::new);
    }

    public static CompletableFuture<List<Tuple<EvalNode, Boolean>>> calcByPredicate(Context context, List<EvalNode> args) {
        final MapEvalNode mapEvalNode = (MapEvalNode) args.get(MAP_EVAL_INDEX);
        final MacroEvalNode macro = (MacroEvalNode) args.get(MACRO_INDEX);
        final EvalNode param = args.size() > ARGS_START_INDEX ? args.get(ARGS_START_INDEX) : null;

        final List<CompletableFuture<Tuple<EvalNode, Boolean>>> mapResultWithPredicate = mapEvalNode.getValue()
                .values().stream()
                .map(arg -> {
                    final List<EvalNode> arguments = new ArrayList<>(Collections.singletonList(macro));
                    if (param != null) {
                        arguments.add(param);
                    }
                    arguments.add(arg);
                    final CompletableFuture<EvalNode> predicateFuture = MacroCall.staticExecute(context, arguments);
                    return predicateFuture.thenApply(predicateNode -> {
                        final Boolean predicate = ((BooleanEvalNode) predicateNode).getValue();
                        return Tuple.<EvalNode, Boolean>create(arg, predicate);
                    });
                }).collect(Collectors.toList());
        return AsyncUtils.sequence(mapResultWithPredicate);
    }
}

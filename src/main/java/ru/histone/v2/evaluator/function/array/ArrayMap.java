package ru.histone.v2.evaluator.function.array;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.function.macro.MacroCall;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MacroEvalNode;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.utils.AsyncUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author gali.alykoff on 08/02/16.
 */
public class ArrayMap extends AbstractFunction implements Serializable {
    public static final String NAME = "map";
    public static final int MAP_EVAL_INDEX = 0;
    public static final int MACRO_INDEX = 1;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        // [MAP, MACROS]
        final MapEvalNode mapEvalNode = (MapEvalNode) args.get(MAP_EVAL_INDEX);
        final MacroEvalNode macro = (MacroEvalNode) args.get(MACRO_INDEX);
        final EvalNode param = args.size() > 2 ? args.get(2) : null;

        final List<CompletableFuture<EvalNode>> mapResultRaw = mapEvalNode.getValue()
                .values().stream()
                .map(arg -> {
                    List<EvalNode> arguments = new ArrayList<>(Collections.singletonList(macro));
                    if (param != null) {
                        arguments.add(param);
                    }
                    arguments.add(arg);
                    return MacroCall.staticExecute(context, arguments);
                })
                .collect(Collectors.toList());
        return AsyncUtils
                .sequence(mapResultRaw)
                .thenApply(MapEvalNode::new);
    }
}

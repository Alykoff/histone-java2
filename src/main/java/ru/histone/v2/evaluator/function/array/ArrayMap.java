package ru.histone.v2.evaluator.function.array;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.Evaluator;
import ru.histone.v2.evaluator.data.HistoneMacro;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.function.macro.MacroCall;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MacroEvalNode;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.parser.node.AstNode;
import ru.histone.v2.utils.AsyncUtils;

import java.io.Serializable;
import java.util.*;
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
        Collections.singleton(args.get(0));

        final List<CompletableFuture<EvalNode>> mapResultRaw = mapEvalNode.getValue()
                .values().stream()
                .map(arg ->
                    MacroCall.staticExecute(Arrays.asList(macro, arg))
                ).collect(Collectors.toList());
        return AsyncUtils
                .sequence(mapResultRaw)
                .thenApply(MapEvalNode::new);
    }
}

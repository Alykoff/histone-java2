package ru.histone.v2.evaluator.function.array;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author gali.alykoff on 11/02/16.
 */
public class ArraySort extends AbstractFunction implements Serializable {
    public static final String NAME = "sort";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        // [LIST, MACRO_SORT, MACRO_BINDINGS...]
        // MACRO_SORT is function:
        // Function(
        //  left_value:node, right_value:node, left_key:string_node, right_key:string_node
        // )->boolean_node
        final MapEvalNode array = (MapEvalNode) args.get(0);
        final List<EvalNode> values = new ArrayList<>(array.getValue().values());
        final int argsSize = args.size();
        final boolean isMacroNotPresent = argsSize == 1;
        if (isMacroNotPresent) {
            
        }
        return null;
    }
}

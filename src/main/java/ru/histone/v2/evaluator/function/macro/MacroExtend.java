package ru.histone.v2.evaluator.function.macro;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.data.HistoneMacro;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MacroEvalNode;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author gali.alykoff on 05/02/16.
 */
public class MacroExtend extends AbstractFunction implements Serializable {
    public static final String NAME = "extend";
    public static final int INDEX_PROPERTY_NAME = 1;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        final CompletableFuture<MacroEvalNode> histoneMacro = CompletableFuture.completedFuture(
                (MacroEvalNode) args.get(0)
        );
        return histoneMacro.thenApply(macro -> {
            final HistoneMacro macroClone = macro.getValue().clone();
            final Map<String, EvalNode> argsBindEvalNodes = new LinkedHashMap<>();
            if (args.size() > INDEX_PROPERTY_NAME
                    && args.get(INDEX_PROPERTY_NAME).getType() == HistoneType.T_ARRAY) {
                final MapEvalNode argsMap = (MapEvalNode) args.get(INDEX_PROPERTY_NAME);
                argsBindEvalNodes.putAll(argsMap.getValue());
            }
            return new MacroEvalNode(macroClone, macro.getExtArgs())
                    .putAllExtArgs(argsBindEvalNodes);
        });
    }
}

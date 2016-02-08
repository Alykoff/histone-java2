package ru.histone.v2.evaluator.function.macro;

import ru.histone.v2.evaluator.data.HistoneMacro;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MacroEvalNode;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author gali.alykoff on 05/02/16.
 */
public class MacroExtend extends AbstractFunction implements Serializable {
    public static final String NAME = "extend";
    public static final int FIRST_ARG_INDEX = 1;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(String baseUri, Locale locale, List<EvalNode> args) throws FunctionExecutionException {
        final CompletableFuture<HistoneMacro> histoneMacro = CompletableFuture.completedFuture(
                ((MacroEvalNode) args.get(0)).getValue().clone()
        );
        return histoneMacro.thenApply(macro -> {
            final Map<String, EvalNode> argsBindEvalNodes = new LinkedHashMap<>();
            if (args.size() > FIRST_ARG_INDEX
                    && args.get(FIRST_ARG_INDEX).getType() == HistoneType.T_ARRAY) {
                final MapEvalNode argsMap = (MapEvalNode) args.get(FIRST_ARG_INDEX);
                argsBindEvalNodes.putAll(argsMap.getValue());
            }
            return new MacroEvalNode(macro).putAllExtArgs(argsBindEvalNodes);
        });
    }
}

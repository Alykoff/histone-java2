package ru.histone.v2.evaluator.function.global;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.exceptions.HistoneException;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.RttiUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author gali.alykoff on 17/05/16.
 */
public class Wait extends AbstractFunction {
    public static final String NAME = "wait";
    private static final int DEFAULT_MILLS_VALUE = 0;
    private static final int MACRO_ARG_INDEX = 1;
    private static final int MILLS_ARG_INDEX = 0;
    private static final int PARAMS_FOR_MACRO_ARG_START_INDEX = 2;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        return doExecute(context, clearGlobal(args));
    }

    private CompletableFuture<EvalNode> doExecute(Context context, List<EvalNode> args) {
        // wait(mills: Number): UNDEFINED
        // wait(mills: Number, macros: () -> T): T
        // wait(mills: Number, notMacros: T): T
        if (args.isEmpty()) {
            return EvalUtils.getValue(null);
        }

        final EvalNode millsNode = args.get(MILLS_ARG_INDEX);
        final int mills = EvalUtils
                .tryPureIntegerValue(millsNode)
                .filter(x -> x >= 0)
                .orElse(DEFAULT_MILLS_VALUE);
        try {
            Thread.sleep(mills);
        } catch (InterruptedException ignore) {
            throw new HistoneException("Wait function was interrupted", ignore);
        }

        if (args.size() == 1) {
            return EvalUtils.getValue(null);
        }

        final EvalNode callbackNode = args.get(MACRO_ARG_INDEX);
        return callbackNode.getType() != HistoneType.T_MACRO
                ? CompletableFuture.completedFuture(callbackNode)
                : RttiUtils.callMacro(context, callbackNode, getMacroArgs(args));
    }

    private List<EvalNode> getMacroArgs(List<EvalNode> args) {
        return args.size() == PARAMS_FOR_MACRO_ARG_START_INDEX
                ? Collections.emptyList()
                : args.subList(PARAMS_FOR_MACRO_ARG_START_INDEX, args.size());
    }
}
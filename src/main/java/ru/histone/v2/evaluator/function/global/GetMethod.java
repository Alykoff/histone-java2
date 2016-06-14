package ru.histone.v2.evaluator.function.global;

import org.apache.commons.lang3.ObjectUtils;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.data.HistoneMacro;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MacroEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.parser.node.*;
import ru.histone.v2.utils.RttiUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author gali.alykoff on 30/05/16.
 */
public class GetMethod extends AbstractFunction {
    public static final String NAME = "getMethod";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        return doExecute(context, clearGlobal(args));
    }

    private CompletableFuture<EvalNode> doExecute(Context context, List<EvalNode> args) {
        if (args.isEmpty()) {
            return EvalUtils.getValue(ObjectUtils.NULL);
        }

        final EvalNode methodName = args.get(0);
        return RttiUtils.callToStringResult(context, methodName).thenCompose(name -> {
            if (!context.findFunction(name)) {
                return EvalUtils.getValue(ObjectUtils.NULL);
            }
            return CompletableFuture.completedFuture(
                    new MacroEvalNode(new HistoneMacro(
                            Collections.emptyList(),
                            buildBody(name),
                            context.clone(),
                            Collections.emptyList(),
                            Collections.emptyMap(),
                            HistoneMacro.MACRO_IS_WRAPPED_GLOBAL_FUNC_FLAG
                    ))
            );
        });
    }

    private AstNode buildBody(String funcName) {
        return new CallExpAstNode(
                CallType.SIMPLE,
                new ExpAstNode(AstType.AST_GLOBAL),
                new StringAstNode(funcName)
        );
    }
}

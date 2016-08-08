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
import ru.histone.v2.rtti.HistoneType;
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
        if (args.isEmpty()) {
            return EvalUtils.getValue(ObjectUtils.NULL);
        }

        if (args.size() == 1) {
            return createGlobalWrapper(context, args.get(0));
        }

        //ok, we have two or more arguments

        if (args.get(0).getType() == HistoneType.T_GLOBAL) {
            return createGlobalWrapper(context, args.get(1));
        }

        return RttiUtils.callToStringResult(context, args.get(1)).thenCompose(name -> {
            if (!context.findFunction(args.get(0), name)) {
                return EvalUtils.getValue(ObjectUtils.NULL);
            }

            HistoneMacro macro = new HistoneMacro(
                    Collections.emptyList(),
                    buildValueBody(context, name),
                    context.clone(),
                    Collections.emptyList(),
                    Collections.emptyMap(),
                    args.get(0),
                    HistoneMacro.WrappingType.VALUE
            );

            return CompletableFuture.completedFuture(new MacroEvalNode(macro));
        });
    }

    private CompletableFuture<EvalNode> createGlobalWrapper(Context context, EvalNode node) {
        return RttiUtils.callToStringResult(context, node).thenCompose(name -> {
            if (!context.findFunction(name)) {
                return EvalUtils.getValue(ObjectUtils.NULL);
            }
            return CompletableFuture.completedFuture(
                    new MacroEvalNode(new HistoneMacro(
                            Collections.emptyList(),
                            buildGlobalBody(context, name),
                            context.clone(),
                            Collections.emptyList(),
                            Collections.emptyMap(),
                            HistoneMacro.WrappingType.GLOBAL
                    ))
            );
        });
    }

    protected Object buildGlobalBody(Context ctx, String funcName) {
        return new CallExpAstNode(CallType.SIMPLE, new ExpAstNode(AstType.AST_GLOBAL), new StringAstNode(funcName));
    }

    protected Object buildValueBody(Context ctx, String funcName) {
        return new CallExpAstNode(CallType.SIMPLE, new StringAstNode("null"), new StringAstNode(funcName));
    }
}

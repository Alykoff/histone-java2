package ru.histone.v2.evaluator.function.string;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.evaluator.node.NullEvalNode;
import ru.histone.v2.evaluator.node.StringEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.AsyncUtils;
import ru.histone.v2.utils.Tuple;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author gali.alykoff on 19/02/16.
 */
public class StringHtmlEntities extends AbstractFunction {
    public static final String NAME = "htmlentities";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        final EvalNode node = args.get(0);
        return htmlEntities(node);
    }

    public static CompletableFuture<EvalNode> htmlEntities(EvalNode node) {
        final String value = ((StringEvalNode) node).getValue();
        final String result = value
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll("\"", "&quot;")
            .replaceAll("'", "&#039;");
        return CompletableFuture.completedFuture(new StringEvalNode(result));
    }
}

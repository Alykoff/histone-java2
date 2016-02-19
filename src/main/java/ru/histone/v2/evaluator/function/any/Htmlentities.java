package ru.histone.v2.evaluator.function.any;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.evaluator.node.NullEvalNode;
import ru.histone.v2.evaluator.node.StringEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.AsyncUtils;
import ru.histone.v2.utils.RttiUtils;
import ru.histone.v2.utils.Tuple;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author gali.alykoff on 19/02/16.
 */
public class HtmlEntities extends AbstractFunction {
    public static final String NAME = "htmlentities";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        final EvalNode node = args.get(0);
        final HistoneType type = node.getType();
        if (type == HistoneType.T_ARRAY) {
            final MapEvalNode mapNode = (MapEvalNode) node;
            final Set<Map.Entry<String, EvalNode>> keyValues = mapNode.getValue().entrySet();
            final List<CompletableFuture<Tuple<String, EvalNode>>> accFutures = new LinkedList<>();
            for (Map.Entry<String, EvalNode> entry : keyValues) {
                final String key = entry.getKey();
                final EvalNode value = entry.getValue();
                final boolean isCheckingValuesInArray = value.getType() == HistoneType.T_ARRAY
                        || value.getType() == HistoneType.T_STRING;
                if (isCheckingValuesInArray) {
                    accFutures.add(RttiUtils.callHtmlEntities(context, value).thenApply(newValue ->
                            new Tuple<>(key, newValue)
                    ));
                } else {
                    accFutures.add(CompletableFuture.completedFuture(
                            new Tuple<>(key, value))
                    );
                }
            }
            return AsyncUtils.sequence(accFutures).thenApply(acc -> {
                Map<String, EvalNode> result = new LinkedHashMap<>();
                for (Tuple<String, EvalNode> tuple : acc) {
                    result.put(tuple.getLeft(), tuple.getRight());
                }
                return new MapEvalNode(result);
            });
        } else if (type == HistoneType.T_STRING) {
            final String value = ((StringEvalNode) node).getValue();
            final String result = value
                .replaceAll("&", "&amp;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#039;");
            return CompletableFuture.completedFuture(new StringEvalNode(result));
        } else {
            return CompletableFuture.completedFuture(NullEvalNode.INSTANCE);
        }
    }
}

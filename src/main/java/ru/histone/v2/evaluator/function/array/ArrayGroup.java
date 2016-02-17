package ru.histone.v2.evaluator.function.array;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.*;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.RttiUtils;
import ru.histone.v2.utils.Tuple;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author gali.alykoff on 11/02/16.
 */
public class ArrayGroup extends AbstractFunction implements Serializable {
    public static final String NAME = "group";
    public static final int ARRAY_INDEX = 0;
    private static final int MACRO_INDEX = 1;
    public static final int START_BIND_INDEX = 2;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        // [ARRAY, MACRO, MACRO_BINDINGS...]
        final MapEvalNode arrayNode = (MapEvalNode) args.get(ARRAY_INDEX);
        final List<Map.Entry<String, EvalNode>> values = arrayNode
                .getValue()
                .entrySet()
                .stream()
                .collect(Collectors.toList());
        if (args.size() <= MACRO_INDEX || values.size() == 0) {
            return CompletableFuture.completedFuture(new MapEvalNode(Collections.emptyList()));
        }

        final EvalNode rawMacroNode = args.get(MACRO_INDEX);
        if (rawMacroNode.getType() != HistoneType.T_MACRO) {
            return RttiUtils.callToBooleanResult(context, rawMacroNode)
                    .thenApply(predicate ->
                            predicate ? values.get(0).getValue() : NullEvalNode.INSTANCE
                    );
        }
        final CompletableFuture<MacroEvalNode> macroFuture =
                ArrayReduce.getMacroWithBindFuture(context, args, START_BIND_INDEX);
        return macroFuture.thenCompose(macro ->
            groupBy(context, macro, values, new HashMap<>())
        ).thenApply(acc -> {
            final Map<String, EvalNode> res = new LinkedHashMap<>(acc.size());
            for (Map.Entry<String, List<EvalNode>> val : acc.entrySet()) {
                res.put(val.getKey(), new MapEvalNode(val.getValue()));
            }
            return new MapEvalNode(res);
        });
    }

    public static CompletableFuture<Map<String, List<EvalNode>>> groupBy(
            Context context,
            MacroEvalNode macro,
            List<Map.Entry<String, EvalNode>> nodes,
            Map<String, List<EvalNode>> acc
    ) {
        if (nodes.isEmpty()) {
            return CompletableFuture.completedFuture(acc);
        }
        final Map.Entry<String, EvalNode> first = nodes.get(0);
        final EvalNode firstValue = first.getValue();
        final EvalNode firstKey = new StringEvalNode(first.getKey());
        return RttiUtils.callMacro(context, macro, firstValue, firstKey).thenCompose(macroResult ->
            RttiUtils.callToStringResult(context, macroResult).thenCompose(group -> {
                System.out.println(macroResult);
                final List<EvalNode> listOfGroup = acc.get(group);
                if (listOfGroup == null) {
                    List<EvalNode> list = new ArrayList<>();
                    list.add(firstValue);
                    acc.put(group, list);
                } else {
                    listOfGroup.add(firstValue);
                }
                return groupBy(context, macro, nodes.subList(1, nodes.size()), acc);
            }));
    }
}

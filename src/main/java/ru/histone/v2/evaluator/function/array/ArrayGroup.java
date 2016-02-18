package ru.histone.v2.evaluator.function.array;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.*;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.ParserUtils;
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
    public static final Comparator<Tuple<Integer, EvalNode>> HISTONE_ARRAY_GROUP_COMPARATOR = (x, y) -> {
        final Integer xKey = x.getLeft();
        final Integer yKey = y.getLeft();
        if (xKey < 0) {
            return yKey < 0 ? yKey.compareTo(xKey) : 1;
        } else {
            return yKey < 0 ? -1 : xKey.compareTo(yKey);
        }
    };

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
            // TODO maybe we should del this sort
            final Map<String, EvalNode> res = new LinkedHashMap<>();
            final List<Tuple<String, EvalNode>> entrySimpleKeys = new ArrayList<>();
            final Set<Tuple<Integer, EvalNode>> entryWithNumKeys = new TreeSet<>(HISTONE_ARRAY_GROUP_COMPARATOR);

            for (Map.Entry<String, List<EvalNode>> groups : acc.entrySet()) {
                final String key = groups.getKey();
                final EvalNode groupsObjs = new MapEvalNode(groups.getValue());
                final Optional<Integer> intKey = ParserUtils.tryInt(key);
                if (intKey.isPresent()) {
                    entryWithNumKeys.add(new Tuple<>(intKey.get(), groupsObjs));
                } else {
                    entrySimpleKeys.add(new Tuple<>(key, groupsObjs));
                }
            }

            for (Tuple<Integer, EvalNode> key : entryWithNumKeys) {
                System.out.println(key);
                res.put(key.getLeft().toString(), key.getRight());
            }
            for (Tuple<String, EvalNode> key : entrySimpleKeys) {
                res.put(key.getLeft(), key.getRight());
            }

            return new MapEvalNode(res);
        });
    }

    public static CompletableFuture<Map<String, List<EvalNode>>> groupBy(
            Context context,
            MacroEvalNode macro,
            List<Map.Entry<String, EvalNode>> nodes,
            Map<String, List<EvalNode>> accNumber
    ) {
        if (nodes.isEmpty()) {
            return CompletableFuture.completedFuture(accNumber);
        }
        final Map.Entry<String, EvalNode> first = nodes.get(0);
        final EvalNode firstValue = first.getValue();
        final EvalNode firstKey = new StringEvalNode(first.getKey());
        return RttiUtils.callMacro(context, macro, firstValue, firstKey).thenCompose(macroResult ->
            RttiUtils.callToStringResult(context, macroResult).thenCompose(group -> {
                final List<EvalNode> listOfGroup = accNumber.get(group);
                if (listOfGroup == null) {
                    List<EvalNode> list = new ArrayList<>();
                    list.add(firstValue);
                    accNumber.put(group, list);
                } else {
                    listOfGroup.add(firstValue);
                }
                return groupBy(
                    context, macro, nodes.subList(1, nodes.size()), accNumber
                );
            }));
    }
}

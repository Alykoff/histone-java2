/*
 * Copyright (c) 2016 MegaFon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.histone.v2.java_compiler.bcompiler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.global.BooleanEvalNodeComparator;
import ru.histone.v2.evaluator.global.NumberComparator;
import ru.histone.v2.evaluator.global.StringEvalNodeLenComparator;
import ru.histone.v2.evaluator.global.StringEvalNodeStrongComparator;
import ru.histone.v2.evaluator.node.*;
import ru.histone.v2.parser.node.AstType;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.rtti.RttiMethod;
import ru.histone.v2.utils.AsyncUtils;
import ru.histone.v2.utils.DateUtils;
import ru.histone.v2.utils.ParserUtils;
import ru.histone.v2.utils.RttiUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.DoubleBinaryOperator;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static ru.histone.v2.evaluator.EvalUtils.*;
import static ru.histone.v2.utils.ParserUtils.tryDouble;
import static ru.histone.v2.utils.ParserUtils.tryLongNumber;

/**
 * @author Alexey Nevinsky
 */
public class StdLibrary {

    private static final Comparator<Number> NUMBER_COMPARATOR = new NumberComparator();
    private static final Comparator<StringEvalNode> STRING_EVAL_NODE_LEN_COMPARATOR = new StringEvalNodeLenComparator();
    private static final Comparator<StringEvalNode> STRING_EVAL_NODE_STRONG_COMPARATOR = new StringEvalNodeStrongComparator();
    private static final Comparator<BooleanEvalNode> BOOLEAN_EVAL_NODE_COMPARATOR = new BooleanEvalNodeComparator();
    private static final Logger LOG = LoggerFactory.getLogger(StdLibrary.class);

    public static CompletableFuture<EvalNode> sub(Context ctx, CompletableFuture<EvalNode> first, CompletableFuture<EvalNode> second) {
        return doArithmetic(first, second, (x, y) -> x - y);
    }


    public static CompletableFuture<EvalNode> mod(Context ctx, CompletableFuture<EvalNode> first, CompletableFuture<EvalNode> second) {
        return doArithmetic(first, second, (x, y) -> x % y);
    }

    public static CompletableFuture<EvalNode> mul(Context ctx, CompletableFuture<EvalNode> first, CompletableFuture<EvalNode> second) {
        return doArithmetic(first, second, (x, y) -> x * y);
    }

    public static CompletableFuture<EvalNode> div(Context ctx, CompletableFuture<EvalNode> first, CompletableFuture<EvalNode> second) {
        return doArithmetic(first, second, (x, y) -> x / y);
    }

    private static CompletableFuture<EvalNode> doArithmetic(CompletableFuture<EvalNode> first, CompletableFuture<EvalNode> second,
                                                            DoubleBinaryOperator sup) {
        return AsyncUtils.sequence(first, second)
                .thenCompose(l -> {
                    EvalNode left = l.get(0);
                    EvalNode right = l.get(1);
                    if (isNumberNode(left) || left.getType() == HistoneType.T_STRING) {
                        Double lValue = getValue(left).orElse(null);
                        if (lValue != null) {
                            if (isNumberNode(right) || right.getType() == HistoneType.T_STRING) {
                                Double rValue = getValue(right).orElse(null);
                                if (rValue != null) {
                                    return EvalUtils.getValue(sup.applyAsDouble(lValue, rValue));
                                }
                            }
                        }
                    }
                    return EvalUtils.getValue(null);
                });
    }

    private static Optional<Double> getValue(EvalNode node) { // TODO duplicate ???
        if (node.getType() == HistoneType.T_STRING) {
            return ParserUtils.tryDouble(((StringEvalNode) node).getValue());
        } else {
            return Optional.of(Double.valueOf(node.getValue() + ""));
        }
    }

    public static CompletableFuture<EvalNode> arr(Object... args) {
        return null;
    }

    public static boolean bool(CompletableFuture<EvalNode> apply) {
        return false;
    }

    public static CompletableFuture<StringBuilder> append(Context ctx, CompletableFuture<StringBuilder> csb,
                                                          CompletableFuture<EvalNode> var) {
        return var
                .thenCompose(node -> RttiUtils.callToString(ctx, node)
                        .thenCompose(v -> csb.thenApply(sb -> sb.append(v.getValue())))
                );
    }

    public static CompletableFuture<EvalNode> asString(Context ctx, CompletableFuture<StringBuilder> csb) {
        return csb.thenCompose(sb -> RttiUtils.callToString(ctx, EvalUtils.createEvalNode(sb.toString())));
    }

    public static CompletableFuture<EvalNode> add(Context ctx, CompletableFuture<EvalNode> first, CompletableFuture<EvalNode> second) {
        return AsyncUtils.sequence(first, second).thenCompose(lr -> {
            EvalNode left = lr.get(0);
            EvalNode right = lr.get(1);
            if (!(left.getType() == HistoneType.T_STRING || right.getType() == HistoneType.T_STRING)) {
                final boolean isLeftNumberNode = isNumberNode(left);
                final boolean isRightNumberNode = isNumberNode(right);
                if (isLeftNumberNode && isRightNumberNode) {
                    final Double res = getValue(left).orElse(null) + getValue(right).orElse(null);
                    return EvalUtils.getNumberFuture(res);
                } else if (isLeftNumberNode || isRightNumberNode) {
                    return EvalUtils.getValue(null);
                }

                if (left.getType() == HistoneType.T_ARRAY && right.getType() == HistoneType.T_ARRAY) {
                    final MapEvalNode result = new MapEvalNode(new LinkedHashMap<>());
                    result.append((MapEvalNode) left);
                    result.append((MapEvalNode) right);
                    return completedFuture(result);
                }
            }

            return AsyncUtils.sequence(
                    RttiUtils.callToString(ctx, left),
                    RttiUtils.callToString(ctx, right)
            ).thenCompose(futures -> {
                StringEvalNode l = (StringEvalNode) futures.get(0);
                StringEvalNode r = (StringEvalNode) futures.get(1);
                return EvalUtils.getValue(l.getValue() + r.getValue());
            });
        });
    }

    public static CompletableFuture<EvalNode> uSub(Context ctx, CompletableFuture<EvalNode> v) {
        return v.thenApply(n -> {
            if (n instanceof LongEvalNode) {
                final Long value = ((LongEvalNode) n).getValue();
                return new LongEvalNode(-value);
            } else if (n instanceof DoubleEvalNode) {
                final Double value = ((DoubleEvalNode) n).getValue();
                return new DoubleEvalNode(-value);
            } else if (n instanceof StringEvalNode) {
                final String stringValue = ((StringEvalNode) n).getValue();
                final Optional<Long> longOptional = tryLongNumber(stringValue);
                if (longOptional.isPresent()) {
                    return new LongEvalNode(-longOptional.get());
                }

                final Optional<Double> doubleOptional = tryDouble(stringValue);
                if (doubleOptional.isPresent()) {
                    return new DoubleEvalNode(-doubleOptional.get());
                }
            }
            return EvalUtils.createEvalNode(null);
        });
    }

    public static CompletableFuture<EvalNode> array(CompletableFuture<EvalNode>... nodes) {
        if (nodes.length == 0) {
            return completedFuture(new MapEvalNode(new LinkedHashMap<>(0)));
        }
        //todo
//        if (node.getNode(0).getType() == AstType.AST_VAR) {
//            return evalAllNodesOfCurrent(node, context).thenApply(evalNodes -> EvalUtils.createEvalNode(null));
//        }

        return AsyncUtils.sequence(nodes)
                .thenApply(nodeList -> {
                    Map<String, EvalNode> map = new LinkedHashMap<>();
                    for (int i = 0; i < nodeList.size() / 2; i++) {
                        EvalNode value = nodeList.get(i * 2);
                        EvalNode key = nodeList.get(i * 2 + 1);
                        map.put(key.getValue() + "", value);
                    }
                    return new MapEvalNode(map);
                });
    }

    /**
     * @param ctx   evaluation context
     * @param nodes array of nodes: нулевой элемент - переменная, у которой получаем какое-то значение, остальные - аргументы.
     * @return
     */
    public static CompletableFuture<EvalNode> mGet(Context ctx, CompletableFuture<EvalNode>... nodes) {
        return AsyncUtils.sequence(nodes)
                .thenCompose(nodeList -> ctx.call(nodeList.get(0), RttiMethod.RTTI_M_GET.getId(), nodeList));
    }

    public static CompletableFuture<EvalNode> simpleCall(Context ctx, String functionName, List<CompletableFuture<EvalNode>> args) {
        return AsyncUtils.sequence(args)
                .thenCompose(argList -> ctx.call(functionName, argList))
                .exceptionally(e -> {
                    LOG.error(e.getMessage(), e);
                    return EvalUtils.createEvalNode(null);
                });
    }

    //todo rework this fucking copypaste!
    public static CompletableFuture<EvalNode> gt(Context ctx, CompletableFuture<EvalNode> first, CompletableFuture<EvalNode> second) {
        return AsyncUtils.sequence(first, second)
                .thenCompose(nodes -> {
                    final EvalNode left = nodes.get(0);
                    final EvalNode right = nodes.get(1);
                    final CompletableFuture<Integer> compareResultRaw = compareNodes(left, right, ctx, STRING_EVAL_NODE_LEN_COMPARATOR);
//                    final CompletableFuture<Integer> compareResultRaw = compareNodes(left, right, ctx, stringNodeComparator);

                    return compareResultRaw.thenApply(compareResult ->
//                            processRelationComparatorHelper(node.getType(), compareResult)
                                    processRelationComparatorHelper(AstType.AST_GT, compareResult)
                    );
                });
    }

    private static CompletableFuture<Integer> compareNodes(
            EvalNode left, EvalNode right, Context context,
            Comparator<StringEvalNode> stringNodeComparator
    ) {
        final CompletableFuture<Integer> result;
        if (isStringNode(left) && isNumberNode(right)) {
            final StringEvalNode stringLeft = (StringEvalNode) left;
            if (isNumeric(stringLeft)) {
                result = processRelationNumberHelper(left, right);
            } else {
                result = processRelationToString(stringLeft, right, context, stringNodeComparator, false);
            }
        } else if (isNumberNode(left) && isStringNode(right)) {
            final StringEvalNode stringRight = (StringEvalNode) right;
            if (isNumeric(stringRight)) {
                result = processRelationNumberHelper(left, right);
            } else {
                result = processRelationToString(stringRight, left, context, stringNodeComparator, true);
            }
        } else if (left.hasAdditionalType(HistoneType.T_DATE) && right.hasAdditionalType(HistoneType.T_DATE)) {
            LocalDateTime leftValue = DateUtils.createDate(((DateEvalNode) left).getValue());
            LocalDateTime rightValue = DateUtils.createDate(((DateEvalNode) right).getValue());
            //leftValue and rightValue always has value, bcz functions which create DateEvalNode checks it
            // or return EmptyEvalNode
            result = CompletableFuture.completedFuture(leftValue.compareTo(rightValue));
        } else if (!isNumberNode(left) || !isNumberNode(right)) {
            if (isStringNode(left) && isStringNode(right)) {
                result = processRelationStringHelper(left, right, stringNodeComparator);
            } else {
                result = processRelationBooleanHelper(left, right, context);
            }
        } else {
            result = processRelationNumberHelper(left, right);
        }
        return result;
    }

    private static CompletableFuture<Integer> processRelationToString(
            StringEvalNode left, EvalNode right, Context context,
            Comparator<StringEvalNode> stringNodeComparator, boolean isInvert
    ) {
        final CompletableFuture<EvalNode> rightFuture = RttiUtils.callToString(context, right);
        final int inverter = isInvert ? -1 : 1;
        return rightFuture.thenApply(stringRight ->
                inverter * stringNodeComparator.compare(left, (StringEvalNode) stringRight)
        );
    }

    private static CompletableFuture<Integer> processRelationStringHelper(
            EvalNode left, EvalNode right, Comparator<StringEvalNode> stringNodeComparator
    ) {
        final StringEvalNode stringRight = (StringEvalNode) right;
        final StringEvalNode stringLeft = (StringEvalNode) left;
        return completedFuture(
                stringNodeComparator.compare(stringLeft, stringRight)
        );
    }

    private static CompletableFuture<Integer> processRelationNumberHelper(
            EvalNode left, EvalNode right
    ) {
        final Number rightValue = getNumberValue(right);
        final Number leftValue = getNumberValue(left);
        return completedFuture(
                NUMBER_COMPARATOR.compare(leftValue, rightValue)
        );
    }

    private static CompletableFuture<Integer> processRelationBooleanHelper(
            EvalNode left, EvalNode right, Context context
    ) {
        final CompletableFuture<EvalNode> leftF = RttiUtils.callToBoolean(context, left);
        final CompletableFuture<EvalNode> rightF = RttiUtils.callToBoolean(context, right);

        return leftF.thenCompose(leftBooleanRaw -> rightF.thenApply(rightBooleanRaw ->
                BOOLEAN_EVAL_NODE_COMPARATOR.compare(
                        (BooleanEvalNode) leftBooleanRaw, (BooleanEvalNode) rightBooleanRaw
                )
        ));
    }

    private static EvalNode processRelationComparatorHelper(AstType astType, int compareResult) {
        switch (astType) {
            case AST_LT:
                return new BooleanEvalNode(compareResult < 0);
            case AST_GT:
                return new BooleanEvalNode(compareResult > 0);
            case AST_LE:
                return new BooleanEvalNode(compareResult <= 0);
            case AST_GE:
                return new BooleanEvalNode(compareResult >= 0);
            case AST_EQ:
                return new BooleanEvalNode(compareResult == 0);
            case AST_NEQ:
                return new BooleanEvalNode(compareResult != 0);
        }
        throw new RuntimeException("Unknown type for this case");
    }

}

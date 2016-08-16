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
import ru.histone.v2.Constants;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.NodesComparator;
import ru.histone.v2.evaluator.data.HistoneMacro;
import ru.histone.v2.evaluator.node.*;
import ru.histone.v2.java_compiler.bcompiler.data.MacroFunction;
import ru.histone.v2.parser.node.AstType;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.rtti.RttiMethod;
import ru.histone.v2.utils.AsyncUtils;
import ru.histone.v2.utils.ParserUtils;
import ru.histone.v2.utils.RttiUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.DoubleBinaryOperator;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static ru.histone.v2.evaluator.EvalUtils.isNumberNode;
import static ru.histone.v2.evaluator.EvalUtils.nodeAsBoolean;
import static ru.histone.v2.utils.ParserUtils.tryDouble;
import static ru.histone.v2.utils.ParserUtils.tryLongNumber;

/**
 * @author Alexey Nevinsky
 */
public class StdLibrary {

    private static final Logger LOG = LoggerFactory.getLogger(StdLibrary.class);

    private static final NodesComparator comparator = new NodesComparator(); //todo do init in constructor and remove static modificator

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
                                    Double res = sup.applyAsDouble(lValue, rValue);
                                    Optional<Long> longValue = ParserUtils.tryLongNumber(res);
                                    if (longValue.isPresent()) {
                                        return EvalUtils.getValue(longValue.get());
                                    }
                                    return EvalUtils.getValue(res);
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

    public static boolean toBoolean(CompletableFuture<EvalNode> node) {
        return nodeAsBoolean(node.join());
    }

    public static CompletableFuture<EvalNode> arr(Object... args) {
        return null;
    }

    public static boolean bool(CompletableFuture<EvalNode> apply) {
        return false;
    }

    public static CompletableFuture<StringBuilder> append(Context ctx, CompletableFuture<StringBuilder> csb,
                                                          CompletableFuture<EvalNode> var) {
        if (var == null) {
            return csb;
        }

        return var
                .thenCompose(node -> RttiUtils.callToString(ctx, node)
                        .thenCompose(v -> csb.thenApply(sb -> sb.append(v.getValue())))
                );
    }

    public static CompletableFuture<EvalNode> asString(Context ctx, CompletableFuture<StringBuilder> csb) {
        return csb.thenCompose(sb -> RttiUtils.callToString(ctx, EvalUtils.createEvalNode(sb.toString())));
    }

    public static CompletableFuture<EvalNode> asBooleanNot(Context context, CompletableFuture<EvalNode> node) {
        return node.thenCompose(n -> EvalUtils.getValue(!nodeAsBoolean(n)));
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

    /**
     * Method creates MapEvalNode from arguments: [CompletableFuture<EvalNode>, String]...
     *
     * @param nodes arguments for create map
     * @return completable future of result map
     */
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
                .thenCompose(argList -> {
                    if (argList.size() >= 1) {
                        return ctx.call(argList.get(0), functionName, argList);
                    }
                    return ctx.call(functionName, argList);
                })
                .exceptionally(EvalUtils.checkThrowable(LOG));
    }

    public static CompletableFuture<EvalNode> mCall(Context ctx, CompletableFuture<EvalNode> valueNode, CompletableFuture<EvalNode>... nodes) {
//        final CompletableFuture<EvalNode> valueNode;
//        if (!(callNode.getNode(0) instanceof CallExpAstNode)) {
//            valueNode = evaluateNode(callNode.getNode(0), context);
//        } else {
//            CallExpAstNode n = callNode.getNode(0);
//            if (n.getCallType() == CallType.SIMPLE) {
//                valueNode = processSimpleCall(context, callNode.getNode(0));
//            } else {
//                valueNode = evaluateNode(callNode.getNode(0), context);
//            }
//        }
        final CompletableFuture<List<EvalNode>> argsFuture = AsyncUtils.sequence(nodes);
        return valueNode
                .thenCompose(value -> argsFuture
                        .thenCompose(args -> {
                                    if (value.getType() == HistoneType.T_MACRO) {
                                        List<EvalNode> arguments = new ArrayList<>();
                                        arguments.add(value);
                                        arguments.addAll(args);
                                        return ctx.call(value, RttiMethod.RTTI_M_CALL.getId(), arguments);
                                    }
                                    if (value.getType() != HistoneType.T_STRING) {
                                        return EvalUtils.getValue(null);
                                    }
                                    return ctx.call((String) value.getValue(), args);
                                }
                        )
                );
    }

    public static CompletableFuture<EvalNode> lt(Context ctx, CompletableFuture<EvalNode> first, CompletableFuture<EvalNode> second) {
        return comparator.processRelation(first, second, AstType.AST_LT, ctx);
    }

    public static CompletableFuture<EvalNode> eq(Context ctx, CompletableFuture<EvalNode> first, CompletableFuture<EvalNode> second) {
        return comparator.processRelation(first, second, AstType.AST_EQ, ctx);
    }

    public static CompletableFuture<EvalNode> ge(Context ctx, CompletableFuture<EvalNode> first, CompletableFuture<EvalNode> second) {
        return comparator.processRelation(first, second, AstType.AST_GE, ctx);
    }

    public static CompletableFuture<EvalNode> gt(Context ctx, CompletableFuture<EvalNode> first, CompletableFuture<EvalNode> second) {
        return comparator.processRelation(first, second, AstType.AST_GT, ctx);
    }

    public static CompletableFuture<EvalNode> le(Context ctx, CompletableFuture<EvalNode> first, CompletableFuture<EvalNode> second) {
        return comparator.processRelation(first, second, AstType.AST_LE, ctx);
    }

    public static CompletableFuture<EvalNode> neq(Context ctx, CompletableFuture<EvalNode> first, CompletableFuture<EvalNode> second) {
        return comparator.processRelation(first, second, AstType.AST_NEQ, ctx);
    }

    public static MapEvalNode constructForSelfValue(MapEvalNode array, int currentIndex, int lastIndex) {
        List<Map.Entry<String, EvalNode>> nodes = new ArrayList<>(array.getValue().entrySet());

        Map<String, EvalNode> res = new LinkedHashMap<>();
        res.put(Constants.SELF_CONTEXT_KEY, EvalUtils.createEvalNode(nodes.get(currentIndex).getKey()));
        res.put(Constants.SELF_CONTEXT_VALUE, nodes.get(currentIndex).getValue());
        res.put(Constants.SELF_CONTEXT_CURRENT_INDEX, EvalUtils.createEvalNode(currentIndex));
        res.put(Constants.SELF_CONTEXT_LAST_INDEX, EvalUtils.createEvalNode(lastIndex));
        return (MapEvalNode) EvalUtils.createEvalNode(res);
    }

    public static MapEvalNode constructWhileSelfValue(int currentIndex) {
        Map<String, EvalNode> res = new LinkedHashMap<>();
        res.put(Constants.SELF_WHILE_ITERATION, EvalUtils.createEvalNode(currentIndex));
//        res.put(Constants.SELF_WHILE_CONDITION, nodes.get(currentIndex).getValue());
        return (MapEvalNode) EvalUtils.createEvalNode(res);
    }

    public static CompletableFuture<EvalNode> toMacro(MacroFunction macroFunction, long argsSize) {
        List<String> args = new ArrayList<>();
        for (int i = 1; i <= argsSize; i++) {
            args.add(i + "");
        }

        HistoneMacro f = new HistoneMacro(args, macroFunction, null, Collections.emptyMap(), HistoneMacro.WrappingType.NONE);

        return CompletableFuture.completedFuture(new MacroEvalNode(f));
    }

    public static CompletableFuture<EvalNode> getFromCtx(Context ctx, CompletableFuture<EvalNode> nameNode) {
        return AsyncUtils.sequence(ctx.getValue(Constants.THIS_CONTEXT_VALUE), nameNode)
                .thenApply(list -> {
                    EvalNode fromCtx = list.get(0);
                    if (fromCtx instanceof HasProperties) {
                        String name = (String) list.get(1).getValue();
                        return ((HasProperties) fromCtx).getProperty(name);
                    }
                    return EvalUtils.createEvalNode(null);
                });
    }

    public static CompletableFuture<EvalNode> processLogicalNode(CompletableFuture<EvalNode> first, CompletableFuture<EvalNode> second, boolean negateCheck) {
        return first.thenCompose(f -> {
            if (nodeAsBoolean(f) ^ negateCheck) {
                return CompletableFuture.completedFuture(f);
            }
            return second;
        });
    }

    public static CompletableFuture<EvalNode> processBorNode(CompletableFuture<EvalNode> first,
                                                             CompletableFuture<EvalNode> second) {
        return processBitwiseNode(first, second, (a, b) -> a | b);
    }

    public static CompletableFuture<EvalNode> processBxorNode(CompletableFuture<EvalNode> first,
                                                              CompletableFuture<EvalNode> second) {
        return processBitwiseNode(first, second, (a, b) -> a ^ b);
    }

    public static CompletableFuture<EvalNode> processBandNode(CompletableFuture<EvalNode> first,
                                                              CompletableFuture<EvalNode> second) {
        return processBitwiseNode(first, second, (a, b) -> a & b);
    }

    private static CompletableFuture<EvalNode> processBitwiseNode(CompletableFuture<EvalNode> firstFuture,
                                                                  CompletableFuture<EvalNode> secondFuture,
                                                                  BiFunction<Long, Long, Long> function) {
        return AsyncUtils.sequence(firstFuture, secondFuture)
                .thenApply(f -> {
                    long first = 0;
                    if (f.get(0).getType() == HistoneType.T_NUMBER) {
                        first = (long) f.get(0).getValue();
                    } else if (f.get(0).getType() == HistoneType.T_BOOLEAN) {
                        first = nodeAsBoolean(f.get(0)) ? 1 : 0;
                    }
                    long second = 0;
                    if (f.get(1).getType() == HistoneType.T_NUMBER) {
                        second = (long) f.get(1).getValue();
                    } else if (f.get(1).getType() == HistoneType.T_BOOLEAN) {
                        second = nodeAsBoolean(f.get(1)) ? 1 : 0;
                    }
                    return EvalUtils.createEvalNode(function.apply(first, second));
                });
    }
}

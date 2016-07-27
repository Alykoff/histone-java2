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

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.StringEvalNode;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.AsyncUtils;
import ru.histone.v2.utils.ParserUtils;
import ru.histone.v2.utils.RttiUtils;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static ru.histone.v2.evaluator.EvalUtils.isNumberNode;

/**
 * @author Alexey Nevinsky
 */
public class StdLibrary {

//    public static CompletableFuture<EvalNode> add(CompletableFuture<EvalNode> first, CompletableFuture<EvalNode> second) {
//        CompletableFuture<List<EvalNode>> leftRight = evalAllNodesOfCurrent(node, context);
//        return leftRight.thenCompose(lr -> {
//            EvalNode left = lr.get(0);
//            EvalNode right = lr.get(1);
//            if (!(left.getType() == HistoneType.T_STRING || right.getType() == HistoneType.T_STRING)) {
//                final boolean isLeftNumberNode = isNumberNode(left);
//                final boolean isRightNumberNode = isNumberNode(right);
//                if (isLeftNumberNode && isRightNumberNode) {
//                    final Double res = getValue(left).orElse(null) + getValue(right).orElse(null);
//                    return EvalUtils.getNumberFuture(res);
//                } else if (isLeftNumberNode || isRightNumberNode) {
//                    return getValue(null);
//                }
//
//                if (left.getType() == HistoneType.T_ARRAY && right.getType() == HistoneType.T_ARRAY) {
//                    final MapEvalNode result = new MapEvalNode(new LinkedHashMap<>());
//                    result.append((MapEvalNode) left);
//                    result.append((MapEvalNode) right);
//                    return completedFuture(result);
//                }
//            }
//
//            CompletableFuture<List<EvalNode>> lrFutures = AsyncUtils.sequence(
//                    RttiUtils.callToString(context, left),
//                    RttiUtils.callToString(context, right)
//            );
//            return lrFutures.thenCompose(futures -> {
//                StringEvalNode l = (StringEvalNode) futures.get(0);
//                StringEvalNode r = (StringEvalNode) futures.get(1);
//                return getValue(l.getValue() + r.getValue());
//            });
//        });
//    }

    public static CompletableFuture<EvalNode> sub(CompletableFuture<EvalNode> first, CompletableFuture<EvalNode> second) {
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
                                    return EvalUtils.getValue(lValue - rValue);
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
}

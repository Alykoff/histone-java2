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

package ru.histone.v2.evaluator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.histone.v2.Constants;
import ru.histone.v2.evaluator.data.HistoneMacro;
import ru.histone.v2.evaluator.data.HistoneRegex;
import ru.histone.v2.evaluator.node.*;
import ru.histone.v2.exceptions.HistoneException;
import ru.histone.v2.exceptions.StopExecutionException;
import ru.histone.v2.parser.node.*;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.rtti.RttiMethod;
import ru.histone.v2.utils.AsyncUtils;
import ru.histone.v2.utils.ParserUtils;
import ru.histone.v2.utils.RttiUtils;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static ru.histone.v2.utils.ParserUtils.tryDouble;
import static ru.histone.v2.utils.ParserUtils.tryLongNumber;

/**
 * The main class for evaluating AST tree.
 *
 * @author Alexey Nevinsky
 * @author Gali Alykoff
 */
public class Evaluator implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final NodesComparator comparator;
    private final Converter converter;

    public Evaluator(Converter converter) {
        this.converter = converter;
        comparator = new NodesComparator(converter);
    }

    public String process(ExpAstNode node, Context context) {
        return processFuture(node, context).join();
    }

    public CompletableFuture<String> processFuture(ExpAstNode node, Context context) {
        return evaluateNode(node, context)
                .thenCompose(n -> RttiUtils.callToString(context, n))
                .thenApply(n -> ((StringEvalNode) n).getValue());
    }

    public CompletableFuture<EvalNode> evaluateNode(AstNode node, Context context) {
        if (node == null) {
            return converter.getValue(null);
        }

        if (node.hasValue()) {
            return getValueNode(node);
        }

        ExpAstNode expNode = (ExpAstNode) node;
        switch (node.getType()) {
            case AST_ARRAY:
                return processArrayNode(expNode, context);
            case AST_REGEXP:
                return processRegExp(expNode);
            case AST_THIS:
                return processThisNode(context);
            case AST_GLOBAL:
                return processGlobalNode();
            case AST_NOT:
                return processNotNode(expNode, context);
            case AST_AND:
                return processAndNode(expNode, context);
            case AST_OR:
                return processOrNode(expNode, context);
            case AST_TERNARY:
                return processTernary(expNode, context);
            case AST_ADD:
                return processAddNode(expNode, context);
            case AST_SUB:
            case AST_MUL:
            case AST_DIV:
            case AST_MOD:
                return processArithmetical(expNode, context);
            case AST_USUB:
                return processUnaryMinus(expNode, context);
            case AST_LT:
            case AST_GT:
            case AST_LE:
            case AST_GE:
            case AST_EQ:
            case AST_NEQ:
                return processRelation(expNode, context);
            case AST_REF:
                return processReferenceNode(expNode, context);
            case AST_CALL:
                return processCall(expNode, context);
            case AST_VAR:
                return processVarNode(expNode, context);
            case AST_IF:
                return processIfNode(expNode, context);
            case AST_FOR:
                return processForNode(expNode, context);
            case AST_WHILE:
                return processWhileNode(expNode, context);
            case AST_MACRO:
                return processMacroNode(expNode, context);
            case AST_RETURN:
                return processReturnNode(expNode, context);
            case AST_NODES:
                return processNodeList(expNode, context, true);
            case AST_NODELIST:
                return processNodeList(expNode, context, false);
            case AST_BOR:
                return processBorNode(expNode, context);
            case AST_BXOR:
                return processBxorNode(expNode, context);
            case AST_BAND:
                return processBandNode(expNode, context);
            case AST_SUPPRESS:
                return processSuppressNode(expNode, context);
            case AST_CONTINUE:
                return processBreakContinueNode(expNode, false);
            case AST_BREAK:
                return processBreakContinueNode(expNode, true);
            case AST_NOP:
                return processNopNode();
            case AST_BNOT:
            case AST_BLS:
            case AST_BRS:
                break;
        }
        throw new HistoneException("Unknown AST Histone Type: " + node.getType());
    }

    private CompletableFuture<EvalNode> processNopNode() {
        return converter.getValue(null);
    }

    private CompletableFuture<EvalNode> processBreakContinueNode(ExpAstNode expNode, boolean isBreak) {
        HistoneType type = isBreak ? HistoneType.T_BREAK : HistoneType.T_CONTINUE;
        final EvalNode res;
        if (expNode.size() > 0) {
            res = new BreakContinueEvalNode(type, String.valueOf(((ValueNode) expNode.getNode(0)).getValue()));
        } else {
            res = new BreakContinueEvalNode(type);
        }
        return CompletableFuture.completedFuture(res);
    }

    private CompletableFuture<EvalNode> processSuppressNode(ExpAstNode expNode, Context context) {
        return evaluateNode(expNode.getNode(0), context)
                .exceptionally(e -> {
                    LOG.error(e.getMessage(), e);
                    return converter.createEvalNode(null);
                })
                .thenApply(node -> converter.createEvalNode(null));
    }

    private CompletableFuture<EvalNode> processThisNode(Context context) {
        return context.getValue(Constants.THIS_CONTEXT_VALUE);
    }

    private CompletableFuture<EvalNode> processReturnNode(ExpAstNode expNode, Context context) {
        return evaluateNode(expNode.getNode(0), context).thenApply(EvalNode::getReturned);
    }

    private CompletableFuture<EvalNode> processGlobalNode() {
        return completedFuture(new GlobalEvalNode());
    }

    private CompletableFuture<EvalNode> processNotNode(ExpAstNode expNode, Context context) {
        CompletableFuture<EvalNode> nodeFuture = evaluateNode(expNode.getNode(0), context);
        return nodeFuture.thenApply(n -> new BooleanEvalNode(!converter.nodeAsBoolean(n)));
    }

    /**
     * [AST_ID, [LINKS_TO_EXTERNAL_VARS], MACRO_BODY, NUM_OF_VARS, VARS...]
     */
    private CompletableFuture<EvalNode> processMacroNode(ExpAstNode node, Context context) {
        final int bodyIndex = 1;
        final int numVarIndex = 2;
        final int startVarIndex = 3;
        final Context cloneContext = context.clone();
        final CompletableFuture<List<AstNode>> astArgsFuture = completedFuture(
                node.size() < startVarIndex
                        ? Collections.emptyList()
                        : node.getNodes().subList(startVarIndex, node.size())
        );
        return astArgsFuture.thenApply(astNodes -> {
            final List<String> args = new ArrayList<>();
            if (node.getNode(numVarIndex) != null) {
                LongEvalNode size = (LongEvalNode) evaluateNode(node.getNode(numVarIndex), context).join();
                for (long i = 1; i <= size.getValue(); i++) {
                    args.add(i + "");
                }
            }
            final Map<String, CompletableFuture<EvalNode>> argsDefaultValues = new HashMap<>();
            for (int i = 0; i < astNodes.size() / 2; i++) {
                long offset = (long) evaluateNode(astNodes.get(i * 2), context).join().getValue();
                CompletableFuture<EvalNode> defValue = evaluateNode(astNodes.get(i * 2 + 1), context);
                argsDefaultValues.put(offset + 1 + "", defValue);
            }
            final AstNode body = node.getNode(bodyIndex);
            return new MacroEvalNode(
                    new HistoneMacro(args, body, cloneContext, argsDefaultValues, HistoneMacro.WrappingType.NONE)
            );
        });
    }

    private CompletableFuture<EvalNode> processTernary(ExpAstNode expNode, Context context) {
        CompletableFuture<EvalNode> condition = evaluateNode(expNode.getNode(0), context);
        return condition.thenCompose(conditionNode -> {
            if (converter.nodeAsBoolean(conditionNode)) {
                return evaluateNode(expNode.getNode(1), context);
            } else if (expNode.getNode(2) != null) {
                return evaluateNode(expNode.getNode(2), context);
            }
            return converter.getValue(null);
        });
    }

    private CompletableFuture<EvalNode> processCall(ExpAstNode expNode, Context context) {
        final CallExpAstNode callNode = (CallExpAstNode) expNode;
        if (callNode.getCallType() == CallType.RTTI_M_GET) {
            final CompletableFuture<EvalNode> valueNode = evaluateNode(callNode.getNode(0), context);
            final CompletableFuture<EvalNode> fieldNode = evaluateNode(callNode.getNode(1), context);
            return valueNode
                    .thenCompose(value -> fieldNode
                            .thenCompose(fieldName ->
                                    context.call(value, RttiMethod.RTTI_M_GET.getId(), Arrays.asList(value, fieldName))
                            )
                    );
        } else if (callNode.getCallType() == CallType.RTTI_M_CALL) {
            return processMethodCall(context, callNode);
        } else {
            return processSimpleCall(context, callNode);
        }
    }

    private CompletableFuture<EvalNode> processMethodCall(Context context, CallExpAstNode callNode) {
        if (callNode.getNode(0) instanceof ValueNode) {
            return converter.getValue(null);
        }

        final CompletableFuture<EvalNode> valueNode;
        if (!(callNode.getNode(0) instanceof CallExpAstNode)) {
            valueNode = evaluateNode(callNode.getNode(0), context);
        } else {
            CallExpAstNode n = callNode.getNode(0);
            if (n.getCallType() == CallType.SIMPLE) {
                valueNode = processSimpleCall(context, callNode.getNode(0));
            } else {
                valueNode = evaluateNode(callNode.getNode(0), context);
            }
        }

        final List<AstNode> paramsAstNodes = callNode.getNodes().subList(1, callNode.getNodes().size());
        final CompletableFuture<List<EvalNode>> argsFuture = AsyncUtils.sequence(paramsAstNodes.stream()
                .map(x -> evaluateNode(x, context))
                .collect(Collectors.toList()));
        return valueNode
                .thenCompose(value -> argsFuture
                        .thenCompose(args -> {
                                    if (value.getType() == HistoneType.T_MACRO) {
                                        List<EvalNode> arguments = new ArrayList<>();
                                        arguments.add(value);
                                        arguments.addAll(args);
                                        return context.call(value, RttiMethod.RTTI_M_CALL.getId(), arguments);
                                    }
                                    if (value.getType() != HistoneType.T_STRING) {
                                        return converter.getValue(null);
                                    }
                                    return context.call((String) value.getValue(), args);
                                }
                        )
                );
    }

    private CompletableFuture<EvalNode> processSimpleCall(Context context, ExpAstNode expNode) {
        if (expNode.size() == 2) {
            return evaluateNode(expNode.getNode(1), context)
                    .thenCompose(fNameNode -> {
                        //we call function without args or getting value from context
                        if (fNameNode.getType() == HistoneType.T_NULL || fNameNode.getType() == HistoneType.T_UNDEFINED) {
                            return converter.getValue(null);
                        }

                        final String name = (String) fNameNode.getValue();
                        CompletableFuture<EvalNode> res = getValueFromContextOrNull(context, null, name);
                        if (res != null) {
                            return res;
                        }
                        return evaluateNode(expNode.getNode(0), context)
                                .thenCompose(value -> context.call(value, name, Collections.singletonList(value)));
                    })
                    .exceptionally(checkThrowable());
        }

        CompletableFuture<List<EvalNode>> nodesFuture = evalAllNodesOfCurrent(expNode, context);
        return nodesFuture.thenCompose(nodes -> {
            final String name = (String) nodes.get(1).getValue();

            final EvalNode value = nodes.get(0);
            List<EvalNode> args = new ArrayList<>(nodes.size() - 1);
            args.add(value);
            args.addAll(nodes.subList(2, nodes.size()));
            return context.call(value, name, args);
        }).exceptionally(checkThrowable());
    }

    private java.util.function.Function<Throwable, EvalNode> checkThrowable() {
        return e -> {
            if (e.getCause() instanceof StopExecutionException) {
                throw (StopExecutionException) e.getCause();
            }
            LOG.error(e.getMessage(), e);
            return converter.createEvalNode(null);
        };
    }

    private CompletableFuture<EvalNode> processWhileNode(ExpAstNode expNode, Context context) {
        // [BODY, CONDITION]
        final int bodyIndex = 0;
        final int conditionIndex = 1;
        final AstNode bodyAstNode = expNode.getNode(bodyIndex);
        final boolean isConditionExists = expNode.size() > 1;
        final AstNode conditionNode = isConditionExists
                ? expNode.getNode(conditionIndex)
                : new BooleanAstNode(true);

        return AsyncUtils.initFuture().thenApply(ignore -> {
            final StringBuilder acc = new StringBuilder();
            long counter = 0;
            while (true) {
                final EvalNode conditionEvalNode = evaluateNode(conditionNode, context).join();
                final Boolean condition = RttiUtils.callToBooleanResult(context, conditionEvalNode).join();
                if (!condition) {
                    break;
                }
                final Context ownContext = createWhileContext(context, conditionEvalNode, counter);
                final EvalNode bodyNode = evaluateNode(bodyAstNode, ownContext).join();
                if (bodyNode.isReturn()) {
                    return bodyNode;
                }

                acc.append(
                        RttiUtils.callToStringResult(context, bodyNode).join()
                );

                if (bodyNode.getType() == HistoneType.T_BREAK) {
                    break;
                }
                counter++;
            }
            return converter.createEvalNode(acc.toString());
        });
    }

    private Context createWhileContext(Context context, EvalNode condition, long counter) {
        Context iterableContext = context.createNew();
        final Map<String, EvalNode> selfVars = new LinkedHashMap<>();
        selfVars.put(Constants.SELF_WHILE_ITERATION, converter.createEvalNode(counter));
        selfVars.put(Constants.SELF_WHILE_CONDITION, condition);
        iterableContext.put(Constants.SELF_CONTEXT_NAME, converter.getValue(selfVars));
        return iterableContext;
    }

    private CompletableFuture<EvalNode> processForNode(ExpAstNode expNode, Context context) {
        // [KEY_NODE, VAR_NODE, LIST_NODES, ARRAY_NODE, [CONDITIONS_NODES, BODIES_NODES, ...], [ELSE_BODIES_NODE]]
        final AstNode iterator = expNode.getNode(3); // get array for iterate it in loop
        return evaluateNode(iterator, context).thenCompose(objToIterate -> {
            if (!(objToIterate instanceof MapEvalNode) || ((MapEvalNode) objToIterate).getValue().size() == 0) {
                return processNonMapValue(expNode, context);
            } else {
                return processMapValue(expNode, context, (MapEvalNode) objToIterate);
            }
        });
    }

    private CompletableFuture<EvalNode> processNonMapValue(ExpAstNode expNode, Context context) {
        if (expNode.size() == 3) {
            return converter.getValue(null);
        }
        int i = 4;
        AstNode expressionNode = expNode.getNode(i + 1);
        AstNode bodyNode = expNode.getNode(i);
        while (expressionNode != null) {
            CompletableFuture<EvalNode> conditionFuture = evaluateNode(expressionNode, context);
            EvalNode conditionNode = conditionFuture.join();
            if (converter.nodeAsBoolean(conditionNode)) {
                return evaluateNode(bodyNode, context);
            }
            i += 2;
            expressionNode = expNode.getNode(i + 1);
            bodyNode = expNode.getNode(i);
        }
        if (bodyNode != null) {
            return evaluateNode(bodyNode, context);
        }

        return converter.getValue(null);
    }

    private CompletableFuture<EvalNode> processMapValue(ExpAstNode expNode, Context context, MapEvalNode objToIterate) {
        CompletableFuture<EvalNode> keyVarName = evaluateNode(expNode.getNode(0), context);
        CompletableFuture<EvalNode> valueVarName = evaluateNode(expNode.getNode(1), context);
        CompletableFuture<List<EvalNode>> leftRightDone = AsyncUtils.sequence(keyVarName, valueVarName);
        CompletableFuture<EvalNode> res = leftRightDone.thenCompose(keyValueNames ->
                iterate(
                        expNode,
                        context,
                        objToIterate,
                        keyValueNames.get(0),
                        keyValueNames.get(1)
                )
        );
        return res.thenApply(node -> {
            if (node.getType() == HistoneType.T_BREAK) {
                String v = ((BreakContinueEvalNode) node).getValue();
                return new StringEvalNode(v);
            }
            return node;
        });
    }

    private CompletableFuture<EvalNode> iterate(
            ExpAstNode expNode, Context context, MapEvalNode
            objToIterate, EvalNode keyVarName, EvalNode valueVarName
    ) {
        final Map<String, EvalNode> value = objToIterate.getValue();
        int i = 0;
        CompletableFuture<EvalNode> res = null;
        for (Map.Entry<String, EvalNode> entry : value.entrySet()) {
            Context iterableContext = getIterableContext(context, objToIterate, keyVarName, valueVarName, i, entry);
            if (res != null) {
                res = res.thenCompose(node -> {
                    if (node.isReturn() || node.getType() == HistoneType.T_BREAK) {
                        return getEvaluatedString(context, node, null);
                    } else {
                        return getEvaluatedString(context, node, evaluateNode(expNode.getNode(2), iterableContext));
                    }
                });
            } else {
                res = getEvaluatedString(context, null, evaluateNode(expNode.getNode(2), iterableContext));
            }
            i++;
        }
        return res;
    }

    private CompletableFuture<EvalNode> getEvaluatedString(Context ctx, EvalNode node,
                                                           CompletableFuture<EvalNode> evalNodeCompletableFuture) {
        if (evalNodeCompletableFuture == null) {
            if (node.isReturn()) {
                return CompletableFuture.completedFuture(node.getReturned());
            }
            return converter.getValue(node);
        }

        return evalNodeCompletableFuture.thenApply(fNode -> {
            if (fNode.isReturn()) {
                return fNode;
            }
            if (fNode.getType() == HistoneType.T_CONTINUE || fNode.getType() == HistoneType.T_BREAK) {
                if (node == null) {
                    return fNode;
                }
                final String value;
                if (fNode.getValue() != null) {
                    value = (String) node.getValue() + fNode.getValue();
                } else {
                    value = (String) node.getValue();
                }
                return new BreakContinueEvalNode((BreakContinueEvalNode) fNode, value);
            }
            StringEvalNode second = (StringEvalNode) RttiUtils.callToString(ctx, fNode).join();
            if (node == null) {
                return second;
            }
            final String firstValue = (String) node.getValue();
            return converter.constructFromObject(firstValue + second.getValue());
        });
    }

    private Context getIterableContext(Context context, MapEvalNode objToIterate, EvalNode keyVarName,
                                       EvalNode valueVarName, int i, Map.Entry<String, EvalNode> entry) {
        Context iterableContext = context.createNew();
        if (valueVarName.getValue() != ObjectUtils.NULL) {
            iterableContext.put(valueVarName.getValue() + "", converter.getValue(entry.getValue()));
        }
        if (keyVarName.getValue() != ObjectUtils.NULL) {
            iterableContext.put(keyVarName.getValue() + "", converter.getValue(entry.getKey()));
        }
        iterableContext.put(Constants.SELF_CONTEXT_NAME, converter.getValue(constructSelfValue(
                entry.getKey(), entry.getValue(), i, objToIterate.getValue().entrySet().size() - 1
        )));
        return iterableContext;
    }

    private Map<String, EvalNode> constructSelfValue(String key, Object value, long currentIndex, long lastIndex) {
        Map<String, EvalNode> res = new LinkedHashMap<>();
        res.put(Constants.SELF_CONTEXT_KEY, new StringEvalNode(key));
        res.put(Constants.SELF_CONTEXT_VALUE, converter.createEvalNode(value));
        res.put(Constants.SELF_CONTEXT_CURRENT_INDEX, new LongEvalNode(currentIndex));
        res.put(Constants.SELF_CONTEXT_LAST_INDEX, new LongEvalNode(lastIndex));
        return res;
    }

    private CompletableFuture<EvalNode> processAddNode(ExpAstNode node, Context context) {
        CompletableFuture<List<EvalNode>> leftRight = evalAllNodesOfCurrent(node, context);
        return leftRight.thenCompose(lr -> {
            EvalNode left = lr.get(0);
            EvalNode right = lr.get(1);
            if (!(left.getType() == HistoneType.T_STRING || right.getType() == HistoneType.T_STRING)) {
                final boolean isLeftNumberNode = converter.isNumberNode(left);
                final boolean isRightNumberNode = converter.isNumberNode(right);
                if (isLeftNumberNode && isRightNumberNode) {
                    final Double res = getValue(left).orElse(null) + getValue(right).orElse(null);
                    return converter.getNumberFuture(res);
                } else if (isLeftNumberNode || isRightNumberNode) {
                    return converter.getValue(null);
                }

                if (left.getType() == HistoneType.T_ARRAY && right.getType() == HistoneType.T_ARRAY) {
                    final MapEvalNode result = new MapEvalNode(new LinkedHashMap<>());
                    result.append((MapEvalNode) left);
                    result.append((MapEvalNode) right);
                    return completedFuture(result);
                }
            }

            CompletableFuture<List<EvalNode>> lrFutures = AsyncUtils.sequence(
                    RttiUtils.callToString(context, left),
                    RttiUtils.callToString(context, right)
            );
            return lrFutures.thenCompose(futures -> {
                StringEvalNode l = (StringEvalNode) futures.get(0);
                StringEvalNode r = (StringEvalNode) futures.get(1);
                return converter.getValue(l.getValue() + r.getValue());
            });
        });
    }

    private CompletableFuture<EvalNode> processArithmetical(ExpAstNode node, Context context) {
        if (CollectionUtils.isNotEmpty(node.getNodes()) && node.getNodes().size() == 2) {
            return evaluateNode(node.getNodes().get(0), context).thenCompose(leftNode -> {
                if (converter.isNumberNode(leftNode) || leftNode.getType() == HistoneType.T_STRING) {
                    Double lValue = getValue(leftNode).orElse(null);
                    if (lValue != null) {
                        return evaluateNode(node.getNodes().get(1), context).thenCompose(rightNode -> {
                            if (converter.isNumberNode(rightNode) || rightNode.getType() == HistoneType.T_STRING) {
                                return CompletableFuture.completedFuture(getValue(rightNode).orElse(null));
                            }
                            return CompletableFuture.completedFuture(null);
                        }).thenCompose(rValue -> {
                            if (rValue != null) {
                                Double res = null;
                                AstType type = node.getType();
                                switch (type) {
                                    case AST_SUB:
                                        res = lValue - rValue;
                                        break;
                                    case AST_MUL:
                                        res = lValue * rValue;
                                        break;
                                    case AST_DIV:
                                        res = lValue / rValue;
                                        break;
                                    case AST_MOD:
                                        res = lValue % rValue;
                                        break;
                                }
                                return converter.getNumberFuture(res);
                            }
                            return converter.getValue(null);
                        });
                    }
                }
                return converter.getValue(null);
            });
        }
        return converter.getValue(null);
    }

    private Optional<Double> getValue(EvalNode node) { // TODO duplicate ???
        if (node.getType() == HistoneType.T_STRING) {
            return ParserUtils.tryDouble(((StringEvalNode) node).getValue());
        } else {
            return Optional.of(Double.valueOf(node.getValue() + ""));
        }
    }

    private CompletableFuture<EvalNode> processRelation(ExpAstNode node, Context context) {
        CompletableFuture<EvalNode> left = evaluateNode(node.getNode(0), context);
        CompletableFuture<EvalNode> right = evaluateNode(node.getNode(1), context);
        return comparator.processRelation(left, right, node.getType(), context);
    }

    private CompletableFuture<EvalNode> processBorNode(ExpAstNode node, Context context) {
        return processBitwiseNode(node, context, (a, b) -> a | b);
    }

    private CompletableFuture<EvalNode> processBxorNode(ExpAstNode node, Context context) {
        return processBitwiseNode(node, context, (a, b) -> a ^ b);
    }

    private CompletableFuture<EvalNode> processBandNode(ExpAstNode node, Context context) {
        return processBitwiseNode(node, context, (a, b) -> a & b);
    }

    private CompletableFuture<EvalNode> processBitwiseNode(ExpAstNode node, Context context,
                                                           BiFunction<Long, Long, Long> function) {
        CompletableFuture<List<EvalNode>> leftRightDone = evalAllNodesOfCurrent(node, context);
        return leftRightDone.thenApply(f -> {
            long first = 0;
            if (f.get(0).getType() == HistoneType.T_NUMBER) {
                first = (long) f.get(0).getValue();
            } else if (f.get(0).getType() == HistoneType.T_BOOLEAN) {
                first = converter.nodeAsBoolean(f.get(0)) ? 1 : 0;
            }
            long second = 0;
            if (f.get(1).getType() == HistoneType.T_NUMBER) {
                second = (long) f.get(1).getValue();
            } else if (f.get(1).getType() == HistoneType.T_BOOLEAN) {
                second = converter.nodeAsBoolean(f.get(1)) ? 1 : 0;
            }
            return converter.createEvalNode(function.apply(first, second));
        });
    }

    private CompletableFuture<EvalNode> processVarNode(ExpAstNode node, Context context) {
        CompletableFuture<EvalNode> valueNameFuture = evaluateNode(node.getNode(1), context);
        CompletableFuture<EvalNode> valueNodeFuture = evaluateNode(node.getNode(0), context)
                .thenCompose(value -> {
                    if (node.getNode(0).getType() == AstType.AST_NODES) {
                        return RttiUtils.callToString(context, value.clearReturned());
                    }
                    return CompletableFuture.completedFuture(value.clearReturned());
                });

        return valueNameFuture.thenApply(f -> {
            context.put(f.getValue() + "", valueNodeFuture);
            return converter.createEvalNode(null);
        });
    }

    private CompletableFuture<EvalNode> processArrayNode(ExpAstNode node, Context context) {
        if (CollectionUtils.isEmpty(node.getNodes())) {
            return completedFuture(new MapEvalNode(new LinkedHashMap<>(0)));
        }
        if (node.getNode(0).getType() == AstType.AST_VAR) {
            return evalAllNodesOfCurrent(node, context).thenApply(evalNodes -> converter.createEvalNode(null));
        }
        //todo do check and refactorings
        if (node.size() > 0) {
            CompletableFuture<List<EvalNode>> futures = evalAllNodesOfCurrent(node, context);
            return futures.thenApply(nodes -> {
                Map<String, EvalNode> map = new LinkedHashMap<>();
                for (int i = 0; i < nodes.size() / 2; i++) {
                    EvalNode value = nodes.get(i * 2);
                    EvalNode key = nodes.get(i * 2 + 1);
                    map.put(key.getValue() + "", value);
                }
                return new MapEvalNode(map);
            });
        } else {
            return completedFuture(new MapEvalNode(new LinkedHashMap<>()));
        }
    }

    private CompletableFuture<EvalNode> processUnaryMinus(ExpAstNode node, Context context) {
        CompletableFuture<EvalNode> res = evaluateNode(node.getNode(0), context);
        return res.thenApply(n -> {
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
            return converter.createEvalNode(null);
        });
    }

    private CompletableFuture<EvalNode> getValueNode(AstNode node) {
        ValueNode valueNode = (ValueNode) node;
        if (valueNode.getValue() == null) {
            return converter.getValue(ObjectUtils.NULL);
        }

        Object val = valueNode.getValue();
        if (val instanceof Boolean) {
            return completedFuture(new BooleanEvalNode((Boolean) val));
        } else if (val instanceof Long) {
            return completedFuture(new LongEvalNode((Long) val));
        } else if (val instanceof Double) {
            return completedFuture(new DoubleEvalNode((Double) val));
        }
        return completedFuture(new StringEvalNode(val + ""));
    }

    private CompletableFuture<EvalNode> processReferenceNode(ExpAstNode node, Context context) {
        //todo
        final int ctxNumber = ((LongEvalNode) evaluateNode(node.getNode(0), context).join()).getValue().intValue();
        final String valueName = ((ValueNode) node.getNode(1)).getValue() + "";
        CompletableFuture<EvalNode> value = getValueFromContext(context, ctxNumber, valueName);
        return value.thenCompose(v -> {
            if (v != null) {
                if (v.getType() == HistoneType.T_UNDEFINED && context.findFunction(valueName)) {
                    return context.call(valueName, Collections.emptyList());
                }
                return completedFuture(v);
            } else {
                return converter.getValue(null);
            }
        });
    }

    private CompletableFuture<EvalNode> getValueFromContextOrNull(Context context, Integer ctxNumber, String valueName) {
        int i = 0;
        while (context != null) {
            if ((ctxNumber == null || i == ctxNumber) && context.getVars().containsKey(valueName)) {
                return context.getValue(valueName);
            }
            context = context.getParent();
            i++;
        }
        return null;
    }

    private CompletableFuture<EvalNode> getValueFromContext(Context context, int ctxNumber, String valueName) {
        CompletableFuture<EvalNode> res = getValueFromContextOrNull(context, ctxNumber, valueName);
        return res != null ? res : converter.getValue(null);
    }

    private CompletableFuture<EvalNode> processAndNode(ExpAstNode node, Context context) {
        return processLogicalNode(node, context, true);
    }

    private CompletableFuture<EvalNode> processOrNode(ExpAstNode node, Context context) {
        return processLogicalNode(node, context, false);
    }

    private CompletableFuture<EvalNode> processLogicalNode(ExpAstNode node, Context context, boolean negateCheck) {
        if (CollectionUtils.isNotEmpty(node.getNodes()) && node.getNodes().size() == 2) {
            return evaluateNode(node.getNodes().get(0), context).thenCompose(leftNode -> {
                if (converter.nodeAsBoolean(leftNode) ^ negateCheck) {
                    return CompletableFuture.completedFuture(leftNode);
                }
                return evaluateNode(node.getNodes().get(1), context);
            });
        }
        return converter.getValue(null);
    }

    /**
     * Method evaluate AST_NODES or AST_NODELIST node. If {@param createContext} is true, then this is block of code and
     * return node must return value only from this block, else return from template
     *
     * @param node
     * @param context
     * @param createContext
     * @return future of processed node
     */
    private CompletableFuture<EvalNode> processNodeList(ExpAstNode node, Context context, boolean createContext) {
        final Context ctx = createContext ? context.createNew() : context;
        if (node.getNodes().size() == 1) {
            AstNode node1 = node.getNode(0);
            return evaluateNode(node1, ctx);
        } else if (node.size() > 0) {
            CompletableFuture<EvalNode> res = getEvaluatedString(ctx, null, evaluateNode(node.getNode(0), ctx));
            for (int i = 1; i < node.size(); i++) {
                AstNode n = node.getNode(i);
                res = res.thenCompose(rNode -> {
                    if (rNode.isReturn()) {
                        return completedFuture(rNode);
                    } else if (rNode.getType() == HistoneType.T_BREAK
                            || rNode.getType() == HistoneType.T_CONTINUE) {
                        return getEvaluatedString(ctx, rNode, null);
                    } else {
                        CompletableFuture<EvalNode> future = evaluateNode(n, ctx);
                        return getEvaluatedString(ctx, rNode, future);
                    }
                });
            }
            return res.thenApply(n -> {
                if (createContext) {
                    return n.clearReturned();
                }
                return n;
            });
        }
        return converter.getValue("");
    }

    private CompletableFuture<List<EvalNode>> evalAllNodesOfCurrent(ExpAstNode node, Context context) {
        final List<CompletableFuture<EvalNode>> futures = node.getNodes()
                .stream()
                .map(currNode -> evaluateNode(currNode, context))
                .collect(Collectors.toList());
        return AsyncUtils.sequence(futures);
    }

    private CompletableFuture<EvalNode> processIfNode(ExpAstNode node, Context context) {
        // [[BODY, CONDITIONS...], [ELSE_BODY]]
        final int initStep = 0;
        return processIfNodeHelper(node, context, initStep);
    }

    private CompletableFuture<EvalNode> processIfNodeHelper(ExpAstNode node, Context context, int i) {
        final AstNode bodyNode = node.getNode(i);
        final AstNode conditionNode = node.getNode(i + 1);
        if (conditionNode == null) {
            return evaluateNode(bodyNode, context.createNew());
        }

        return evaluateNode(conditionNode, context).thenCompose(condNode ->
                RttiUtils.callToBooleanResult(context, condNode).thenCompose(predicate -> {
                    if (predicate) {
                        return evaluateNode(bodyNode, context.createNew());
                    } else {
                        return processIfNodeHelper(node, context, i + 2);
                    }
                })
        );
    }

    private CompletableFuture<EvalNode> processRegExp(ExpAstNode node) {
        return AsyncUtils.initFuture().thenApply(ignore -> {
            final StringAstNode flagsNumNode = node.getNode(1);

            boolean isIgnoreCase = false;
            boolean isMultiline = false;
            boolean isGlobal = false;
            int flags = 0;

            if (flagsNumNode != null) {
                final String flagStr = flagsNumNode.getValue();

                isIgnoreCase = flagStr.contains("i");
                isMultiline = flagStr.contains("m");
                isGlobal = flagStr.contains("g");
            }

            final StringAstNode expNode = node.getNode(0);
            final String exp = expNode.getValue();
            final Pattern pattern = Pattern.compile(exp, flags);
            return new RegexEvalNode(new HistoneRegex(isGlobal, isIgnoreCase, isMultiline, pattern));
        });
    }

}
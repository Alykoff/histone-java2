package ru.histone.v2.evaluator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.NotImplementedException;
import ru.histone.HistoneException;
import ru.histone.v2.evaluator.data.HistoneRegex;
import ru.histone.v2.evaluator.global.NumberComparator;
import ru.histone.v2.evaluator.node.*;
import ru.histone.v2.parser.node.*;
import ru.histone.v2.utils.ParserUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collector;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.histone.v2.evaluator.EvalUtils.*;
import static ru.histone.v2.utils.AsyncUtils.*;

/**
 * Created by alexey.nevinsky on 12.01.2016.
 */
public class Evaluator {
    private final static Comparator<Number> NUMBER_COMPARATOR = new NumberComparator();

    public String process(String baseUri, ExpAstNode node, Context context) throws HistoneException {
        context.setBaseUri(baseUri);
        return processInternal(node, context);
    }

    private String processInternal(ExpAstNode node, Context context) throws HistoneException {
        CompletableFuture<EvalNode> res = evaluateNode(node, context);
        return res
                .thenApply(n -> n.getValue() != null ? n.getValue().toString() : "")
                .getNow("");
    }

    private CompletableFuture<EvalNode> evaluateNode(AstNode node, Context context) {
        if (node == null) {
            return CompletableFuture.completedFuture(EmptyEvalNode.INSTANCE);
        }

        if (node.hasValue()) {
            return getValueNode(node);
        }

        ExpAstNode expNode = (ExpAstNode) node;
        switch (node.getType()) {
            case AST_ARRAY:
                return processArrayNode(expNode, context);
            case AST_REGEXP:
                return processRegExp(expNode, context);
            case AST_THIS:
                break;
            case AST_GLOBAL:
                break;
            case AST_NOT:
                break;
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
                return processRelation(expNode, context);
            case AST_EQ:
                return processEqNode(expNode, context, true);
            case AST_NEQ:
                return processEqNode(expNode, context, false);
            case AST_REF:
                return processReferenceNode(expNode, context);
            case AST_METHOD:
                return processMethod(expNode, context);
            case AST_PROP:
                return processPropertyNode(expNode, context);
            case AST_CALL:
                return processCall(expNode, context);
            case AST_VAR:
                return processVarNode(expNode, context);
            case AST_IF:
                return processIfNode(expNode, context);
            case AST_FOR:
                return processForNode(expNode, context);
            case AST_MACRO:
                return processMacroNode(expNode, context);
            case AST_RETURN:
                break;
            case AST_NODES:
                return processNodeList(expNode, context.createNew());
            case AST_NODELIST:
                return processNodeList(expNode, context);
            case AST_BOR:
                return processBorNode(expNode, context);
            case AST_BXOR:
                return processBxorNode(expNode, context);
            case AST_BAND:
                return processBandNode(expNode, context);
            case AST_SUPRESS:
                break;
            case AST_LISTEN:
                break;
            case AST_TRIGGER:
                break;
        }
        throw new HistoneException("WTF!?!?!? " + node.getType());

    }

    private CompletableFuture<EvalNode> processMacroNode(ExpAstNode node, Context context) {
        final long param;
        if (node.size() >= 1) {
            final LongAstNode paramNode = node.getNode(1);
            param = paramNode.getValue();
        } else {
            param = 0;
        }
        throw new NotImplementedException();
    }

    private CompletableFuture<EvalNode> processMethod(ExpAstNode expNode, Context context) throws HistoneException {
        final int valueIndex = 0;
        final int methodIndex = 1;
        final int startArgsIndex = 2;
        final List<CompletableFuture<EvalNode>> evalNodes = expNode.getNodes()
                .stream()
                .map(node -> evaluateNode(node, context))
                .collect(Collectors.toList());
        final CompletableFuture<List<EvalNode>> nodesFuture = sequence(evalNodes);

        return nodesFuture.thenCompose(nodes -> {
            final EvalNode valueNode = nodes.get(valueIndex);
            final EvalNode methodNode = nodes.get(methodIndex);
            final List<EvalNode> argsNode = new ArrayList<>();
            argsNode.add(valueNode);
            argsNode.addAll(nodes.subList(startArgsIndex, nodes.size()));
            final Function f = context.getFunction(valueNode, methodNode.asString());
            return f.execute(argsNode);
        });
    }

    private EvalNode processMethod(ExpAstNode expNode, Context context, List<EvalNode> args) throws HistoneException {
        throw new NotImplementedException();
//        EvalNode valueNode = evaluateNode(expNode.getNode(0), context);
//        EvalNode methodNode = evaluateNode(expNode.getNode(1), context);
//
//        final List<EvalNode> allParams = new ArrayList<>();
//        allParams.add(valueNode);
//        allParams.addAll(args);
//
//        Function f = context.getFunction(valueNode, methodNode.asString());
//        return f.execute(allParams);
    }

    private CompletableFuture<EvalNode> processTernary(ExpAstNode expNode, Context context) throws HistoneException {
        CompletableFuture<EvalNode> condition = evaluateNode(expNode.getNode(0), context);
        return condition.thenCompose(conditionNode -> {
            if (nodeAsBoolean(conditionNode)) {
                return evaluateNode(expNode.getNode(1), context);
            } else if (expNode.getNode(2) != null) {
                return evaluateNode(expNode.getNode(2), context);
            }
            return CompletableFuture.completedFuture(EmptyEvalNode.INSTANCE);
        });
    }

    private CompletableFuture<EvalNode> processPropertyNode(ExpAstNode expNode, Context context) throws HistoneException {
        CompletableFuture<EvalNode> valueFuture = evaluateNode(expNode.getNode(0), context);
        CompletableFuture<EvalNode> propertyNameFuture = evaluateNode(expNode.getNode(1), context);

        CompletableFuture<List<EvalNode>> leftRightDone = sequence(valueFuture, propertyNameFuture);

        return leftRightDone.thenApply(futures -> {
            Object obj = ((MapEvalNode) futures.get(0)).getProperty((String) futures.get(1).getValue());
            return createEvalNode(obj);
        });
    }

    private CompletableFuture<EvalNode> processCall(ExpAstNode expNode, Context context) throws HistoneException {
        throw new NotImplementedException();
//        final ExpAstNode node = expNode.getNode(0);
//        final EvalNode functionNameNode = evaluateNode((node).getNode(0), context);
//        final List<AstNode> paramsAstNodes = expNode.getNodes().subList(1, expNode.getNodes().size());
//        final List<EvalNode> paramsNodes = toEvalNodes(paramsAstNodes, context);
//        if (functionNameNode instanceof StringEvalNode) {
//            final Function function = context.getFunction((String) functionNameNode.getValue());
//            return function.execute(paramsNodes);
//        } else {
//            return processMethod(node, context, paramsNodes);
//        }
    }

    private List<EvalNode> toEvalNodes(List<AstNode> astNodes, Context context) throws HistoneException {
        List<EvalNode> res = new ArrayList<>();
//        for (AstNode node : astNodes) {
//            res.add(evaluateNode(node, context));
//        }
//        return res;
        throw new NotImplementedException();
    }

    private CompletableFuture<EvalNode> processForNode(ExpAstNode expNode, Context context) throws HistoneException {
        CompletableFuture<EvalNode> objToIterateFuture = evaluateNode(expNode.getNode(2), context);
        return objToIterateFuture.thenCompose(objToIterate -> {
            if (!(objToIterate instanceof MapEvalNode)) {
                return processNonMapValue(expNode, context);
            } else {
                return processMapValue(expNode, context, (MapEvalNode) objToIterate);
            }
        });
    }

    private CompletionStage<EvalNode> processNonMapValue(ExpAstNode expNode, Context context) {
        if (expNode.size() == 4) {
            return CompletableFuture.completedFuture(EmptyEvalNode.INSTANCE);
        }
        int i = 0;
        ExpAstNode expressionNode = expNode.getNode(i + 4);
        ExpAstNode bodyNode = expNode.getNode(i + 5);
        while (bodyNode != null) {
            CompletableFuture<EvalNode> conditionFuture = evaluateNode(expressionNode, context);
            EvalNode conditionNode = conditionFuture.join();
            if (nodeAsBoolean(conditionNode)) {
                return evaluateNode(bodyNode, context);
            }
            i++;
            expressionNode = expNode.getNode(i + 4);
            bodyNode = expNode.getNode(i + 5);
        }
        if (expressionNode != null) {
            return evaluateNode(expressionNode, context);
        }

        return CompletableFuture.completedFuture(EmptyEvalNode.INSTANCE);
    }

    private CompletableFuture<EvalNode> processMapValue(ExpAstNode expNode, Context context, MapEvalNode
            objToIterate) {
        CompletableFuture<EvalNode> keyVarName = evaluateNode(expNode.getNode(0), context);
        CompletableFuture<EvalNode> valueVarName = evaluateNode(expNode.getNode(1), context);

        CompletableFuture<List<EvalNode>> leftRightDone = sequence(keyVarName, valueVarName);
        return leftRightDone.thenCompose(keyValueNames -> {
            CompletableFuture<List<EvalNode>> res = iterate(expNode, context, objToIterate, keyValueNames.get(0), keyValueNames.get(1));
            return res.thenApply(nodes ->
                    new StringEvalNode(nodes
                            .stream()
                            .map(node -> node.getValue() + "")
                            .collect(Collectors.joining())
                    )
            );
        });
    }

    private CompletableFuture<List<EvalNode>> iterate(ExpAstNode expNode, Context context, MapEvalNode
            objToIterate,
                                                      EvalNode keyVarName, EvalNode valueVarName) throws HistoneException {
        Context iterableContext;
        StringBuilder sb = new StringBuilder();
        int i = 0;

        List<CompletableFuture<EvalNode>> futures = new ArrayList<>(objToIterate.getValue().size());
        for (Map.Entry<String, Object> entry : objToIterate.getValue().entrySet()) {
            iterableContext = getIterableContext(context, objToIterate, keyVarName, valueVarName, i, entry);
            futures.add(evaluateNode(expNode.getNode(3), iterableContext));

            String res = processInternal(expNode.getNode(3), iterableContext);
            sb.append(res);
            i++;
        }
        return CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[futures.size()]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
                );
    }

    private Context getIterableContext(Context context, MapEvalNode objToIterate, EvalNode keyVarName, EvalNode valueVarName, int i, Map.Entry<String, Object> entry) {
        Context iterableContext = context.createNew();
        if (valueVarName != NullEvalNode.INSTANCE) {
            iterableContext.put(valueVarName.getValue() + "", EvalUtils.getValue(entry.getValue()));
        }
        if (keyVarName != NullEvalNode.INSTANCE) {
            iterableContext.put(keyVarName.getValue() + "", EvalUtils.getValue(entry.getKey()));
        }
        iterableContext.put("self", EvalUtils.getValue(constructSelfValue(
                entry.getKey(), entry.getValue(), i, objToIterate.getValue().entrySet().size() - 1
        )));
        return iterableContext;
    }


    private Map<String, Object> constructSelfValue(String key, Object value, int currentIndex, int lastIndex) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("index", currentIndex);
        res.put("last", lastIndex);
        res.put("key", key);
        res.put("value", value);
        return res;
    }

    private CompletableFuture<EvalNode> processAddNode(ExpAstNode node, Context context) throws HistoneException {
        CompletableFuture<List<EvalNode>> leftRight = evalNodes(node, context);
        return leftRight.thenApply(lr -> {
            EvalNode left = lr.get(0);
            EvalNode right = lr.get(1);
            if (!(left instanceof StringEvalNode || right instanceof StringEvalNode)) {
                if (isNumberNode(left) && isNumberNode(right)) {
                    Float res = getValue(left) + getValue(right);
                    if (res % 1 == 0 && res <= Integer.MAX_VALUE) {
                        return new LongEvalNode(res.longValue());
                    } else {
                        return new FloatEvalNode(res);
                    }
                }

                if (left instanceof MapEvalNode && right instanceof MapEvalNode) {
                    throw new NotImplementedException();
                }
            }

            return new StringEvalNode(left.asString() + right.asString());
        });
    }

    private CompletableFuture<EvalNode> processArithmetical(ExpAstNode node, Context context) throws HistoneException {
        CompletableFuture<EvalNode> leftFuture = evaluateNode(node.getNode(0), context);
        CompletableFuture<EvalNode> rightFuture = evaluateNode(node.getNode(1), context);

        CompletableFuture<List<EvalNode>> leftRightDone = sequence(leftFuture, rightFuture);

        return leftRightDone.thenApply(futures -> {
            EvalNode left = leftFuture.getNow(null);
            EvalNode right = rightFuture.getNow(null);

            if ((isNumberNode(left) || left instanceof StringEvalNode) &&
                    (isNumberNode(right) || right instanceof StringEvalNode)) {
                Float leftValue = getValue(left);
                Float rightValue = getValue(right);
                if (leftValue == null || rightValue == null) {
                    return EmptyEvalNode.INSTANCE;
                }

                Float res;
                AstType type = node.getType();
                if (type == AstType.AST_SUB) {
                    res = leftValue - rightValue;
                } else if (type == AstType.AST_MUL) {
                    res = leftValue * rightValue;
                } else if (type == AstType.AST_DIV) {
                    res = leftValue / rightValue;
                } else {
                    res = leftValue % rightValue;
                }
                if (res % 1 == 0 && res <= Integer.MAX_VALUE) {
                    return new LongEvalNode(res.longValue());
                } else {
                    return new FloatEvalNode(res);
                }
            }
            return EmptyEvalNode.INSTANCE;
        });
    }

    private Float getValue(EvalNode node) {
        if (node instanceof StringEvalNode) {
            return ParserUtils.isFloat(((StringEvalNode) node).getValue());
        } else {
            return Float.valueOf(node.getValue() + "");
        }
    }

    private CompletableFuture<EvalNode> processRelation(ExpAstNode node, Context context) throws HistoneException {
        CompletableFuture<EvalNode> leftFuture = evaluateNode(node.getNode(0), context);
        CompletableFuture<EvalNode> rightFuture = evaluateNode(node.getNode(1), context);

        CompletableFuture<List<EvalNode>> leftRightDone = sequence(leftFuture, rightFuture);

        return leftRightDone.thenApply(f -> {
            EvalNode left = leftFuture.getNow(null);
            EvalNode right = rightFuture.getNow(null);

            final Integer compareResult;
            if (left instanceof StringEvalNode && isNumberNode(right)) {
                final Number rightValue = getNumberValue(right);
                final StringEvalNode stringLeft = (StringEvalNode) left;
                if (isNumeric(stringLeft)) {
                    final Number leftValue = getNumberValue(stringLeft);
                    compareResult = NUMBER_COMPARATOR.compare(leftValue, rightValue);
                } else {
                    throw new NotImplementedException(); // TODO call RTTI toString right
                }
            } else if (isNumberNode(left) && right instanceof StringEvalNode) {
                final StringEvalNode stringRight = (StringEvalNode) right;
                if (isNumeric(stringRight)) {
                    final Number rightValue = getNumberValue(right);
                    final Number leftValue = getNumberValue(left);
                    compareResult = NUMBER_COMPARATOR.compare(leftValue, rightValue);
                } else {
                    throw new NotImplementedException(); // TODO call RTTI toString left
                }
            } else if (!isNumberNode(left) || !isNumberNode(right)) {
                if (left instanceof StringEvalNode && right instanceof StringEvalNode) {
                    final StringEvalNode stringRight = (StringEvalNode) right;
                    final StringEvalNode stringLeft = (StringEvalNode) left;
                    final long leftLength = stringLeft.getValue().length();
                    final long rightLength = stringRight.getValue().length();
                    compareResult = Long.valueOf(leftLength).compareTo(rightLength);
                } else {
                    throw new NotImplementedException(); // TODO call RTTI toBoolean for both nodes
                }
            } else {
                final Number rightValue = getNumberValue(right);
                final Number leftValue = getNumberValue(left);
                compareResult = NUMBER_COMPARATOR.compare(leftValue, rightValue);
            }

            return processRelationHelper(node.getType(), compareResult);
        });
    }

    private EvalNode processRelationHelper(AstType astType, int compareResult) {
        switch (astType) {
            case AST_LT:
                return new BooleanEvalNode(compareResult < 0);
            case AST_GT:
                return new BooleanEvalNode(compareResult > 0);
            case AST_LE:
                return new BooleanEvalNode(compareResult <= 0);
            case AST_GE:
                return new BooleanEvalNode(compareResult >= 0);
        }
        throw new RuntimeException("Unknown type for this case");
    }

    private CompletableFuture<EvalNode> processBorNode(ExpAstNode node, Context context) throws HistoneException {
        CompletableFuture<EvalNode> leftFuture = evaluateNode(node.getNode(0), context);
        CompletableFuture<EvalNode> rightFuture = evaluateNode(node.getNode(1), context);

        CompletableFuture<List<EvalNode>> leftRightDone = sequence(leftFuture, rightFuture);

        return leftRightDone.thenApply(f -> {
            EvalNode left = leftFuture.getNow(null);
            EvalNode right = rightFuture.getNow(null);

            return new BooleanEvalNode(nodeAsBoolean(left) | nodeAsBoolean(right));
        });
    }

    private CompletableFuture<EvalNode> processBxorNode(ExpAstNode node, Context context) throws HistoneException {
        CompletableFuture<EvalNode> leftFuture = evaluateNode(node.getNode(0), context);
        CompletableFuture<EvalNode> rightFuture = evaluateNode(node.getNode(1), context);

        CompletableFuture<List<EvalNode>> leftRightDone = sequence(leftFuture, rightFuture);

        return leftRightDone.thenApply(f -> {
            EvalNode left = leftFuture.getNow(null);
            EvalNode right = rightFuture.getNow(null);

            return new BooleanEvalNode(nodeAsBoolean(left) ^ nodeAsBoolean(right));
        });
    }

    private CompletableFuture<EvalNode> processBandNode(ExpAstNode node, Context context) throws HistoneException {
        CompletableFuture<EvalNode> leftFuture = evaluateNode(node.getNode(0), context);
        CompletableFuture<EvalNode> rightFuture = evaluateNode(node.getNode(1), context);

        CompletableFuture<List<EvalNode>> leftRightDone = sequence(leftFuture, rightFuture);

        return leftRightDone.thenApply(f -> {
            EvalNode left = leftFuture.getNow(null);
            EvalNode right = rightFuture.getNow(null);

            return new BooleanEvalNode(nodeAsBoolean(left) & nodeAsBoolean(right));
        });
    }

    private CompletableFuture<EvalNode> processVarNode(ExpAstNode node, Context context) throws HistoneException {
        CompletableFuture<EvalNode> valueNameFuture = evaluateNode(node.getNode(1), context);
        CompletableFuture<EvalNode> valueNodeFuture = evaluateNode(node.getNode(0), context);

        CompletableFuture<List<EvalNode>> leftRightDone = sequence(valueNameFuture);

        return leftRightDone.thenApply(f -> {
            EvalNode valueName = valueNameFuture.getNow(null);
            context.put(valueName.getValue() + "", valueNodeFuture);
            return EmptyEvalNode.INSTANCE;
        });
    }

    private CompletableFuture<EvalNode> processArrayNode(ExpAstNode node, Context context) throws HistoneException {
        if (CollectionUtils.isEmpty(node.getNodes())) {
            return CompletableFuture.completedFuture(new MapEvalNode(Collections.emptyMap()));
        }
        if (node.getNode(0).getType() == AstType.AST_VAR) {
            return evalNodes(node, context).thenApply(evalNodes -> EmptyEvalNode.INSTANCE);
        } else {
            if (node.size() > 0) {
                CompletableFuture<List<EvalNode>> futures = evalNodes(node, context);
                return futures.thenApply(nodes -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    for (int i = 0; i < nodes.size() / 2; i++) {
                        EvalNode key = nodes.get(i * 2);
                        EvalNode value = nodes.get(i * 2 + 1);
                        map.put(key.getValue() + "", value.getValue());
                    }
                    return new MapEvalNode(map);
                });
            } else {
                return CompletableFuture.completedFuture(new MapEvalNode(new LinkedHashMap<>()));
            }
        }
    }

    private CompletableFuture<EvalNode> processUnaryMinus(ExpAstNode node, Context context) throws HistoneException {
        CompletableFuture<EvalNode> res = evaluateNode(node.getNode(0), context);
        return res.thenApply(n -> {
            if (n instanceof LongEvalNode) {
                Long value = ((LongEvalNode) n).getValue();
                return new LongEvalNode(-value);
            }
            throw new NotImplementedException();
        });
    }

    private CompletableFuture<EvalNode> getValueNode(AstNode node) {
        ValueNode valueNode = (ValueNode) node;
        if (valueNode.getValue() == null) {
            return CompletableFuture.completedFuture(NullEvalNode.INSTANCE);
        }

        Object val = valueNode.getValue();
        if (val instanceof Boolean) {
            return CompletableFuture.completedFuture(new BooleanEvalNode((Boolean) val));
        } else if (val instanceof Long) {
            return CompletableFuture.completedFuture(new LongEvalNode((Long) val));
        } else if (val instanceof Float) {
            return CompletableFuture.completedFuture(new FloatEvalNode((Float) val));
        }
        return CompletableFuture.completedFuture(new StringEvalNode(val + ""));
    }

    private CompletableFuture<EvalNode> processReferenceNode(ExpAstNode node, Context context) {
        StringAstNode valueNode = node.getNode(0);
        CompletableFuture<EvalNode> value = getValueFromParentContext(context, valueNode.getValue());
        return value.thenCompose(v -> {
            if (v != null) {
                return CompletableFuture.completedFuture(v);
            } else {
                return CompletableFuture.completedFuture(EmptyEvalNode.INSTANCE);
            }
        });
    }

    private CompletableFuture<EvalNode> getValueFromParentContext(Context context, String valueName) {
        while (context != null) {
            if (context.contains(valueName)) {
                return (CompletableFuture<EvalNode>) context.getVars().get(valueName);
            }
            context = context.getParent();
        }
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<EvalNode> processOrNode(ExpAstNode node, Context context) throws HistoneException {
        CompletableFuture<EvalNode> leftFuture = evaluateNode(node.getNode(0), context);
        CompletableFuture<EvalNode> rightFuture = evaluateNode(node.getNode(1), context);

        CompletableFuture<List<EvalNode>> leftRightDone = sequence(leftFuture, rightFuture);

        return leftRightDone.thenApply(f -> {
            EvalNode left = leftFuture.getNow(null);
            EvalNode right = rightFuture.getNow(null);

            return new BooleanEvalNode(nodeAsBoolean(left) || nodeAsBoolean(right));
        });
    }

    private CompletableFuture<EvalNode> processAndNode(ExpAstNode node, Context context) throws HistoneException {
        CompletableFuture<EvalNode> leftFuture = evaluateNode(node.getNode(0), context);
        CompletableFuture<EvalNode> rightFuture = evaluateNode(node.getNode(1), context);

        CompletableFuture<List<EvalNode>> leftRightDone = sequence(leftFuture, rightFuture);

        return leftRightDone.thenApply(f -> {
            EvalNode left = leftFuture.getNow(null);
            EvalNode right = rightFuture.getNow(null);

            return new BooleanEvalNode(nodeAsBoolean(left) && nodeAsBoolean(right));
        });
    }

    private CompletableFuture<EvalNode> processNodeList(ExpAstNode node, Context context) throws HistoneException {
        //todo rework this method, add rtti and other cool features
        if (node.getNodes().size() == 1) {
            AstNode node1 = node.getNode(0);
            return evaluateNode(node1, context);
        } else {
            CompletableFuture<List<EvalNode>> f = evalNodes(node, context);
            return f.thenApply(nodes -> new StringEvalNode(
                    nodes.stream()
                            .map(n -> {
                                Function function = context.getFunction(n, "toString");
                                return function.execute(Collections.singletonList(n));
                            })
                            .map(CompletableFuture::join)
                            .map(n -> n.getValue() + "")
                            .collect(Collectors.joining())
            ));
        }
    }

    private CompletableFuture<List<EvalNode>> evalNodes(ExpAstNode node, Context context) throws HistoneException {
        List<CompletableFuture<EvalNode>> futures = new ArrayList<>(node.size());
        for (AstNode currNode : node.getNodes()) {
            futures.add(evaluateNode(currNode, context));
        }
        return sequence(futures);
    }


    private CompletableFuture<EvalNode> processEqNode(ExpAstNode node, Context context, boolean isEquals) {
        CompletableFuture<EvalNode> leftFuture = evaluateNode(node.getNode(0), context);
        CompletableFuture<EvalNode> rightFuture = evaluateNode(node.getNode(1), context);

        CompletableFuture<List<EvalNode>> leftRightDone = sequence(leftFuture, rightFuture);

        return leftRightDone.thenApply(f -> {
            EvalNode left = leftFuture.getNow(null);
            EvalNode right = rightFuture.getNow(null);

            return new BooleanEvalNode(equalityNode(left, right) && isEquals);
        });
    }

    private CompletableFuture<EvalNode> processIfNode(ExpAstNode node, Context context) throws HistoneException {
        CompletableFuture<EvalNode> conditionFuture = evaluateNode(node.getNode(1), context);

        return conditionFuture
                .thenCompose(condNode -> {
                    Context current = context.createNew();
                    CompletableFuture<EvalNode> result;
                    if (nodeAsBoolean(condNode)) {
                        result = evaluateNode(node.getNode(0), current);
                    } else {
                        result = evaluateNode(node.getNode(2), current);
                    }
                    current.setParent(null);
                    return result;
                });
    }

    private CompletableFuture<EvalNode> processRegExp(ExpAstNode node, Context context) {
        return CompletableFuture.supplyAsync(() -> {
            final LongAstNode flagsNumNode = node.getNode(1);
            final long flagsNum = flagsNumNode.getValue();

            int flags = 0;
            if ((flagsNum & AstRegexType.RE_IGNORECASE.getId()) != 0) {
                flags |= Pattern.CASE_INSENSITIVE;
            }
            if ((flagsNum & AstRegexType.RE_MULTILINE.getId()) != 0) {
                flags |= Pattern.MULTILINE;
            }

            final boolean isGlobal = (flagsNum & AstRegexType.RE_GLOBAL.getId()) != 0;
            final StringAstNode expNode = node.getNode(0);
            final String exp = expNode.getValue();
            final Pattern pattern = Pattern.compile(exp, flags);
            return new RegexEvalNode(new HistoneRegex(isGlobal, pattern));
        });
    }

}

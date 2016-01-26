package ru.histone.v2.evaluator;

import org.apache.commons.collections.ArrayStack;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.NotImplementedException;
import ru.histone.HistoneException;
import ru.histone.v2.evaluator.data.HistoneRegex;
import ru.histone.v2.evaluator.global.NumberComparator;
import ru.histone.v2.evaluator.node.*;
import ru.histone.v2.parser.node.*;
import ru.histone.v2.utils.ParserUtils;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;

import static ru.histone.v2.evaluator.EvalUtils.*;

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
        StringBuilder sb = new StringBuilder();
        for (AstNode currentNode : node.getNodes()) {
            EvalNode evalNode = evaluateNode(currentNode, context);
            if (evalNode instanceof NullEvalNode) {
                sb.append("null");
            } else if (evalNode.getValue() != null) {
                sb.append(evalNode.asString());
            }
        }
        return sb.toString();
    }

    private EvalNode evaluateNode(AstNode node, Context context) throws HistoneException {
        if (node == null) {
            return EmptyEvalNode.INSTANCE;
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
                return processEqNode(expNode, context);
            case AST_NEQ:
                return processEqNode(expNode, context).neg();
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

    private EvalNode processMacroNode(ExpAstNode node, Context context) {
        final long param;
        if (node.size() >= 1) {
            final LongAstNode paramNode = node.getNode(1);
            param = paramNode.getValue();
        } else {
            param = 0;
        }
        throw new NotImplementedException();
    }

    private EvalNode processMethod(ExpAstNode expNode, Context context) throws HistoneException {
        EvalNode valueNode = evaluateNode(expNode.getNode(0), context);
        EvalNode methodNode = evaluateNode(expNode.getNode(1), context);

        final int size = expNode.getNodes().size();
        final List<EvalNode> args = new ArrayList<>();
        for (AstNode node : expNode.getNodes().subList(2, size)) {
            args.add(evaluateNode(node, context));
        }
        final List<EvalNode> allParams = new ArrayList<>();
        allParams.add(valueNode);
        allParams.addAll(args);

        Function f = context.getFunction(valueNode, methodNode.asString());
        return f.execute(allParams);
    }

    private EvalNode processMethod(ExpAstNode expNode, Context context, List<EvalNode> args) throws HistoneException {
        EvalNode valueNode = evaluateNode(expNode.getNode(0), context);
        EvalNode methodNode = evaluateNode(expNode.getNode(1), context);

        final List<EvalNode> allParams = new ArrayList<>();
        allParams.add(valueNode);
        allParams.addAll(args);

        Function f = context.getFunction(valueNode, methodNode.asString());
        return f.execute(allParams);
    }

    private EvalNode processTernary(ExpAstNode expNode, Context context) throws HistoneException {
        EvalNode condition = evaluateNode(expNode.getNode(0), context);
        if (nodeAsBoolean(condition)) {
            return evaluateNode(expNode.getNode(1), context);
        } else if (expNode.getNode(2) != null) {
            return evaluateNode(expNode.getNode(2), context);
        }
        return EmptyEvalNode.INSTANCE;
    }

    private EvalNode processPropertyNode(ExpAstNode expNode, Context context) throws HistoneException {
        EvalNode value = evaluateNode(expNode.getNode(0), context);
        EvalNode propertyName = evaluateNode(expNode.getNode(1), context);

        Object obj = ((MapEvalNode) value).getProperty((String) propertyName.getValue());
        return createEvalNode(obj);
    }

    private EvalNode processCall(ExpAstNode expNode, Context context) throws HistoneException {
        final ExpAstNode node = expNode.getNode(0);
        final EvalNode functionNameNode = evaluateNode((node).getNode(0), context);
        final List<AstNode> paramsAstNodes = expNode.getNodes().subList(1, expNode.getNodes().size());
        final List<EvalNode> paramsNodes = toEvalNodes(paramsAstNodes, context);
        if (functionNameNode instanceof StringEvalNode) {
            Function function = context.getFunction((String) functionNameNode.getValue());
            return function.execute(paramsNodes);
        } else {
            return processMethod(node, context, paramsNodes);
        }
//        if (expNode.getType() == AstType.AST_METHOD) {
//            Function function = context.getFunction((String) functionNameNode.getValue());
//            return function.execute(paramsNodes);
//        } else {
//            throw new NotImplementedException("Need RTTI call"); // TODO
//        }
    }

    private List<EvalNode> toEvalNodes(List<AstNode> astNodes, Context context) throws HistoneException {
        List<EvalNode> res = new ArrayList<>();
        for (AstNode node : astNodes) {
            res.add(evaluateNode(node, context));
        }
        return res;
    }

    private EvalNode processForNode(ExpAstNode expNode, Context context) throws HistoneException {
        EvalNode objToIterate = evaluateNode(expNode.getNode(2), context);
        if (!(objToIterate instanceof MapEvalNode)) {
            if (expNode.size() == 4) {
                return EmptyEvalNode.INSTANCE;
            }
            int i = 0;
            ExpAstNode expressionNode = expNode.getNode(i + 4);
            ExpAstNode bodyNode = expNode.getNode(i + 5);
            while (bodyNode != null) {
                EvalNode conditionNode = evaluateNode(expressionNode, context);
                if (nodeAsBoolean(conditionNode)) {
                    String res = processInternal(bodyNode, context);
                    return new StringEvalNode(res);
                }
                i++;
                expressionNode = expNode.getNode(i + 4);
                bodyNode = expNode.getNode(i + 5);
            }
            if (expressionNode != null) {
                String res = processInternal(expressionNode, context);
                return new StringEvalNode(res);
            }

            return EmptyEvalNode.INSTANCE;
        }

        EvalNode keyVarName = evaluateNode(expNode.getNode(0), context);
        EvalNode valueVarName = evaluateNode(expNode.getNode(1), context);

        StringBuilder sb = iterate(expNode, context, (MapEvalNode) objToIterate, keyVarName, valueVarName);

        return new StringEvalNode(sb.toString());
    }

    private StringBuilder iterate(ExpAstNode expNode, Context context, MapEvalNode objToIterate, EvalNode keyVarName,
                                  EvalNode valueVarName) throws HistoneException {
        Context iterableContext;
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Map.Entry<String, Object> entry : objToIterate.getValue().entrySet()) {
            iterableContext = context.createNew();
            if (valueVarName != NullEvalNode.INSTANCE) {
                iterableContext.put((String) valueVarName.getValue(), entry.getValue());
            }
            if (keyVarName != NullEvalNode.INSTANCE) {
                iterableContext.getVars().putIfAbsent((String) keyVarName.getValue(), entry.getKey());
            }
            iterableContext.put("self", constructSelfValue(
                    entry.getKey(), entry.getValue(), i, objToIterate.getValue().entrySet().size() - 1));
            String res = processInternal(expNode.getNode(3), iterableContext);
            sb.append(res);
            i++;
        }
        return sb;
    }


    private Map<String, Object> constructSelfValue(String key, Object value, int currentIndex, int lastIndex) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("index", currentIndex);
        res.put("last", lastIndex);
        res.put("key", key);
        res.put("value", value);
        return res;
    }

    private EvalNode processAddNode(ExpAstNode node, Context context) throws HistoneException {
        EvalNode left = evaluateNode(node.getNode(0), context);
        EvalNode right = evaluateNode(node.getNode(1), context);

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
                throw new org.apache.commons.lang.NotImplementedException();
            }
        }

        return new StringEvalNode(left.asString() + right.asString());
    }

    private EvalNode processArithmetical(ExpAstNode node, Context context) throws HistoneException {
        EvalNode left = evaluateNode(node.getNode(0), context);
        EvalNode right = evaluateNode(node.getNode(1), context);

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
    }

    private Float getValue(EvalNode node) {
        if (node instanceof StringEvalNode) {
            return ParserUtils.isFloat(((StringEvalNode) node).getValue());
        } else {
            return Float.valueOf(node.getValue() + "");
        }
    }

    private EvalNode processRelation(ExpAstNode node, Context context) throws HistoneException {
        final EvalNode left = evaluateNode(node.getNode(0), context);
        final EvalNode right = evaluateNode(node.getNode(1), context);

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

    private EvalNode processBorNode(ExpAstNode node, Context context) throws HistoneException {
        EvalNode conditionNode1 = evaluateNode(node.getNode(0), context);
        EvalNode conditionNode2 = evaluateNode(node.getNode(1), context);
        return new BooleanEvalNode(nodeAsBoolean(conditionNode1) | nodeAsBoolean(conditionNode2));
    }

    private EvalNode processBxorNode(ExpAstNode node, Context context) throws HistoneException {
        EvalNode conditionNode1 = evaluateNode(node.getNode(0), context);
        EvalNode conditionNode2 = evaluateNode(node.getNode(1), context);
        return new BooleanEvalNode(nodeAsBoolean(conditionNode1) ^ nodeAsBoolean(conditionNode2));
    }

    private EvalNode processBandNode(ExpAstNode node, Context context) throws HistoneException {
        EvalNode conditionNode1 = evaluateNode(node.getNode(0), context);
        EvalNode conditionNode2 = evaluateNode(node.getNode(1), context);
        return new BooleanEvalNode(nodeAsBoolean(conditionNode1) & nodeAsBoolean(conditionNode2));
    }

    private EvalNode processVarNode(ExpAstNode node, Context context) throws HistoneException {
        EvalNode valueNode = evaluateNode(node.getNode(0), context);
        EvalNode valueName = evaluateNode(node.getNode(1), context);
        if (valueNode.getValue() != null) {
            context.getVars().putIfAbsent(valueName.getValue() + "", valueNode.getValue());
        }
        return EmptyEvalNode.INSTANCE;
    }

    private EvalNode processArrayNode(ExpAstNode node, Context context) throws HistoneException {
        if (CollectionUtils.isEmpty(node.getNodes())) {
            return new MapEvalNode(Collections.emptyMap());
        }
        if (node.getNode(0).getType() == AstType.AST_VAR) {
            for (AstNode astNode : node.getNodes()) {
                evaluateNode(astNode, context);
            }
            return EmptyEvalNode.INSTANCE;
        } else {
            Map<String, Object> map = new LinkedHashMap<>();
            for (int i = 0; i < node.getNodes().size() / 2; i++) {
                EvalNode key = evaluateNode(node.getNodes().get(i * 2), context);
                EvalNode value = evaluateNode(node.getNodes().get(i * 2 + 1), context);
                map.put(key.getValue() + "", value.getValue());
            }
            return new MapEvalNode(map);
        }
    }

    private EvalNode processUnaryMinus(ExpAstNode node, Context context) throws HistoneException {
        EvalNode res = evaluateNode(node.getNode(0), context);
        if (res instanceof LongEvalNode) {
            Long value = ((LongEvalNode) res).getValue();
            return new LongEvalNode(-value);
        } else {
            throw new NotImplementedException();
        }
    }

    private EvalNode<? extends Serializable> getValueNode(AstNode node) {
        ValueNode valueNode = (ValueNode) node;
        if (valueNode.getValue() == null) {
            return NullEvalNode.INSTANCE;
        }

        Object val = valueNode.getValue();
        if (val == null) {
            return NullEvalNode.INSTANCE;
        } else if (val instanceof Boolean) {
            return new BooleanEvalNode((Boolean) val);
        } else if (val instanceof Long) {
            return new LongEvalNode((Long) val);
        } else if (val instanceof Float) {
            return new FloatEvalNode((Float) val);
        }
        return new StringEvalNode(val + "");
    }

    private EvalNode processReferenceNode(ExpAstNode node, Context context) {
        StringAstNode valueNode = node.getNode(0);
        Object value = getValueFromParentContext(context, valueNode.getValue());
        if (value != null) {
            return createEvalNode(value);
        } else {
            return EmptyEvalNode.INSTANCE;
        }
    }

    private Object getValueFromParentContext(Context context, String valueName) {
        while (context != null) {
            if (context.getVars().containsKey(valueName)) {
                return context.getVars().get(valueName);
            }
            context = context.getParent();
        }
        return null;
    }

    private EvalNode processOrNode(ExpAstNode node, Context context) throws HistoneException {
        EvalNode conditionNode1 = evaluateNode(node.getNode(0), context);
        EvalNode conditionNode2 = evaluateNode(node.getNode(1), context);
        return new BooleanEvalNode(nodeAsBoolean(conditionNode1) || nodeAsBoolean(conditionNode2));
    }

    private EvalNode processAndNode(ExpAstNode node, Context context) throws HistoneException {
        EvalNode conditionNode1 = evaluateNode(node.getNode(0), context);
        EvalNode conditionNode2 = evaluateNode(node.getNode(1), context);
        return new BooleanEvalNode(nodeAsBoolean(conditionNode1) && nodeAsBoolean(conditionNode2));
    }

    private EvalNode processNodeList(ExpAstNode node, Context context) throws HistoneException {
        //todo rework this method, add rtti and other cool features
        if (node.getNodes().size() == 1) {
            AstNode node1 = node.getNode(0);
            return evaluateNode(node1, context);
        } else {
            StringBuilder sb = new StringBuilder();
            for (AstNode currNode : node.getNodes()) {
                EvalNode processed = evaluateNode(currNode, context);
                sb.append(processed.getValue());
            }
            return new StringEvalNode(sb.toString());
        }
    }

    private BooleanEvalNode processEqNode(ExpAstNode node, Context context) throws HistoneException {
        EvalNode left = evaluateNode(node.getNode(0), context);
        EvalNode right = evaluateNode(node.getNode(1), context);
        return new BooleanEvalNode(equalityNode(left, right));
    }

    private EvalNode processIfNode(ExpAstNode node, Context context) throws HistoneException {
        Context current = context.createNew();
        EvalNode conditionNode = evaluateNode(node.getNode(1), context);
        EvalNode result;
        if (nodeAsBoolean(conditionNode)) {
            result = evaluateNode(node.getNode(0), current);
        } else {
            result = evaluateNode(node.getNode(2), current);
        }
        current.setParent(null);
        return result;
    }

    private EvalNode processRegExp(ExpAstNode node, Context context) {
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
    }

}

package ru.histone.v2.evaluator;

import org.apache.commons.collections.CollectionUtils;
import ru.histone.HistoneException;
import ru.histone.v2.evaluator.node.*;
import ru.histone.v2.parser.node.*;
import ru.histone.v2.utils.ParserUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by alexey.nevinsky on 12.01.2016.
 */
public class Evaluator {

    public String process(String baseUri, ExpAstNode node, Context context) throws HistoneException {
        context.setBaseUri(baseUri);
        return processInternal(node, context);
    }

    private String processInternal(ExpAstNode node, Context context) throws HistoneException {
        StringBuilder sb = new StringBuilder();
        Context.NodeContext root = createRootContext();
        for (AstNode currentNode : node.getNodes()) {
            EvalNode evalNode = evaluateNode(currentNode, context, root);
            if (evalNode instanceof NullEvalNode) {
                sb.append("null");
            } else if (evalNode.getValue() != null) {
                sb.append(evalNode.asString());
            }
        }
        return sb.toString();
    }

    private Context.NodeContext createRootContext() {
        Context.NodeContext root = new Context.NodeContext();
        return root;
    }

    private EvalNode evaluateNode(AstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
        if (node == null) {
            return EmptyEvalNode.INSTANCE;
        }

        if (node.hasValue()) {
            return getValueNode(node);
        }

        ExpAstNode expNode = (ExpAstNode) node;
        switch (node.getType()) {
            case AST_NOP:
                break;
            case AST_ARRAY:
                return processArrayNode(expNode, context, currentContext);
            case AST_REGEXP:
                break;
            case AST_THIS:
                break;
            case AST_GLOBAL:
                break;
            case AST_NOT:
                break;
            case AST_AND:
                return processAndNode(expNode, context, currentContext);
            case AST_OR:
                return processOrNode(expNode, context, currentContext);
            case AST_TERNARY:
                break;
            case AST_ADD:
                return processAddNode(expNode, context, currentContext);
            case AST_SUB:
            case AST_MUL:
            case AST_DIV:
            case AST_MOD:
                return processArithmetical(expNode, context, currentContext);
            case AST_USUB:
                return processUnaryMinus(expNode, context, currentContext);
            case AST_LT:
                break;
            case AST_GT:
                return processGreaterThan(expNode, context, currentContext);
            case AST_LE:
                return processLessOrEquals(expNode, context, currentContext);
            case AST_GE:
                break;
            case AST_EQ:
                return processEqNode(expNode, context, currentContext);
            case AST_NEQ:
                return processEqNode(expNode, context, currentContext).neg();
            case AST_REF:
                return processReferenceNode(expNode, context, currentContext);
            case AST_METHOD:
                break;
            case AST_PROP:
                break;
            case AST_CALL:
                break;
            case AST_VAR:
                return processVarNode(expNode, context, currentContext);
            case AST_IF:
                return processIfNode(expNode, context, currentContext);
            case AST_FOR:
                break;
            case AST_MACRO:
                break;
            case AST_RETURN:
                break;
            case AST_NODES:
                return processNodeList(expNode, context, currentContext.createNew());
            case AST_NODELIST:
                return processNodeList(expNode, context, currentContext);
            case AST_BOR:
                return processBorNode(expNode, context, currentContext);
            case AST_BXOR:
                return processBxorNode(expNode, context, currentContext);
            case AST_BAND:
                return processBandNode(expNode, context, currentContext);
            case AST_SUPRESS:
                break;
            case AST_LISTEN:
                break;
            case AST_TRIGGER:
                break;
        }
        throw new HistoneException("WTF!?!?!? " + node.getType());

    }

    private EvalNode processAddNode(ExpAstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
        EvalNode left = evaluateNode(node.getNode(0), context, currentContext);
        EvalNode right = evaluateNode(node.getNode(1), context, currentContext);

        if (!(left instanceof StringEvalNode || right instanceof StringEvalNode)) {
            if (EvalUtils.isNumberNode(left) && EvalUtils.isNumberNode(right)) {
                Float res = getValue(left) + getValue(right);
                if (res % 1 == 0 && res <= Integer.MAX_VALUE) {
                    return new LongEvalNode(res.longValue());
                } else {
                    return new FloatEvalNode(res);
                }
            }

            if (left instanceof MapEvalNode && right instanceof MapEvalNode) {

            }
        }

        return new StringEvalNode(left.asString() + right.asString());
    }

    private EvalNode processArithmetical(ExpAstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
        EvalNode left = evaluateNode(node.getNode(0), context, currentContext);
        EvalNode right = evaluateNode(node.getNode(1), context, currentContext);

        if ((EvalUtils.isNumberNode(left) || left instanceof StringEvalNode) &&
                (EvalUtils.isNumberNode(right) || right instanceof StringEvalNode)) {
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

    private EvalNode processGreaterThan(ExpAstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
        EvalNode left = evaluateNode(node.getNode(0), context, currentContext);
        EvalNode right = evaluateNode(node.getNode(1), context, currentContext);

        return null;
    }

    private EvalNode processBorNode(ExpAstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
        EvalNode conditionNode1 = evaluateNode(node.getNode(0), context, currentContext);
        EvalNode conditionNode2 = evaluateNode(node.getNode(1), context, currentContext);
        return new BooleanEvalNode(EvalUtils.nodeAsBoolean(conditionNode1) | EvalUtils.nodeAsBoolean(conditionNode2));
    }

    private EvalNode processBxorNode(ExpAstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
        EvalNode conditionNode1 = evaluateNode(node.getNode(0), context, currentContext);
        EvalNode conditionNode2 = evaluateNode(node.getNode(1), context, currentContext);
        return new BooleanEvalNode(EvalUtils.nodeAsBoolean(conditionNode1) ^ EvalUtils.nodeAsBoolean(conditionNode2));
    }

    private EvalNode processBandNode(ExpAstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
        EvalNode conditionNode1 = evaluateNode(node.getNode(0), context, currentContext);
        EvalNode conditionNode2 = evaluateNode(node.getNode(1), context, currentContext);
        return new BooleanEvalNode(EvalUtils.nodeAsBoolean(conditionNode1) & EvalUtils.nodeAsBoolean(conditionNode2));
    }

    private EvalNode processVarNode(ExpAstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
        EvalNode valueNode = evaluateNode(node.getNode(1), context, currentContext);
        EvalNode valueName = evaluateNode(node.getNode(0), context, currentContext);
        if (valueNode.getValue() != null) {
            currentContext.getVars().putIfAbsent(valueName.getValue() + "", valueNode.getValue());
        }
        return EmptyEvalNode.INSTANCE;
    }

    private EvalNode processArrayNode(ExpAstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
        if (CollectionUtils.isEmpty(node.getNodes())) {
            return new ObjectEvalNode(Collections.emptyMap());
        }
        if (node.getNode(0).getType() == AstType.AST_VAR) {
            for (AstNode astNode : node.getNodes()) {
                evaluateNode(astNode, context, currentContext);
            }
            return EmptyEvalNode.INSTANCE;
        } else {
            Map<String, Object> map = new LinkedHashMap<>();
            for (int i = 0; i < node.getNodes().size() / 2; i++) {
                EvalNode key = evaluateNode(node.getNodes().get(i * 2), context, currentContext);
                EvalNode value = evaluateNode(node.getNodes().get(i * 2 + 1), context, currentContext);
                map.put(key.getValue() + "", value.getValue());
            }
            return new ObjectEvalNode(map);
        }
    }

    private EvalNode processUnaryMinus(ExpAstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
        EvalNode res = evaluateNode(node.getNode(0), context, currentContext);
        if (res instanceof LongEvalNode) {
            Long value = ((LongEvalNode) res).getValue();
            return new LongEvalNode(-value);
        } else {
            throw new NotImplementedException();
        }
    }

    private EvalNode processLessOrEquals(ExpAstNode node, Context context, Context.NodeContext currentContext) {
        throw new NotImplementedException();
    }

    private EvalNode<? extends Serializable> getValueNode(AstNode node) {
        ValueNode valueNode = (ValueNode) node;
        if (valueNode.getValue() == null) {
            return new NullEvalNode();
        }

        Object val = valueNode.getValue();
        if (val == null) {
            return new NullEvalNode();
        } else if (val instanceof Boolean) {
            return new BooleanEvalNode((Boolean) val);
        } else if (val instanceof Long) {
            return new LongEvalNode((Long) val);
        }
        return new StringEvalNode(val + "");
    }

    private EvalNode processReferenceNode(ExpAstNode node, Context context, Context.NodeContext currentContext) {
        StringAstNode valueNode = node.getNode(0);
        Object value = getValueFromParentContext(currentContext, valueNode.getValue());
        if (value != null) {
            return EvalUtils.createEvalNode(value);
        } else {
            return EmptyEvalNode.INSTANCE;
        }
    }

    private Object getValueFromParentContext(Context.NodeContext context, String valueName) {
        while (context != null) {
            if (context.getVars().containsKey(valueName)) {
                return context.getVars().get(valueName);
            }
            context = context.getParent();
        }
        return null;
    }

    private EvalNode processOrNode(ExpAstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
        EvalNode conditionNode1 = evaluateNode(node.getNode(0), context, currentContext);
        EvalNode conditionNode2 = evaluateNode(node.getNode(1), context, currentContext);
        return new BooleanEvalNode(EvalUtils.nodeAsBoolean(conditionNode1) || EvalUtils.nodeAsBoolean(conditionNode2));
    }

    private EvalNode processAndNode(ExpAstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
        EvalNode conditionNode1 = evaluateNode(node.getNode(0), context, currentContext);
        EvalNode conditionNode2 = evaluateNode(node.getNode(1), context, currentContext);
        return new BooleanEvalNode(EvalUtils.nodeAsBoolean(conditionNode1) && EvalUtils.nodeAsBoolean(conditionNode2));
    }

    private EvalNode processNodeList(ExpAstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
        //todo rework this method, add rtti and other cool features
        if (node.getNodes().size() == 1) {
            AstNode node1 = node.getNode(0);
            return evaluateNode(node1, context, currentContext);
        } else {
            StringBuilder sb = new StringBuilder();
            for (AstNode currNode : node.getNodes()) {
                EvalNode processed = evaluateNode(currNode, context, currentContext);
                sb.append(processed.getValue());
            }
            return new StringEvalNode(sb.toString());
        }
    }

    private BooleanEvalNode processEqNode(ExpAstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
        EvalNode left = evaluateNode(node.getNode(0), context, currentContext);
        EvalNode right = evaluateNode(node.getNode(1), context, currentContext);
        return new BooleanEvalNode(EvalUtils.equalityNode(left, right));
    }

    private EvalNode processIfNode(ExpAstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
        Context.NodeContext current = currentContext.createNew();
        EvalNode conditionNode = evaluateNode(node.getNode(1), context, currentContext);
        EvalNode result;
        if (EvalUtils.nodeAsBoolean(conditionNode)) {
            result = evaluateNode(node.getNode(0), context, current);
        } else {
            result = evaluateNode(node.getNode(2), context, current);
        }
        current.setParent(null);
        return result;
    }

}

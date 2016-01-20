package ru.histone.v2.evaluator;

import org.apache.commons.collections.CollectionUtils;
import ru.histone.HistoneException;
import ru.histone.v2.evaluator.node.*;
import ru.histone.v2.exceptions.UnknownAstTypeException;
import ru.histone.v2.parser.node.AstNode;
import ru.histone.v2.parser.node.AstType;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by alexey.nevinsky on 12.01.2016.
 */
public class Evaluator {

    public String process(String baseUri, AstNode node, Context context) throws HistoneException {
        context.setBaseUri(baseUri);
        return processInternal(node, context);
    }

    private String processInternal(AstNode node, Context context) throws HistoneException {
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
            return new EmptyEvalNode();
        }

        final int nodeType = node.getType();
        if (nodeType == Integer.MIN_VALUE) {
            return getValueNode(node);
        }

        AstType type = AstType.fromId(nodeType);
        if (type == null) {
            throw new UnknownAstTypeException(nodeType);
        }

        switch (type) {
            case AST_NOP:
                break;
            case AST_ARRAY:
                return processArrayNode(node, context, currentContext);
            case AST_REGEXP:
                break;
            case AST_THIS:
                break;
            case AST_GLOBAL:
                break;
            case AST_NOT:
                break;
            case AST_AND:
                return processAndNode(node, context, currentContext);
            case AST_OR:
                return processOrNode(node, context, currentContext);
            case AST_TERNARY:
                break;
            case AST_ADD:
                break;
            case AST_SUB:
                break;
            case AST_MUL:
                break;
            case AST_DIV:
                break;
            case AST_MOD:
                break;
            case AST_USUB:
                return processUnaryMinus(node, context, currentContext);
            case AST_LT:
                break;
            case AST_GT:
                return processGreaterThan(node, context, currentContext);
            case AST_LE:
                return processLessOrEquals(node, context, currentContext);
            case AST_GE:
                break;
            case AST_EQ:
                return processEqNode(node, context, currentContext);
            case AST_NEQ:
                return processEqNode(node, context, currentContext).neg();
            case AST_REF:
                return processReferenceNode(node, context, currentContext);
            case AST_METHOD:
                break;
            case AST_PROP:
                break;
            case AST_CALL:
                break;
            case AST_VAR:
                return processVarNode(node, context, currentContext);
            case AST_IF:
                return processIfNode(node, context, currentContext);
            case AST_FOR:
                break;
            case AST_MACRO:
                break;
            case AST_RETURN:
                break;
            case AST_NODES:
                return processNodeList(node, context, currentContext.createNew());
            case AST_NODELIST:
                return processNodeList(node, context, currentContext);
            case AST_BOR:
                return processBorNode(node, context, currentContext);
            case AST_BXOR:
                return processBxorNode(node, context, currentContext);
            case AST_BAND:
                return processBandNode(node, context, currentContext);
            case AST_SUPRESS:
                break;
            case AST_LISTEN:
                break;
            case AST_TRIGGER:
                break;
        }
        throw new HistoneException("WTF!?!?!? " + type);

    }

    private EvalNode processGreaterThan(AstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
        EvalNode left = evaluateNode(node.getNode(0), context, currentContext);
        EvalNode right = evaluateNode(node.getNode(1), context, currentContext);

        return null;
    }

    private EvalNode processBorNode(AstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
        EvalNode conditionNode1 = evaluateNode(node.getNode(0), context, currentContext);
        EvalNode conditionNode2 = evaluateNode(node.getNode(1), context, currentContext);
        return new BooleanEvalNode(EvalUtils.nodeAsBoolean(conditionNode1) | EvalUtils.nodeAsBoolean(conditionNode2));
    }

    private EvalNode processBxorNode(AstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
        EvalNode conditionNode1 = evaluateNode(node.getNode(0), context, currentContext);
        EvalNode conditionNode2 = evaluateNode(node.getNode(1), context, currentContext);
        return new BooleanEvalNode(EvalUtils.nodeAsBoolean(conditionNode1) ^ EvalUtils.nodeAsBoolean(conditionNode2));
    }

    private EvalNode processBandNode(AstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
        EvalNode conditionNode1 = evaluateNode(node.getNode(0), context, currentContext);
        EvalNode conditionNode2 = evaluateNode(node.getNode(1), context, currentContext);
        return new BooleanEvalNode(EvalUtils.nodeAsBoolean(conditionNode1) & EvalUtils.nodeAsBoolean(conditionNode2));
    }

    private EvalNode processVarNode(AstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
        EvalNode valueNode = evaluateNode(node.getNode(0), context, currentContext);
        if (valueNode.getValue() != null) {
            currentContext.getVars().putIfAbsent(node.getValue() + "", valueNode.getValue());
        }
        return new EmptyEvalNode();
    }

    private EvalNode processArrayNode(AstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
        if (CollectionUtils.isEmpty(node.getNodes())) {
            return new ObjectEvalNode(Collections.emptyMap());
        }
        if (node.getNode(0).getType() == AstType.AST_VAR.getId()) {
            for (AstNode astNode : node.getNodes()) {
                evaluateNode(astNode, context, currentContext);
            }
            return new EmptyEvalNode();
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

    private EvalNode processUnaryMinus(AstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
        EvalNode res = evaluateNode(node.getNode(0), context, currentContext);
        if (res instanceof IntEvalNode) {
            Integer value = (Integer) res.getValue();
            return new IntEvalNode(-value);
        } else {
            throw new NotImplementedException();
        }
    }

    private EvalNode processLessOrEquals(AstNode node, Context context, Context.NodeContext currentContext) {
        throw new NotImplementedException();
    }

    private EvalNode<? extends Serializable> getValueNode(AstNode node) {
        if (node.getValue() == null) {
            return new NullEvalNode();
        }

        Object val = node.getValue();
        if (val == null) {
            return new NullEvalNode();
        } else if (val instanceof Boolean) {
            return new BooleanEvalNode((Boolean) val);
        } else if (val instanceof Integer) {
            return new IntEvalNode((Integer) val);
        }
        return new StringEvalNode(val + "");
    }

    private EvalNode processReferenceNode(AstNode node, Context context, Context.NodeContext currentContext) {
        Object value = getValueFromParentContext(currentContext, (String) node.getValue());
        if (value != null) {
            return EvalUtils.createEvalNode(value);
        } else {
            return new EmptyEvalNode();
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

    private EvalNode processOrNode(AstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
        EvalNode conditionNode1 = evaluateNode(node.getNode(0), context, currentContext);
        EvalNode conditionNode2 = evaluateNode(node.getNode(1), context, currentContext);
        return new BooleanEvalNode(EvalUtils.nodeAsBoolean(conditionNode1) || EvalUtils.nodeAsBoolean(conditionNode2));
    }

    private EvalNode processAndNode(AstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
        EvalNode conditionNode1 = evaluateNode(node.getNode(0), context, currentContext);
        EvalNode conditionNode2 = evaluateNode(node.getNode(1), context, currentContext);
        return new BooleanEvalNode(EvalUtils.nodeAsBoolean(conditionNode1) && EvalUtils.nodeAsBoolean(conditionNode2));
    }

    private EvalNode processNodeList(AstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
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

    private BooleanEvalNode processEqNode(AstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
        EvalNode left = evaluateNode(node.getNode(0), context, currentContext);
        EvalNode right = evaluateNode(node.getNode(1), context, currentContext);
        return new BooleanEvalNode(EvalUtils.equalityNode(left, right));
    }

    private EvalNode processIfNode(AstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
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

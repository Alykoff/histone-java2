package ru.histone.v2.evaluator;

import org.apache.commons.collections.CollectionUtils;
import ru.histone.HistoneException;
import ru.histone.v2.evaluator.node.*;
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
            EvalNode astNode = evaluateNode(currentNode, context, root);
            if (astNode.getValue() != null) {
                sb.append(astNode.getValue());
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
            return new NullAstNode();
        }

        if (node.getType() == Integer.MIN_VALUE) {
            return getValueNode(node);
        }

        AstType type = AstType.fromId(node.getType());
        if (type == null) {
            throw new HistoneException("WTF!?!?!?");
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
                break;
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
                break;
            case AST_IF:
                return processIfNode(node, context, currentContext);
            case AST_FOR:
                break;
            case AST_MACRO:
                break;
            case AST_RETURN:
                break;
            case AST_NODES:
                break;
            case AST_NODELIST:
                return processNodeList(node, context, currentContext);
            case AST_BOR:
                break;
            case AST_BXOR:
                break;
            case AST_BAND:
                break;
            case AST_SUPRESS:
                break;
            case AST_LISTEN:
                break;
            case AST_TRIGGER:
                break;
        }
        throw new HistoneException("WTF!?!?!? " + type);

    }

    private EvalNode processArrayNode(AstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
        if (CollectionUtils.isEmpty(node.getNodes())) {
            return new ObjectAstNode(Collections.emptyMap());
        }
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < node.getNodes().size() / 2; i++) {
            EvalNode key = evaluateNode(node.getNodes().get(i * 2), context, currentContext);
            EvalNode value = evaluateNode(node.getNodes().get(i * 2 + 1), context, currentContext);
            map.put(key.getValue() + "", value.getValue());
        }
        return new ObjectAstNode(map);
    }

    private EvalNode processUnaryMinus(AstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
        EvalNode res = evaluateNode(node.getNode(0), context, currentContext);
        if (res instanceof IntAstNode) {
            Integer value = (Integer) res.getValue();
            return new IntAstNode(-value);
        } else {
            throw new NotImplementedException();
        }
    }

    private EvalNode processLessOrEquals(AstNode node, Context context, Context.NodeContext currentContext) {
        throw new NotImplementedException();
    }

    private EvalNode<? extends Serializable> getValueNode(AstNode node) {
        if (node.getValue() == null) {
            return new StringAstNode("");
        }

        Object val = node.getValue();
        if (val == null) {
            return new NullAstNode();
        } else if (val instanceof Boolean) {
            return new BooleanAstNode((Boolean) val);
        } else if (val instanceof Integer) {
            return new IntAstNode((Integer) val);
        }
        return new StringAstNode(val + "");
    }

    private EvalNode processReferenceNode(AstNode node, Context context, Context.NodeContext currentContext) {
        Object value = getValueFromParentContext(currentContext, (String) node.getValue());
        if (value != null) {
            return null;
        } else {
            return new NullAstNode();
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
        BooleanAstNode conditionNode1 = (BooleanAstNode) evaluateNode(node.getNode(0), context, currentContext);
        BooleanAstNode conditionNode2 = (BooleanAstNode) evaluateNode(node.getNode(1), context, currentContext);
        return new BooleanAstNode(conditionNode1.getValue() || conditionNode2.getValue());
    }

    private EvalNode processAndNode(AstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
        BooleanAstNode conditionNode1 = (BooleanAstNode) evaluateNode(node.getNode(0), context, currentContext);
        BooleanAstNode conditionNode2 = (BooleanAstNode) evaluateNode(node.getNode(1), context, currentContext);
        return new BooleanAstNode(conditionNode1.getValue() && conditionNode2.getValue());
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
            return new StringAstNode(sb.toString());
        }
    }

    private BooleanAstNode processEqNode(AstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
        EvalNode left = evaluateNode(node.getNode(0), context, currentContext);
        EvalNode right = evaluateNode(node.getNode(1), context, currentContext);
        return new BooleanAstNode(EvalUtils.equalityNode(left, right));
    }

    private EvalNode processIfNode(AstNode node, Context context, Context.NodeContext currentContext) throws HistoneException {
        Context.NodeContext current = new Context.NodeContext();
        current.setParent(currentContext);
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

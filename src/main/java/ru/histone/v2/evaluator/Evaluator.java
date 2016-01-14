package ru.histone.v2.evaluator;

import org.apache.commons.lang.NotImplementedException;
import ru.histone.HistoneException;
import ru.histone.v2.evaluator.node.BooleanAstNode;
import ru.histone.v2.evaluator.node.StringAstNode;
import ru.histone.v2.parser.node.AstNode;
import ru.histone.v2.parser.node.AstType;

/**
 * Created by alexey.nevinsky on 12.01.2016.
 */
public class Evaluator {

    public String process(String baseUri, AstNode<?> node, Context context) throws HistoneException {
        context.setBaseUri(baseUri);
        return processInternal(node, context);
    }

    private String processInternal(AstNode<?> node, Context context) throws HistoneException {
        StringBuilder sb = new StringBuilder();
        for (AstNode currentNode : node.getNodes()) {
            sb.append(evaluateNode(currentNode, context).getValue());
        }
        return sb.toString();
    }

    private AstNode evaluateNode(AstNode node, Context context) throws HistoneException {
        if (node.getType() == Integer.MIN_VALUE) {
            return new StringAstNode(node.getValues().get(0) + "");
        }

        AstType type = AstType.fromId(node.getType());
        if (type == null) {
            throw new HistoneException("WTF!?!?!?");
        }

        switch (type) {
            case AST_NOP:
                break;
            case AST_ARRAY:
                break;
            case AST_REGEXP:
                break;
            case AST_THIS:
                break;
            case AST_GLOBAL:
                break;
            case AST_NOT:
                break;
            case AST_AND:
                return processAndNode(node, context);
            case AST_OR:
                return processOrNode(node, context);
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
                break;
            case AST_LT:
                break;
            case AST_GT:
                break;
            case AST_LE:
                break;
            case AST_GE:
                break;
            case AST_EQ:
                return processEqNode(node, context);
            case AST_NEQ:
                return processEqNode(node, context).neg();
            case AST_REF:
                break;
            case AST_METHOD:
                break;
            case AST_PROP:
                break;
            case AST_CALL:
                break;
            case AST_VAR:
                break;
            case AST_IF:
                return processIfNode(node, context);
            case AST_FOR:
                break;
            case AST_MACRO:
                break;
            case AST_RETURN:
                break;
            case AST_NODES:
                break;
            case AST_NODELIST:
                return processNodeList(node, context);
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

            default:
                throw new HistoneException("WTF!?!?!?");
        }
        throw new HistoneException("WTF!?!?!? " + type);

    }

    private AstNode processOrNode(AstNode node, Context context) throws HistoneException {
        BooleanAstNode conditionNode1 = (BooleanAstNode) evaluateNode(node.getNode(0), context);
        BooleanAstNode conditionNode2 = (BooleanAstNode) evaluateNode(node.getNode(1), context);
        return new BooleanAstNode(conditionNode1.getValue() || conditionNode2.getValue());
    }

    private AstNode processAndNode(AstNode node, Context context) throws HistoneException {
        BooleanAstNode conditionNode1 = (BooleanAstNode) evaluateNode(node.getNode(0), context);
        BooleanAstNode conditionNode2 = (BooleanAstNode) evaluateNode(node.getNode(1), context);
        return new BooleanAstNode(conditionNode1.getValue() && conditionNode2.getValue());
    }

    private AstNode processNodeList(AstNode node, Context context) throws HistoneException {
        if (node.getNodes().size() == 1) {
            AstNode node1 = node.getNode(0);
            return evaluateNode(node1, context);
        } else {
            throw new NotImplementedException();
        }
    }

    private BooleanAstNode processEqNode(AstNode node, Context context) throws HistoneException {
        AstNode left = evaluateNode(node.getNode(0), context);
        AstNode right = evaluateNode(node.getNode(1), context);
        return new BooleanAstNode(EvalUtils.equalityNode(left, right));
    }

    private AstNode processIfNode(AstNode node, Context context) throws HistoneException {
        BooleanAstNode conditionNode = (BooleanAstNode) evaluateNode(node.getNode(1), context);
        if (conditionNode.getValue()) {
            return evaluateNode(node.getNode(0), context);
        } else {
            return evaluateNode(node.getNode(2), context);
        }
    }

}

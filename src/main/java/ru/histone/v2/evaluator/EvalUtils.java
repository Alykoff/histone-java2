package ru.histone.v2.evaluator;

import org.apache.commons.lang.ObjectUtils;
import ru.histone.v2.evaluator.node.BooleanAstNode;
import ru.histone.v2.evaluator.node.IntAstNode;
import ru.histone.v2.evaluator.node.NullAstNode;
import ru.histone.v2.evaluator.node.StringAstNode;
import ru.histone.v2.parser.node.AstNode;

/**
 * Created by inv3r on 14/01/16.
 */
public class EvalUtils {
    public static boolean equalityNode(AstNode node1, AstNode node2) {
        //todo normal equality logic
        return ObjectUtils.equals(node1.getValue(), node2.getValue());
    }

    public static boolean nodeAsBoolean(AstNode node) {
        if (node instanceof NullAstNode) {
            return false;
        } else if (node instanceof BooleanAstNode) {
            return (Boolean) node.getValue();
        } else if (node instanceof IntAstNode) {
            return ((int) node.getValue()) != 0;
        } else if (node instanceof StringAstNode) {
            return !node.getValue().equals("");
        }
        return true;
    }
}

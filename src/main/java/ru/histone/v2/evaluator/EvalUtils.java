package ru.histone.v2.evaluator;

import org.apache.commons.lang.ObjectUtils;
import ru.histone.v2.parser.node.AstNode;

/**
 * Created by inv3r on 14/01/16.
 */
public class EvalUtils {
    public static boolean equalityNode(AstNode node1, AstNode node2) {
        //todo normal equality logic
        return ObjectUtils.equals(node1.getValue(), node2.getValue());
    }
}

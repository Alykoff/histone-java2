package ru.histone.v2.utils;

import ru.histone.v2.parser.node.AstNode;
import ru.histone.v2.parser.node.ExpAstNode;
import ru.histone.v2.parser.node.StringAstNode;
import ru.histone.v2.parser.node.ValueNode;

import java.util.List;

/**
 * Created by alexey.nevinsky on 12.01.2016.
 */
public class ParserUtils {
    public static String astToString(AstNode node) {
        StringBuffer sb = new StringBuffer();
        nodeToString(sb, node);
        return sb.toString();
    }

    private static void nodeToString(StringBuffer sb, AstNode node) {
        if (node.hasValue()) {
            Object nodeValue = ((ValueNode) node).getValue();
            if (nodeValue instanceof Number || nodeValue instanceof Boolean || nodeValue == null) {
                sb.append(nodeValue);
            } else {
                sb.append("\"").append(nodeValue).append("\"");
            }
        } else {
            List<AstNode> nodes = ((ExpAstNode) node).getNodes();
            sb.append("[").append(node.getTypeId());
            if (nodes.size() > 0) {
                for (AstNode child : nodes) {
                    sb.append(",");
                    nodeToString(sb, child);
                }
            }
            sb.append("]");
        }
    }

    public static String getValueFromStringNode(AstNode node) {
        return ((StringAstNode) node).getValue();
    }

    public static boolean isLeafNodeType(AstNode node) {
        return node != null && node.hasValue();
    }

    public static boolean isString(Object obj) {
        return obj instanceof String;
    }

    public static boolean isNumber(Object value) {
        if (value instanceof Number) {
            return true;
        } else if (!isString(value)) {
            return false;
        }

        String v = (String) value;
        try {
            Integer.parseInt(v);
            return true;
        } catch (Exception ignore) {

        }

        try {
            Double.parseDouble(v);
            return true;
        } catch (Exception ignore) {

        }
        return false;
    }

    public static boolean isInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (Exception ignore) {
            return false;
        }
    }

    public static Float isFloat(String val) {
        try {
            return Float.parseFloat(val);
        } catch (Exception ignore) {
            return null;
        }
    }
}

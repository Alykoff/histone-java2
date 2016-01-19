package ru.histone.v2.utils;

import org.apache.commons.lang.StringUtils;
import ru.histone.v2.evaluator.node.BooleanAstNode;
import ru.histone.v2.evaluator.node.IntAstNode;
import ru.histone.v2.evaluator.node.NullAstNode;
import ru.histone.v2.parser.node.AstNode;

/**
 * Created by alexey.nevinsky on 12.01.2016.
 */
public class ParserUtils {
    public static String astToString(AstNode node) {
        StringBuffer sb = new StringBuffer();
        nodeToString(sb, node);
        return sb.toString();
    }

    private static void nodeToString(StringBuffer sb, AstNode<?> node) {
        if (node.getType() == Integer.MIN_VALUE) {
            if (node instanceof NullAstNode) {
                sb.append("null");
            } else if (node instanceof IntAstNode) {
                sb.append(node.getValues().get(0));
            } else {
                sb.append("\"").append(node.getValues().get(0)).append("\"");
            }
        } else {
            sb.append("[").append(node.getType());
            if (node.getValues().size() > 0) {
                sb.append(",\"");
                sb.append(StringUtils.join(node.getValues(), "\", \""));
                sb.append("\"");
            }
            if (node.getNodes().size() > 0) {
                for (AstNode child : node.getNodes()) {
                    sb.append(",");
                    nodeToString(sb, child);
                }
            }
            sb.append("]");
        }
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

    public static boolean isDouble(String val) {
        try {
            Double.parseDouble(val);
            return true;
        } catch (Exception ignore) {
            return false;
        }
    }
}

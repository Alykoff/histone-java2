package ru.histone.v2.utils;

import org.apache.commons.lang.StringUtils;
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
        sb.append("[").append(node.getType());
        if (node.getType() != Integer.MIN_VALUE) {
            for (AstNode child : node.getNodes()) {
                sb.append(",");
                nodeToString(sb, child);
            }
            sb.append("]");
        } else {
            sb.append(",'");
            sb.append(StringUtils.join(node.getValues(), "', '"));
            sb.append("']");
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

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
}

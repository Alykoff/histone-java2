package ru.histone.v2.parser;

import ru.histone.v2.parser.node.AstNode;
import ru.histone.v2.parser.node.AstType;

/**
 * Created by alexey.nevinsky on 12.01.2016.
 */
public class Optimizer {

    public void mergeStrings(AstNode node) {
        if (node.getType() == AstType.AST_NODELIST.getId() || node.getType() == AstType.AST_NODES.getId()) {

        } else {
            node.getNodes().forEach(this::mergeStrings);
        }
    }
}

package ru.histone.v2.parser;

import ru.histone.v2.parser.node.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexey.nevinsky on 12.01.2016.
 */
public class Optimizer {
    public AstNode mergeStrings(AstNode rawNode) {
        if (rawNode.hasValue()) {
            return rawNode;
        }
        final ExpAstNode node = (ExpAstNode) rawNode;
        final List<AstNode> innerNodes = node.getNodes();
        if (node.getType() == AstType.AST_NODELIST || node.getType() == AstType.AST_NODES) {
            final List<AstNode> newInnerNodes = getNewNodes(innerNodes);
            node.rewriteNodes(newInnerNodes);
        } else {
            innerNodes.forEach(this::mergeStrings);
        }

        return node;
    }

    private List<AstNode> getNewNodes(List<AstNode> innerNodes) {
        final StringBuilder accStringNode = new StringBuilder();
        final List<AstNode> res = new ArrayList<>(innerNodes.size());

        for (int i = 0; i < innerNodes.size(); i++) {
            AstNode current = innerNodes.get(i);
            boolean isStringLeaf = current.hasValue();

            if (isStringLeaf) {
                accStringNode.append(((ValueNode) current).getValue());
            }

            if (!isStringLeaf && accStringNode.length() > 0) {
                res.add(new StringAstNode(accStringNode.toString()));
                accStringNode.setLength(0);
            }

            if (i == innerNodes.size() - 1 && accStringNode.length() > 0) {
                res.add(new StringAstNode(accStringNode.toString()));
            }

            if (!isStringLeaf) {
                current = mergeStrings(current);
                res.add(current);
            }
        }
        return res;
    }
}

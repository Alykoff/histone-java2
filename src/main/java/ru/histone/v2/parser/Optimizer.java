package ru.histone.v2.parser;

import ru.histone.v2.parser.node.AstNode;
import ru.histone.v2.parser.node.AstType;
import ru.histone.v2.utils.ParserUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexey.nevinsky on 12.01.2016.
 */
public class Optimizer {
    private static AstNode buildStringLeafNode(StringBuilder value) {
        return AstNode.forValue(value.toString());
    }

    public AstNode mergeStrings(AstNode node) {

        final List<AstNode> innerNodes = node.getNodes();
        if (node.getType() == AstType.AST_NODELIST.getId() || node.getType() == AstType.AST_NODES.getId()) {
            final List<AstNode> newInnerNodes = getNewNodes(innerNodes);
            node.setNodes(newInnerNodes);
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
            boolean isStringLeaf = ParserUtils.isStringLeafNode(current);

            if (isStringLeaf) {
                accStringNode.append(current.getValue());
            }

            if (!isStringLeaf && accStringNode.length() > 0) {
                res.add(buildStringLeafNode(accStringNode));
                accStringNode.setLength(0);
            }

            if (i == innerNodes.size() - 1 && accStringNode.length() > 0) {
                res.add(buildStringLeafNode(accStringNode));
            }

            if (!isStringLeaf) {
                current = mergeStrings(current);
                res.add(current);
            }
        }
        return res;
    }
}

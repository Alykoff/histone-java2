package ru.histone.v2.parser;

import ru.histone.v2.parser.node.AstNode;
import ru.histone.v2.parser.node.AstType;
import ru.histone.v2.utils.ParserUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by alexey.nevinsky on 12.01.2016.
 */
public class Optimizer {
    public AstNode mergeStrings(AstNode node) {
        final int nodeType = node.getType();
        final List<AstNode> innerNodes = node.getNodes();
        final boolean isNodeHaveListOfNodes = nodeType == AstType.AST_NODELIST.getId()
                || nodeType == AstType.AST_NODES.getId();
        if (!isNodeHaveListOfNodes) {
            final List<AstNode> newInnerNodes = innerNodes
                    .stream()
                    .parallel()
                    .map(Optimizer.this::mergeStrings)
                    .collect(Collectors.toList());
            node.setNodes(newInnerNodes);
            return node;
        }

        final int innerNodesSize = innerNodes.size();
        int innerNodesCounter = 0;

        final List<AstNode> newInnerNodes = new ArrayList<>(innerNodesSize);
        StringBuilder accStringNode = null;
        boolean isEndOfLoop = innerNodesCounter >= innerNodesSize;
        while (!isEndOfLoop) {
            final AstNode currentNode = node.getNode(innerNodesCounter);
            isEndOfLoop = ++innerNodesCounter >= innerNodesSize;
            boolean isStringLeafNode = ParserUtils.isStringLeafNode(currentNode);
            if (isStringLeafNode) {
                final Object currentNodeValue = currentNode.getValue();
                accStringNode = accStringNode == null
                        ? new StringBuilder().append(currentNodeValue)
                        : accStringNode.append(currentNodeValue);
            }


            if (!isStringLeafNode && accStringNode != null) {
                newInnerNodes.add(buildStringLeafNode(accStringNode));
                accStringNode = null;
            }

            if (isEndOfLoop && accStringNode != null) {
                newInnerNodes.add(buildStringLeafNode(accStringNode));
            }

            if (!isStringLeafNode) {
                newInnerNodes.add(currentNode);
            }

//            final int currentNodeType = currentNode.getType();
//            if (currentNodeType == AstType.AST_VAR.getId()) {
//
//            } else {
        }
        node.setNodes(newInnerNodes);
        return node;
    }

    private static AstNode buildStringLeafNode(StringBuilder value) {
        return new AstNode(AstNode.LEAF_NODE_TYPE_ID)
                .setValue(value.toString());
    }
}

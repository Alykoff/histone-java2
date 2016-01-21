package ru.histone.v2.parser;

import ru.histone.HistoneException;
import ru.histone.v2.parser.node.*;
import ru.histone.v2.utils.ParserUtils;

import java.util.*;

/**
 *
 * Created by gali.alykoff on 20/01/16.
 */
public class Marker {
    public static final String SELF_SCOPE_ID = "self";

    // TODO
    public void markReferences(
            AstNode rawNode, Deque<Map<String, Long>> scopeChain
    ) throws HistoneException {
        if (rawNode == null || rawNode.hasValue()) {
            return;
        }
        if (scopeChain == null) {
            scopeChain = new LinkedList<>();
        }
        final ExpAstNode node = (ExpAstNode) rawNode;
        final AstType type = node.getType();
        switch (type) {
            case AST_REF: {
                markRef(node, scopeChain);
                break;
            }
            case AST_VAR: {
                markVar(node, scopeChain);
                break;
            }
            case AST_IF: {
                final int startSearchElsePosition = 0;
                markReferenceForElseStatement(startSearchElsePosition, node, scopeChain);
                break;
            }
            case AST_FOR: {
                markFor(node, scopeChain);
                break;
            }
            case AST_MACRO: {
                markMacro(node, scopeChain);
                break;
            }
            case AST_NODES: {
                markNodes(node, scopeChain);
                break;
            }
            default: {
                for (int i = 0; i < node.size(); i++) {
                    markReferences(node.getNode(i), scopeChain);
                }
                break;
            }
        }
    }

    private void markRef(
            ExpAstNode node, Deque<Map<String, Long>> scopeChain
    ) throws HistoneException {
        final String nameOfVar = ((StringAstNode) node.getNode(0)).getValue();
        final ExpAstNode refNode = getReference(nameOfVar, scopeChain);
        node.rewriteNodes(Collections.singletonList(refNode));
    }

    private void markVar(
            ExpAstNode node, Deque<Map<String, Long>> scopeChain
    ) throws HistoneException {
        final AstNode expNode = node.getNode(0);
        final AstNode nameNode = node.getNode(1);
        markReferences(expNode, scopeChain);
        final String nameOfVar = ParserUtils.getValueFromStringNode(nameNode);
        node.setNode(1, setReference(nameOfVar, scopeChain));
    }

    private void markNodes(
            ExpAstNode node, Deque<Map<String, Long>> scopeChain
    ) throws HistoneException {
        scopeChain.push(new HashMap<>());
        final int size = node.size();
        for (int i = 0; i < size; i++) {
            markReferences(node.getNode(i), scopeChain);
        }
        scopeChain.pop();
    }

    private void markFor(
            ExpAstNode node, Deque<Map<String, Long>> scopeChain
    ) throws HistoneException {
        scopeChain.push(new HashMap<>());
        setReference(SELF_SCOPE_ID, scopeChain);
        final int[] varIndexes = new int[] {0, 1};
        for (int i : varIndexes) {
            final StringAstNode keyVarNode = node.getNode(i);
            final String keyVar = keyVarNode.getValue();
            if (keyVar != null) {
                node.setNode(i, setReference(keyVar, scopeChain));
            }
        }
        final int outputIndex = 2;
        final ExpAstNode outputNode = node.getNode(outputIndex);
        markReferences(outputNode, scopeChain);
        scopeChain.pop();

        final int collectionIndex = 3;
        final AstNode collectionNode = node.getNode(collectionIndex);
        markReferences(collectionNode, scopeChain);

        final int startElseStatement = 4;
        markReferenceForElseStatement(startElseStatement, node, scopeChain);
    }

    private void markMacro(
            ExpAstNode node, Deque<Map<String, Long>> scopeChain
    ) throws HistoneException {
        final int startVarIndex = 2;
        final int nodeSize = node.size();
        for (int i = startVarIndex; i < nodeSize; i++) {
            final ExpAstNode child = node.getNode(i);
            final int expressionIndex = 2;
            final int childSize = child.size();
            if (childSize > expressionIndex) {
                markReferences((child).getNode(expressionIndex), scopeChain);
            }
        }
        scopeChain.push(new HashMap<>());
        setReference(SELF_SCOPE_ID, scopeChain);

        final List<AstNode> param = new ArrayList<>();
        for (int i = startVarIndex; i < nodeSize; i++) {
            final ExpAstNode paramNode = node.getNode(i);
            final StringAstNode varNameNode = paramNode.getNode(1);
            setReference(varNameNode.getValue(), scopeChain);
            if (paramNode.size() > 2) {
                final ExpAstNode expAstNode = paramNode.getNode(2);
                param.add(new LongAstNode(i - startVarIndex));
                param.add(expAstNode);
            }
        }
        if (!param.isEmpty()) {
            final List<AstNode> newNodes = node.getNodes().subList(0, startVarIndex);
            newNodes.addAll(param);
            node.rewriteNodes(newNodes);
        }
        markReferences(node.getNode(0), scopeChain);
        scopeChain.pop();
    }

    private void markReferenceForElseStatement(
                int startIndex, ExpAstNode node, Deque<Map<String, Long>> scopeChain
    ) throws HistoneException {
        final int nodesSize = node.size();
        for (int i = startIndex; i < nodesSize; i += 2) {
            final int conditionIndex = i + 1;
            // process condition if it's present
            if (conditionIndex < nodesSize) {
                markReferences(node.getNode(conditionIndex), scopeChain);
            }
            scopeChain.push(new HashMap<>());
            markReferences(node.getNode(i), scopeChain);
            scopeChain.pop();
        }
    }

    private ExpAstNode getReference(String name, Deque<Map<String, Long>> scopeChain) {
        int scopeIndex = scopeChain.size();
        final int currentScope = scopeIndex - 1;
        final Iterator<Map<String, Long>> iterator = scopeChain.descendingIterator();
        while (iterator.hasNext()) {
            scopeIndex--;
            final Map<String, Long> scope = iterator.next();
            final Long variableIndex = scope.get(name);
            if (variableIndex != null) {
                final int scopeDeep = currentScope - scopeIndex;;
                final AstNode deepOfVarDefinitionNode = new LongAstNode(scopeDeep);
                final AstNode varIndexNode = new LongAstNode(variableIndex);
                return new ExpAstNode(AstType.AST_REF)
                        .add(deepOfVarDefinitionNode)
                        .add(varIndexNode);
            }
        }

        final AstNode globalNode = new ExpAstNode(AstType.AST_GLOBAL);
        final AstNode nameOfGlobalVarNode = new StringAstNode(name);
        return new ExpAstNode(AstType.AST_METHOD)
                .add(globalNode)
                .add(nameOfGlobalVarNode);
    }

    private LongAstNode setReference(String name, Deque<Map<String, Long>> scopeChain) {
        final Map<String, Long> lastScope = scopeChain.getLast();
        if (!lastScope.containsKey(name)) {
            lastScope.put(name, (long) lastScope.keySet().size());
        }
        return new LongAstNode(lastScope.get(name));
    }
}

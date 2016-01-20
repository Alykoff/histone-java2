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

    // TODO
    public void markReferences(
            AstNode rawNode, Deque<Map<String, Integer>> scopeChain
    ) throws HistoneException {
        if (rawNode.hasValue()) {
            return;
        }
        if (scopeChain == null) {
            scopeChain = new LinkedList<>();
        }
        final ExpAstNode astNode = (ExpAstNode) rawNode;
        final AstType type = astNode.getType();
        switch (type) {
            case AST_REF:
                String nameOfVar = ((StringAstNode) astNode.getNode(0)).getValue();
                final ExpAstNode refNode = getReference(nameOfVar, scopeChain);
                astNode.setNodes(Collections.singletonList(refNode));
                break;
            case AST_VAR:
                markReferences(astNode.getNode(0), scopeChain);
                nameOfVar = ParserUtils.getValueFromStringNode(astNode.getNode(1));
//                final String name = setReference(nameOfVar, scopeChain);

                break;
            case AST_IF: break;
            case AST_FOR: break;
            case AST_MACRO: break;
            case AST_NODES: break;
            default: break;
        }
    }

    private ExpAstNode getReference(String name, Deque<Map<String, Integer>> scopeChain) {
        int scopeIndex = scopeChain.size();
        final int currentScope = scopeIndex - 1;
        final Iterator<Map<String, Integer>> iterator = scopeChain.descendingIterator();
        while (iterator.hasNext()) {
            scopeIndex--;
            final Map<String, Integer> scope = iterator.next();
            final Integer variableIndex = scope.get(name);
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

    private Integer setReference(String name, Deque<Map<String, Integer>> scopeChain) {
        final Map<String, Integer> lastScope = scopeChain.getLast();
        if (!lastScope.containsKey(name)) {
            lastScope.put(name, lastScope.keySet().size());
        }
        return lastScope.get(name);
    }
}

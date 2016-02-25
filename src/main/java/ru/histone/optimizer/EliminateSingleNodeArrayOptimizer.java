/**
 *    Copyright 2013 MegaFon
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package ru.histone.optimizer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import ru.histone.evaluator.nodes.NodeFactory;
import ru.histone.v2.exceptions.HistoneException;

/**
 * Упрощает AST дерево, объединяя несколько подряд следующих текстовых элементов в одну строку.
 */
public class EliminateSingleNodeArrayOptimizer extends AbstractASTWalker {
    public EliminateSingleNodeArrayOptimizer(NodeFactory nodeFactory) {
        super(nodeFactory);
    }

    protected ArrayNode simplifyArrayNode(ArrayNode ast) throws HistoneException {
        ArrayNode result = null;

        if (ast.size() == 1 && ast.get(0).isArray() && ast.get(0).size() > 0) {
            if (ast.get(0).get(0).isTextual()) {
                result = (ArrayNode) ast.get(0);
            } else if (ast.get(0).get(0).isArray()) {
                result = simplifyArrayNode((ArrayNode) ast.get(0));
            } else {
                result = (ArrayNode) processAstNode(ast);
            }
        } else {
            ArrayNode newAst = nodeFactory.jsonArray();
            for (JsonNode node : ast) {
                if (node.isArray()) {
                    newAst.add(processAstNode(node));
                } else {
                    newAst.add(node);
                }
            }
            result = newAst;
        }

        return result;
    }

    @Override
    public ArrayNode process(ArrayNode ast) throws HistoneException {
        ast = removeHistoneAstSignature(ast);

        return simplifyArrayNode(ast);
    }

    @Override
    public void pushContext() {
        // There is no context to push in this optimizer
    }

    @Override
    public void popContext() {
        // There is no context to pop in this optimizer
    }
}

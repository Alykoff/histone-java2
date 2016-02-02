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
import com.fasterxml.jackson.databind.node.TextNode;
import ru.histone.HistoneException;
import ru.histone.evaluator.nodes.NodeFactory;

/**
 * Упрощает AST дерево, объединяя несколько подряд следующих текстовых элементов в одну строку.
 */
public class FragmentsConcatinationOptimizer extends AbstractASTWalker {
    public FragmentsConcatinationOptimizer(NodeFactory nodeFactory) {
        super(nodeFactory);
    }

    protected ArrayNode simplifyArrayNode(ArrayNode ast) throws HistoneException {
        ArrayNode result = nodeFactory.jsonArray();
        StringBuilder sb = new StringBuilder();
        for (JsonNode node : ast) {
            if (node.isTextual()) {
                // if we have fragment here
                sb.append(node.asText());
//            } else if (node.isArray() && node.size() == 2 && node.get(0).isInt() && node.get(0).asInt() == AstType.STRING) {
//                //if we have text constant here
//                sb.append(node.get(1).asText());
            } else {
                if (sb.length() > 0) {
                    result.add(nodeFactory.jsonString(sb.toString()));
                    sb.setLength(0);
                }
                JsonNode processedNode = processAstNode(node);
                if (processedNode.isArray() && processedNode.get(0).isTextual()) {
                    processedNode = simplifyArrayNode((ArrayNode) processedNode);
                }
                if (processedNode.isArray() && processedNode.size() == 1 && processedNode.get(0).isTextual()) {
                    if (result.size() > 0 && result.get(result.size() - 1).isTextual()) {
                        TextNode prev = (TextNode) result.get(result.size() - 1);
                        TextNode cur = (TextNode) processedNode.get(0);
                        TextNode newElem = (TextNode) nodeFactory.jsonString(prev.textValue() + cur.textValue());
                        result.set(result.size() - 1, newElem);
                    } else {
                        result.add(processedNode.get(0));
                    }
                } else if (processedNode.isArray() && processedNode.size() == 0) {
                    //do nothing
                } else {
                    result.add(processedNode);
                }
            }
        }
        if (sb.length() > 0) {
            if (result.size() > 0 && result.get(result.size() - 1).isTextual()) {
                TextNode prev = (TextNode) result.get(result.size() - 1);
                TextNode cur = (TextNode) nodeFactory.jsonString(sb.toString());
                TextNode newElem = (TextNode) nodeFactory.jsonString(prev.textValue() + cur.textValue());
                result.set(result.size() - 1, newElem);
            } else {
                result.add(nodeFactory.jsonString(sb.toString()));
            }
        }
        return result;
    }

    @Override
    public ArrayNode process(ArrayNode ast) throws HistoneException {
        ast = removeHistoneAstSignature(ast);

        ArrayNode jsonNodes = simplifyArrayNode(ast);

        if (jsonNodes.isArray() && jsonNodes.size() > 0 && jsonNodes.get(0).isInt()) {
            jsonNodes = nodeFactory.jsonArray(jsonNodes);
        }

        return jsonNodes;
    }

    @Override
    protected JsonNode processStatements(ArrayNode ast) throws HistoneException {
        int type = ast.get(0).asInt();
        ast = (ArrayNode) ast.get(1);
        return nodeFactory.jsonArray(type, simplifyArrayNode(ast));
    }

    @Override
    protected JsonNode processFor(ArrayNode ast) throws HistoneException {
        int type = ast.get(0).asInt();
        ArrayNode var = (ArrayNode) ast.get(1);
        ArrayNode collection = (ArrayNode) ast.get(2);
        ArrayNode statements = (ArrayNode) ast.get(3).get(0);

        ArrayNode elseStatements = null;
        if (ast.get(3).size() > 1) {
            elseStatements = (ArrayNode) ast.get(3).get(1);
        }

        collection = (ArrayNode) processAstNode(collection);

        pushContext();
        ArrayNode statementsOut = nodeFactory.jsonArray();
        for (int i = 0; i < statements.size(); i++) {
            statementsOut.add(processAstNode(statements.get(i)));
        }
        popContext();

        ArrayNode elseStatementsOut = null;
        if (elseStatements != null) {
            elseStatementsOut = nodeFactory.jsonArray();
            for (int i = 0; i < elseStatements.size(); i++) {
                elseStatementsOut.add(processAstNode(elseStatements.get(i)));
            }
        }

        ArrayNode statementsContainer = elseStatementsOut == null ?
                nodeFactory.jsonArray(simplifyArrayNode(statementsOut)) :
                nodeFactory.jsonArray(simplifyArrayNode(statementsOut), simplifyArrayNode(elseStatementsOut));

        return nodeFactory.jsonArray(type, var, collection, statementsContainer);
    }

    @Override
    protected JsonNode processMacro(ArrayNode expr_macro) throws HistoneException {
        int type = expr_macro.get(0).asInt();
        JsonNode name = expr_macro.get(1);
        ArrayNode args = (ArrayNode) expr_macro.get(2);
        ArrayNode statements = (ArrayNode) expr_macro.get(3);

        pushContext();
        args = (ArrayNode) processAstNode(args);
        statements = (ArrayNode) processArrayOfAstNodes(statements);
        popContext();

        return nodeFactory.jsonArray(type, name, args, simplifyArrayNode(statements));
    }

    @Override
    protected JsonNode processIf(ArrayNode ast) throws HistoneException {
        int type = ast.get(0).asInt();
        ArrayNode conditions = (ArrayNode) ast.get(1);

        ArrayNode conditionsOut = nodeFactory.jsonArray();

        for (JsonNode condition : conditions) {
            JsonNode expression = condition.get(0);
            JsonNode statements = condition.get(1);

            expression = processAstNode(expression);

            pushContext();
            ArrayNode statementsOut = nodeFactory.jsonArray();
            for (int i = 0; i < statements.size(); i++) {
                statementsOut.add(processAstNode(statements.get(i)));
            }
            popContext();

            ArrayNode conditionOut = nodeFactory.jsonArray();
            conditionOut.add(expression);
            conditionOut.add(simplifyArrayNode(statementsOut));
            conditionsOut.add(conditionOut);
        }

        return nodeFactory.jsonArray(type, conditionsOut);
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

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
import ru.histone.parser.AstNodeType;
import ru.histone.v2.exceptions.HistoneException;

/**
 * This optimization unit simplifies 'if' exporessions, if condition is constant (so it either removes the whole if block or
 * leaves statements without 'if' condition.
 * <p/>
 * User: sazonovkirill@gmail.com
 * Date: 09.01.13
 */
public class ConstantIfCases extends AbstractASTWalker {
    public ConstantIfCases(NodeFactory nodeFactory) {
        super(nodeFactory);
    }

    public ArrayNode replaceConstantIfs(ArrayNode ast) throws HistoneException {
        return process(ast);
    }

    protected JsonNode processIf(ArrayNode ast) throws HistoneException {
        ArrayNode conditions = (ArrayNode) ast.get(1);

        if (conditions.size() == 1 &&
                conditions.get(0).size() == 2 &&
                conditions.get(0).get(0).size() == 1 &&
                (conditions.get(0).get(0).get(0).asInt() == AstNodeType.TRUE ||
                        conditions.get(0).get(0).get(0).asInt() == AstNodeType.FALSE)) {

            JsonNode condition = conditions.get(0);
            JsonNode expression = condition.get(0);
            JsonNode statements = condition.get(1);

            if (expression.get(0).asInt() == AstNodeType.TRUE) {
                return nodeFactory.jsonArray(AstNodeType.STATEMENTS, processArrayOfAstNodes(statements));
            } else {
                return nodeFactory.jsonString("");
            }
        } else {
            ArrayNode conditionsOut = nodeFactory.jsonArray();

            for (JsonNode condition : conditions) {
                JsonNode expression = condition.get(0);
                JsonNode statements = condition.get(1);

                expression = processAstNode(expression);

                pushContext();
                JsonNode[] statementsOut = new JsonNode[statements.size()];
                for (int i = 0; i < statements.size(); i++) {
                    statementsOut[i] = processAstNode(statements.get(i));
                }
                popContext();

                ArrayNode conditionOut = nodeFactory.jsonArray();
                conditionOut.add(expression);
                conditionOut.add(nodeFactory.jsonArray(statementsOut));
                conditionsOut.add(conditionOut);
            }

            return nodeFactory.jsonArray(AstNodeType.IF, conditionsOut);
        }
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

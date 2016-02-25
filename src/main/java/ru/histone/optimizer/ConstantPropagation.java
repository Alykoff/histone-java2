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
import ru.histone.utils.Assert;
import ru.histone.v2.exceptions.HistoneException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class ConstantPropagation extends AbstractASTWalker {
    private Context context;

    public ConstantPropagation(NodeFactory nodeFactory) {
        super(nodeFactory);
    }

//    public ArrayNode propagateConstants(ArrayNode ast) throws HistoneException {
//        context = new Context();
//
//        return process(ast);
//    }
//
    @Override
    public void pushContext() {
        context.push();
    }

    @Override
    public void popContext() {
        context.pop();
    }

    @Override
    protected JsonNode processFor(ArrayNode ast) throws HistoneException {
        ArrayNode var = (ArrayNode) ast.get(1);
        ArrayNode collection = (ArrayNode) ast.get(2);
        ArrayNode statements = (ArrayNode) ast.get(3).get(0);

        ArrayNode elseStatements = null;
        if (ast.get(3).size() > 1) {
            elseStatements = (ArrayNode) ast.get(3).get(1);
        }

        pushContext();
        JsonNode[] statementsOut = new JsonNode[statements.size()];
        for (int i = 0; i < statements.size(); i++) {
            statementsOut[i] = processAstNode(statements.get(i));
        }
        popContext();

        JsonNode[] elseStatementsOut = null;
        if (elseStatements != null) {
            pushContext();
            elseStatementsOut = new JsonNode[elseStatements.size()];
            for (int i = 0; i < elseStatements.size(); i++) {
                elseStatementsOut[i] = processAstNode(elseStatements.get(i));
            }
            popContext();
        }

        collection = (ArrayNode) processAstNode(collection);

        ArrayNode statementsContainer = elseStatementsOut == null ?
                nodeFactory.jsonArray(nodeFactory.jsonArray(statementsOut)) :
                nodeFactory.jsonArray(nodeFactory.jsonArray(statementsOut), nodeFactory.jsonArray(elseStatementsOut));

        return nodeFactory.jsonArray(AstNodeType.FOR, var, collection, statementsContainer);
    }

    @Override
    protected JsonNode processIf(ArrayNode ast) throws HistoneException {
        ArrayNode conditions = (ArrayNode) ast.get(1);

        ArrayNode conditionsOut = nodeFactory.jsonArray();

        for (JsonNode condition : conditions) {
            JsonNode expression = condition.get(0);
            JsonNode statements = condition.get(1);

            pushContext();
            JsonNode[] statementsOut = new JsonNode[statements.size()];
            for (int i = 0; i < statements.size(); i++) {
                statementsOut[i] = processAstNode(statements.get(i));
            }
            popContext();

            expression = processAstNode(expression);

            ArrayNode conditionOut = nodeFactory.jsonArray();
            conditionOut.add(expression);
            conditionOut.add(nodeFactory.jsonArray(statementsOut));
            conditionsOut.add(conditionOut);
        }

        return nodeFactory.jsonArray(AstNodeType.IF, conditionsOut);
    }

    @Override
    protected JsonNode processMacro(ArrayNode macro) throws HistoneException {
        JsonNode name = macro.get(1);
        ArrayNode args = (ArrayNode) macro.get(2);
        ArrayNode statements = (ArrayNode) macro.get(3);

        pushContext();
        args = (ArrayNode) processAstNode(args);
        statements = (ArrayNode) processArrayOfAstNodes(statements);
        popContext();

        return nodeFactory.jsonArray(AstNodeType.MACRO, name, args, statements);
    }

    protected JsonNode processVariable(ArrayNode ast) throws HistoneException {
        Assert.isTrue(ast.size() == 3);

        JsonNode var = ast.get(1);
        String varName = var.asText();
        context.removeVar(varName);

        JsonNode preValue = ast.get(2);
        JsonNode valueNode = processAstNode(preValue);

        if (valueNode.isArray()) {
            ArrayNode value = (ArrayNode) valueNode;
            if (var.isTextual() && (isConstant(value) || /*isStatements(value) || */isMapOfConstants(value))) {
                context.putVar(varName, value);
            }
        }

        return nodeFactory.jsonArray(AstNodeType.VAR, var, valueNode);
    }

    protected JsonNode processSelector(ArrayNode ast) throws HistoneException {
        JsonNode fullVariable = ast.get(1);

        boolean arrayTokenFound = false;
        JsonNode[] processedTokens = new JsonNode[fullVariable.size()];
        for (int i = 0; i < processedTokens.length; i++) {
            JsonNode token = fullVariable.get(i);
            if (token.isArray()) {
                processedTokens[i] = processAstNode(token);
                arrayTokenFound = true;
            } else {
                processedTokens[i] = token;
            }
        }

        if (arrayTokenFound) {
            return nodeFactory.jsonArray(AstNodeType.SELECTOR, nodeFactory.jsonArray(processedTokens));
        }

        JsonNode token = processedTokens[0];
        if (token.isTextual()) {
            String varName = token.asText();
            if (context.hasVar(varName)) {
                ArrayNode value = context.getVarValue(varName);
                for (int i = 1; i < fullVariable.size(); i++) {
                    JsonNode t = fullVariable.get(i);
                    if (t.isTextual()) {
                        value = findValueInAstMap(value, t.asText());
                    } else if (t.isArray() && t.size() == 2 && t.get(0).isInt() && t.get(0).asInt() == AstNodeType.STRING) {
                        value = findValueInAstMap(value, t.get(1).asText());
                    } else if (t.isArray() && t.size() == 2 && t.get(0).isInt() && t.get(0).asInt() == AstNodeType.INT) {
                        value = findValueInAstMap(value, t.get(1).asInt());
                    } else {
                        break;
                    }

                    if (value == null) break;
                }
                if (value != null) return value;
            }
        }

        return nodeFactory.jsonArray(AstNodeType.SELECTOR, nodeFactory.jsonArray(processedTokens));
    }

    private ArrayNode findValueInAstMap(ArrayNode mapEntries, String key) {
        mapEntries = (ArrayNode) mapEntries.get(1);
        for (JsonNode jsonNode : mapEntries) {
            ArrayNode mapEntry = (ArrayNode) jsonNode;
            JsonNode entryKey = mapEntry.get(0);
            ArrayNode entryValue = (ArrayNode) mapEntry.get(1);

            if (entryKey.asText().equals(key)) {
                return entryValue;
            }
        }
        return null;
    }

    private ArrayNode findValueInAstMap(ArrayNode mapEntries, int index) {
        mapEntries = (ArrayNode) mapEntries.get(1);
        boolean isArray = true;
        for (JsonNode mapEntry : mapEntries) {
            JsonNode key = mapEntry.get(0);
            isArray = isArray && key.isNull();
        }
        if (!isArray) {
            for (JsonNode jsonNode : mapEntries) {
                ArrayNode mapEntry = (ArrayNode) jsonNode;
                JsonNode entryKey = mapEntry.get(0);
                ArrayNode entryValue = (ArrayNode) mapEntry.get(1);

                if (entryKey.asInt() == index) {
                    return entryValue;
                }
            }
        }

        if (index < mapEntries.size()) {
            return (ArrayNode) mapEntries.get(index).get(1);
        } else {
            return null;
        }
    }

    static class Context {
        private Deque<Map<String, ArrayNode>> stackVars = new ArrayDeque<Map<String, ArrayNode>>();

        public Context() {
            push();
        }

        public void push() {
            stackVars.push(new HashMap<String, ArrayNode>());
        }

        public void pop() {
            stackVars.pollFirst();
        }

        public void putVar(String varName, ArrayNode value) {
            stackVars.getFirst().put(varName, value);
        }

        public void removeVar(String varName) {
            for (Map<String, ArrayNode> frame : stackVars) {
                if (frame.containsKey(varName)) {
                    frame.remove(varName);
                }
            }
        }

        public ArrayNode getVarValue(String varName) {
            for (Map<String, ArrayNode> frame : stackVars) {
                if (frame.containsKey(varName)) {
                    return frame.get(varName);
                }
            }
            return null;
        }

        public boolean hasVar(String varName) {
            for (Map<String, ArrayNode> frame : stackVars) {
                if (frame.containsKey(varName)) {
                    return true;
                }
            }
            return false;
        }
    }
}

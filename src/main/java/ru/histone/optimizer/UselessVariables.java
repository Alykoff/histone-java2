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

import java.util.HashSet;
import java.util.Set;

/**
 * This optimization unit removes useless variables from AST; The criteria is extremely simple: variable is useless if there
 * is no selectors AT ALL, using it.
 * <p/>
 * User: sazonovkirill@gmail.com
 * Date: 09.01.13
 */
public class UselessVariables extends AbstractASTWalker {
    /**
     * @see {@link Mode}
     */
    private Mode mode;
    private Set<String> selectors;

    public UselessVariables(NodeFactory nodeFactory) {
        super(nodeFactory);
    }

    public ArrayNode removeUselessVariables(ArrayNode ast) throws HistoneException {
        selectors = new HashSet<String>();
        mode = Mode.COLLECT_SELECTORS;
        process(ast);
        mode = Mode.REMOVE_VARIABLES;
        return process(ast);
    }

    @Override
    protected JsonNode processSelector(ArrayNode ast) throws HistoneException {
        JsonNode fullVariable = ast.get(1);

        JsonNode firstToken = fullVariable.get(0);
        if (firstToken.isTextual()) {
            String varName = firstToken.asText();
            if (mode == Mode.COLLECT_SELECTORS) {
                selectors.add(varName);
            }
        } else {
            processAstNode(firstToken);
        }

        for (int i = 1; i < fullVariable.size(); i++) {
            JsonNode t = fullVariable.get(i);
            if (t.isTextual()) {
                // nothing
            } else if (t.isArray() && t.size() == 2 && t.get(0).isInt() && t.get(0).asInt() == AstNodeType.STRING) {
                // nothing
            } else if (t.isArray() && t.size() == 2 && t.get(0).isInt() && t.get(0).asInt() == AstNodeType.INT) {
                // nothing
            } else {
                processAstNode(t);
            }
        }

        return ast;
    }

    @Override
    protected JsonNode processVariable(ArrayNode ast) throws HistoneException {
        Assert.isTrue(ast.size() == 3);

        JsonNode var = ast.get(1);
        JsonNode value = ast.get(2);
        JsonNode processedValue = processAstNode(value);

        String varName = var.asText();
        if (mode == Mode.REMOVE_VARIABLES && !selectors.contains(varName)) {
            return nodeFactory.jsonString("");
        } else {
            return nodeFactory.jsonArray(AstNodeType.VAR, var, processedValue);
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

    /**
     * Optimization unit traverses AST twice: first time for collecting selectors, second - for removing useless variables.
     */
    enum Mode {
        /**
         * Collecting selectors
         */
        COLLECT_SELECTORS,

        /**
         * Removing useless variables
         */
        REMOVE_VARIABLES
    }
}

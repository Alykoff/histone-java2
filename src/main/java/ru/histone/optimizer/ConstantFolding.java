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
import ru.histone.evaluator.Evaluator;
import ru.histone.evaluator.EvaluatorException;
import ru.histone.evaluator.nodes.Node;
import ru.histone.evaluator.nodes.NodeFactory;
import ru.histone.utils.Assert;
import ru.histone.v2.exceptions.HistoneException;

import java.util.Arrays;

/**
 * This optimization unit folds expressions, that use only unary/binary/ternary operations (e.g. operations over arguments) and constants.
 * <p/>
 * User: sazonovkirill@gmail.com
 * Date: 25.12.12
 */
public class ConstantFolding extends AbstractASTWalker {
    private final Evaluator evaluator;

    public ConstantFolding(NodeFactory nodeFactory, Evaluator evaluator) {
        super(nodeFactory);
        this.evaluator = evaluator;
    }

    public ArrayNode foldConstants(ArrayNode ast) throws HistoneException {
        return process(ast);
    }

    @Override
    public void pushContext() {
        // There is no context to push in this optimizer
    }

    @Override
    public void popContext() {
        // There is no context to push in this optimizer
    }

    protected JsonNode processOperationOverArguments(ArrayNode ast) throws HistoneException {
        Assert.notNull(ast);
        Assert.notNull(ast.size() > 1);
        Assert.isTrue(ast.get(0).isNumber());
        for (int i = 1; i < ast.size(); i++) Assert.isTrue(ast.get(i).isArray());
        Assert.isTrue(getOperationsOverArguments().contains(ast.get(0).asInt()));

        int operationType = ast.get(0).asInt();

        ArrayNode[] processedArguments = new ArrayNode[ast.size() - 1];
        for (int i = 1; i < ast.size(); i++) {
            JsonNode processedArg = processAstNode(ast.get(i));
            Assert.isTrue(processedArg.isArray());
            processedArguments[i - 1] = (ArrayNode) processedArg;
        }

        if (isConstants(Arrays.asList(processedArguments))) {
            Node node = evaluate(operationType, processedArguments);
            return node2Ast(node);
        } else {
            return nodeFactory.jsonArray(operationType, processedArguments);
        }
    }

    private Node evaluate(int operationType, ArrayNode... arguments) throws EvaluatorException {
        return evaluator.evaluate(nodeFactory.jsonArray(operationType, arguments));
    }
}

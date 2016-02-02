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
import com.fasterxml.jackson.databind.node.IntNode;
import ru.histone.HistoneException;
import ru.histone.evaluator.Evaluator;
import ru.histone.evaluator.nodes.Node;
import ru.histone.evaluator.nodes.NodeFactory;
import ru.histone.parser.AstNodeType;
import ru.histone.utils.Assert;
import ru.histone.utils.StringUtils;

import java.util.*;

/**
 * This optimizations unit marks 'safe' AST nodes; 'safe' here means, that AST node is constant, doesn't depend on
 * context variables and doesn't contain any function calls. In this case we consider it to don't have side effects and can be
 * evaluated only once and can be treated as string constant later.
 *
 * @author sazonovkirill@gmail.com
 * @author peter@salnikov.cc
 * @see {@link SafeASTEvaluationOptimizer} evaluates safe AST nodes
 */
public class SafeASTNodesMarker extends AbstractASTWalker {
    private final Evaluator evaluator;
    private Context context = new Context();

    public SafeASTNodesMarker(NodeFactory nodeFactory, Evaluator evaluator) {
        super(nodeFactory);
        this.evaluator = evaluator;
    }

    public static boolean safeArray(ArrayNode array) {
        boolean isSafe = true;
        for (JsonNode node : array) {
            if (!safeAstNode(node)) {
                return false;
            }
        }
        return isSafe;
    }

    public static boolean safeArray(JsonNode[] array) {
        boolean isSafe = true;
        for (JsonNode node : array) {
            if (!safeAstNode(node)) {
                return false;
            }
        }
        return isSafe;
    }

    public static boolean safeAstNode(JsonNode node) {
        return (node.isArray() && getNodeType((ArrayNode) node) > 0) || node.isTextual();
    }

    public static boolean unsafeAstNode(JsonNode node) {
        return node.isArray() && getNodeType((ArrayNode) node) < 0;
    }

    /**
     * Statements are safe, if all of them are safe.
     */
    @Override
    protected JsonNode processStatements(ArrayNode ast) throws HistoneException {
        ast = (ArrayNode) ast.get(1);

        JsonNode[] statementsOut = new JsonNode[ast.size()];
        for (int i = 0; i < ast.size(); i++) {
            statementsOut[i] = processAstNode(ast.get(i));
        }
        boolean isSafe = safeArray(statementsOut);

        return ast(isSafe, AstNodeType.STATEMENTS, nodeFactory.jsonArray(statementsOut));
    }

    /**
     * Var is safe, if its expression is safe.
     */
    @Override
    protected JsonNode processVariable(ArrayNode ast) throws HistoneException {
        Assert.isTrue(ast.size() == 3);

        JsonNode varName = ast.get(1);
        JsonNode varExpression = ast.get(2);
        JsonNode processedVarExpression = processAstNode(varExpression);

        boolean isSafe = safeAstNode(processedVarExpression);
        if (isSafe) {
            context.addSafeVar(varName.asText());
        }

        return ast(isSafe, AstNodeType.VAR, varName, processedVarExpression);
    }

    /**
     * Map is safe if all its items are safe.
     */
    @Override
    protected JsonNode processMap(ArrayNode ast) throws HistoneException {
        ArrayNode items = (ArrayNode) ast.get(1);

        ArrayNode processedItems = nodeFactory.jsonArray();
        for (JsonNode item : items) {
            if (item.isArray()) {
                ArrayNode arr = (ArrayNode) item;
                JsonNode key = arr.get(0);
                JsonNode value = arr.get(1);

                value = processAstNode(value);
                processedItems.add(nodeFactory.jsonArray(key, value));
            }
        }

        boolean isSafe = safeMapItems(processedItems);
        return ast(isSafe, AstNodeType.MAP, processedItems);
    }

    /**
     * Macros is safe if it statements are safe.
     */
    @Override
    protected JsonNode processMacro(ArrayNode macro) throws HistoneException {
        JsonNode name = macro.get(1);
        ArrayNode args = (ArrayNode) macro.get(2);
        ArrayNode statements = (ArrayNode) macro.get(3);
        pushContext();

        // Adding macro arguments and 'self' keyword as safe variables.
        context.addSafeVar(KEYWORD_SELF);
        Set<String> argNames = new HashSet<String>();
        if (!args.isNull()) {
            for (JsonNode arg : args) {
                argNames.add(arg.asText());
                context.addSafeVar(arg.asText());
            }
        }

        args = (ArrayNode) processAstNode(args);
        ArrayNode statementsOut = (ArrayNode) processArrayOfAstNodes(statements);
        popContext();

        boolean isSafe = safeArray(statementsOut);
        if (isSafe) {
            context.addSafeMacro(name.asText());
        }
        context.addMacro(name.asText());
        return ast(isSafe, AstNodeType.MACRO, name, args, statementsOut);
    }

    /**
     * Imports are always not safe
     */
    @Override
    protected JsonNode processImport(ArrayNode ast) throws HistoneException {
        String resource = ast.get(1).asText();

        return ast(false, AstNodeType.IMPORT, nodeFactory.jsonString(resource));
    }

    /**
     * CALL is safe if:
     * 1. It's macro call, not function of type call (so target block is null).
     * 2. Macro name is string (what else can it bee?
     * 3. Macros is safe in context.
     * 4. All args are safe.
     */
    @Override
    protected JsonNode processCall(ArrayNode ast) throws HistoneException {
        JsonNode target = ast.get(1);
        JsonNode name = ast.get(2);
        JsonNode args = ast.get(3);

        if (!target.isNull()) {
            boolean targetIsSafe = safeAstNode(target);
            if (!targetIsSafe) {
                return ast(false, AstNodeType.CALL, target, name, args);
            }

            Node node = evaluator.evaluate(target);
            if (evaluator.hasFunction(node.getClass(), name.asText())) {
                boolean isFunctionSafe = evaluator.isFunctionSave(node.getClass(), name.asText());
                return ast(isFunctionSafe, AstNodeType.CALL, target, name, args);
            } else {
                return ast(false, AstNodeType.CALL, target, name, args);
            }
        } else {
            String macrosName = name.asText();
            boolean isSafe = context.isMacroSafe(macrosName);

            if (args.isArray()) {
                args = processArrayOfAstNodes(args);
                isSafe = isSafe && safeArray((ArrayNode) args);
            }

            return ast(isSafe, AstNodeType.CALL, target, name, args);
        }

//        if (!target.isNull() || !name.isTextual() || StringUtils.isBlank(name.asText())) {
//            return ast(false, AstType.CALL, target, name, args);
//        }

  /*      int targetType = target.get(0);
        if(targetType == AstType.INT){
            targetCla
        }*/


    }

    /**
     * Selector is safe if variable is safe or all children elements are safe.
     */
    @Override
    protected JsonNode processSelector(ArrayNode ast) throws HistoneException {
        JsonNode fullVariable = ast.get(1);

        List<String> ss = new ArrayList<String>();
        for (JsonNode t : fullVariable) {
            if (t.isTextual()) {
                ss.add(t.asText());
            } else if (t.isArray() && t.size() == 2 && t.get(0).isInt() && t.get(0).asInt() == AstNodeType.STRING) {
                ss.add(t.get(1).asText());
            } else if (t.isArray() && t.size() == 2 && t.get(0).isInt() && t.get(0).asInt() == AstNodeType.INT) {
                ss.add(String.valueOf(t.get(1).asInt()));
            } else {
                return ast(false, AstNodeType.SELECTOR, fullVariable);
            }
        }
        String fullVariableName = StringUtils.join(ss, ".");
        boolean isSafe = context.isVarSafe(fullVariableName);

        return ast(isSafe, AstNodeType.SELECTOR, fullVariable);
    }

    /**
     * FOR is safe if all its statements are safe and collection, for iterarated over, is safe also.
     */
    @Override
    protected JsonNode processFor(ArrayNode ast) throws HistoneException {
        ArrayNode var = (ArrayNode) ast.get(1);
        ArrayNode collection = (ArrayNode) ast.get(2);
        ArrayNode statements = (ArrayNode) ast.get(3).get(0);

        ArrayNode elseStatements = null;
        if (ast.get(3).size() > 1) {
            elseStatements = (ArrayNode) ast.get(3).get(1);
        }

        boolean isSafe = true;

        String iterVal = var.get(0).asText();
        String iterKey = (var.size() > 1) ? var.get(1).asText() : null;

        collection = (ArrayNode) processAstNode(collection);
        isSafe = isSafe && safeAstNode(collection);

        pushContext();
        context.addSafeVar(iterVal);
        if (iterKey != null) {
            context.addSafeVar(iterKey);
        }
        context.addSafeVar("self.index");
        context.addSafeVar("self.last");
        JsonNode[] statementsOut = new JsonNode[statements.size()];
        for (int i = 0; i < statements.size(); i++) {
            statementsOut[i] = processAstNode(statements.get(i));
        }
        isSafe = isSafe && safeArray(statementsOut);
        popContext();

        JsonNode[] elseStatementsOut = null;
        if (elseStatements != null) {
            elseStatementsOut = new JsonNode[elseStatements.size()];
            for (int i = 0; i < elseStatements.size(); i++) {
                elseStatementsOut[i] = processAstNode(elseStatements.get(i));
            }
            isSafe = isSafe && safeArray(elseStatementsOut);
        }

        ArrayNode statementsContainer = elseStatementsOut == null ?
                nodeFactory.jsonArray(nodeFactory.jsonArray(statementsOut)) :
                nodeFactory.jsonArray(nodeFactory.jsonArray(statementsOut), nodeFactory.jsonArray(elseStatementsOut));

        // EXPERIMENTAL fix
//        return ast(false, AstType.FOR, var, collection, statementsContainer);

        return ast(isSafe, AstNodeType.FOR, var, collection, statementsContainer);
    }

    /**
     * If is safe if for all conditions both expression and statements are safe.
     */
    @Override
    protected JsonNode processIf(ArrayNode ast) throws HistoneException {
        ArrayNode conditions = (ArrayNode) ast.get(1);

        boolean isSafe = true;
        ArrayNode conditionsOut = nodeFactory.jsonArray();
        for (JsonNode condition : conditions) {
            JsonNode expression = condition.get(0);
            JsonNode statements = condition.get(1);

            expression = processAstNode(expression);
            isSafe = isSafe && safeAstNode(expression);

            pushContext();
            JsonNode[] statementsOut = new JsonNode[statements.size()];
            for (int i = 0; i < statements.size(); i++) {
                statementsOut[i] = processAstNode(statements.get(i));
                isSafe = isSafe && safeAstNode(statementsOut[i]);
            }
            popContext();

            ArrayNode conditionOut = nodeFactory.jsonArray();
            conditionOut.add(expression);
            conditionOut.add(nodeFactory.jsonArray(statementsOut));
            conditionsOut.add(conditionOut);
        }

        return ast(isSafe, AstNodeType.IF, conditionsOut);
    }

    @Override
    protected JsonNode processOperationOverArguments(ArrayNode ast) throws HistoneException {
        Assert.notNull(ast);
        Assert.notNull(ast.size() > 1);
        Assert.isTrue(ast.get(0).isNumber());
        for (int i = 1; i < ast.size(); i++) Assert.isTrue(ast.get(i).isArray());
        Assert.isTrue(getOperationsOverArguments().contains(ast.get(0).asInt()));

        int operationType = ast.get(0).asInt();
        boolean isSafe = true;
        final List<ArrayNode> processedArguments = new ArrayList<ArrayNode>();
        for (int i = 1; i < ast.size(); i++) {
            JsonNode processedArg = processAstNode(ast.get(i));
            isSafe = isSafe && safeAstNode(processedArg);
            Assert.isTrue(processedArg.isArray());
            processedArguments.add((ArrayNode) processedArg);
        }

        return ast(isSafe, operationType, processedArguments);
    }

    @Override
    public void pushContext() {
        context.push();
    }

    @Override
    public void popContext() {
        context.pop();
    }

    private boolean safeMapItems(ArrayNode mapEntries) {
        boolean isSafe = true;
        for (JsonNode mapEntry : mapEntries) {
            JsonNode entryKey = mapEntry.get(0);
            JsonNode entryValue = mapEntry.get(1);

            if (!entryValue.isNull()) {
                if (!safeAstNode(entryValue)) {
                    return false;
                }
            }
        }
        return isSafe;
    }

    protected ArrayNode ast(boolean isSafe, int operationType, JsonNode... arguments) {
        return nodeFactory.jsonArray(isSafe ? operationType : -operationType, arguments);
    }

    protected ArrayNode ast(boolean isSafe, int operationType, Collection<? extends ArrayNode> arguments) {
        ArrayNode array = nodeFactory.jsonArray(IntNode.valueOf(isSafe ? operationType : -operationType));
        for (ArrayNode argument : arguments) {
            array.add(argument);
        }
        return array;
    }

    class Context {
        private Deque<Set<String>> declaredVariables;
        private Deque<Set<String>> safeMacroses;
        private Deque<Set<String>> allMacroses;

        public Context() {
            this.declaredVariables = new ArrayDeque<Set<String>>();
            this.safeMacroses = new ArrayDeque<Set<String>>();
            this.allMacroses = new ArrayDeque<Set<String>>();
            push();
        }

        public void push() {
            declaredVariables.push(new HashSet<String>());
            safeMacroses.push(new HashSet<String>());
            allMacroses.push(new HashSet<String>());
        }

        public void pop() {
            declaredVariables.pollFirst();
            safeMacroses.pollFirst();
            allMacroses.pollFirst();
        }

        public boolean isVarSafe(String name) {
            return declaredVariables.getFirst().contains(name);
        }

        public void addSafeVar(String name) {
            declaredVariables.getFirst().add(name);
        }

        public boolean hasMacro(String name) {
            return allMacroses.getFirst().contains(name);
        }

        public boolean isMacroSafe(String name) {
            return safeMacroses.getFirst().contains(name);
        }

        public void addMacro(String name) {
            allMacroses.getFirst().add(name);
        }

        public void addSafeMacro(String name) {
            safeMacroses.getFirst().add(name);
        }

    }

}

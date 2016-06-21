/*
 * Copyright (c) 2016 MegaFon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.histone.v2.parser;

import ru.histone.v2.parser.node.*;
import ru.histone.v2.utils.Tuple;

import java.util.*;

/**
 * @author Alexey Nevinsky
 */
public class SsaOptimizer {

    private Deque<Scope> scopes = new ArrayDeque<>();
    private boolean isMacroScope = false;

    public void process(AstNode root) {
        enter();
        processNode(root);
        leave();
    }

    private void processNode(AstNode node) {
        if (node.hasValue()) {
            return;
        }

        ExpAstNode currentNode = (ExpAstNode) node;

        switch (node.getType()) {
            case AST_VAR: {
                processNode(currentNode.getNode(0));
                ValueNode n = currentNode.getNode(1);
                if (n instanceof StringAstNode) {
                    Long id = getVarName(((StringAstNode) currentNode.getNode(1)).getValue());
                    currentNode.setNode(1, new LongAstNode(id));
                } else {
                    Long id = getVarName(n.getValue() + "");
                    currentNode.setNode(1, new LongAstNode(id));
                }
            }
            break;
            case AST_REF: {
                ValueNode n = currentNode.getNode(1);
                if (n instanceof StringAstNode) {
                    Tuple<Long, Long> refId = getRefPair(((StringAstNode) currentNode.getNode(1)).getValue());
                    currentNode.setNode(1, new LongAstNode(refId.getRight()));
                } else {
                    Tuple<Long, Long> refId = getRefPair(n.getValue() + "");
                    if (refId != null) {
                        currentNode.setNode(1, new LongAstNode(refId.getRight()));
                    }
                }
            }
            break;
            default: {
                processNodes(currentNode.getNodes());
            }
        }
    }

    private void processNodes(List<AstNode> nodes) {
        for (AstNode node : nodes) {
            processNode(node);
        }
    }

    public void enter() {
        scopes.addFirst(new Scope());
    }

    public void leave() {
        scopes.pop();
    }


    public Long getVarName(String varName) {
        Scope scope = scopes.peek();

        Var var = scope.vars.get(varName);
        if (var == null) {
            var = new Var();
            var.addName(scope.counter++);
            scope.vars.put(varName, var);
        } else if (var.used) {
            var.addName(scope.counter++);
        }

        return var.lastName();
    }

    public Tuple<Long, Long> getRefPair(final String name) {
        Iterator<Scope> iterator = scopes.iterator();
        long i = 0;
        while (iterator.hasNext()) {
            Scope scope = iterator.next();
            Var var = scope.vars.get(name);
            if (var != null) {
                var.used = true;
                return new Tuple<>(i, var.lastName());
            }
            i++;
        }
        return null;
    }

    public static class Scope {
        private long counter = 0;
        private Map<String, Var> vars = new HashMap<>();
    }

    public static class Var {
        private boolean used = false;
        private List<Long> names = new ArrayList<>();

        void addName(Long name) {
            names.add(name);
        }

        Long lastName() {
            return names.get(names.size() - 1);
        }
    }
}

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

import ru.histone.v2.Constants;
import ru.histone.v2.parser.node.*;
import ru.histone.v2.utils.Tuple;

import java.util.*;

/**
 * @author Alexey Nevinsky
 */
public class SsaOptimizer {

    private Deque<Scope> scopes = new ArrayDeque<>();

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
            case AST_NODES: {
                enter();
                processNodes(currentNode.getNodes());
                leave();
            }
            break;
            case AST_WHILE: {
                if (currentNode.size() > 1) {
                    processNode(currentNode.getNode(1));
                }

                enter();
                getVarName(Constants.SELF_NAME);
                processNode(currentNode.getNode(0)); //bcz we don't need to process condition node in current scope
                leave();
            }
            break;
            case AST_REF: {
                ValueNode n = currentNode.getNode(1);
                LongAstNode scopeDiff = currentNode.getNode(0);
                if (n instanceof StringAstNode) {
                    Tuple<Long, Long> refId = getRefPair(scopeDiff.getValue(), ((StringAstNode) currentNode.getNode(1)).getValue());
                    currentNode.setNode(1, new LongAstNode(refId.getRight()));
                } else {
                    Tuple<Long, Long> refId = getRefPair(scopeDiff.getValue(), n.getValue() + "");
                    if (refId != null) {
                        currentNode.setNode(1, new LongAstNode(refId.getRight()));
                    }
                }
            }
            break;
            case AST_FOR: {
                enter();
                getVarName(Constants.SELF_NAME);
                if (((ValueNode) currentNode.getNode(0)).getValue() != null) {
                    getVarName(((ValueNode) currentNode.getNode(0)).getValue() + "");
                }
                if (((ValueNode) currentNode.getNode(1)).getValue() != null) {
                    getVarName(((ValueNode) currentNode.getNode(1)).getValue() + "");
                }
                processNode(currentNode.getNode(2));
                leave();

                int i = 3;

                AstNode n;
                while ((n = currentNode.getNode(i)) != null) {
                    if (i % 2 == 0) {
                        enter();
                        processNode(n);
                        leave();
                    } else {
                        processNode(n);
                    }
                    i++;
                }
            }
            break;
            case AST_MACRO: {
                enter();
                getVarName(Constants.SELF_NAME);
                if (currentNode.size() > 2) {
                    LongAstNode argsSize = currentNode.getNode(2);
                    for (long i = 0; i < argsSize.getValue(); i++) {
                        getVarName((i + 1) + "");
                    }
                }

                processNode(currentNode.getNode(1));
                leave();
            }
            break;
            case AST_IF: {
                int i = 0;

                AstNode n;
                while ((n = currentNode.getNode(i)) != null) {
                    if (i % 2 == 0) {
                        enter();
                        processNode(n);
                        leave();
                    } else {
                        processNode(n);
                        int k = 0;
                    }
                    i++;
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

    private void enter() {
        scopes.addFirst(new Scope());
    }

    private void leave() {
        scopes.pop();
    }


    private Long getVarName(String varName) {
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

    private Tuple<Long, Long> getRefPair(long scopeDiff, final String name) {
        Iterator<Scope> iterator = scopes.iterator();
        long i = 0;
        long diff = 0;
        while (iterator.hasNext()) {
            //we search scope by diff
            if (diff != scopeDiff) {
                diff++;
                iterator.next();
                continue;
            }
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

    private static class Scope {
        private long counter = 0;
        private Map<String, Var> vars = new HashMap<>();
    }

    private static class Var {
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

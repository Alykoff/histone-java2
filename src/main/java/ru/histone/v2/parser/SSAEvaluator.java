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

import java.util.*;

/**
 * @author alexey.nevinsky
 */
public class SSAEvaluator {
    private Deque<Map<String, Var>> scopes = new ArrayDeque<>();
    private long nameCounter;
    private boolean isMacroScope;

    public SSAEvaluator() {
        scopes.add(new HashMap<>());
    }

    public void processTree(AstNode rawNode) {
        if (rawNode.hasValue()) {
            return;
        }

        ExpAstNode node = (ExpAstNode) rawNode;
        switch (rawNode.getType()) {
            case AST_VAR: {
                processTree(node.getNode(0));
                node.setNode(1, new StringAstNode(getVarName(node.getNode(1))));
            }
            break;

            case AST_REF: {
                node.setNode(0, new StringAstNode(getRefName(((StringAstNode) node.getNode(0)).getValue(), isMacroScope)));
            }
            break;

            case AST_MACRO: {
                isMacroScope = true;
                scopes.addFirst(new HashMap<>());
                for (int i = 2; i < node.size(); i++) {
                    ExpAstNode n = node.getNode(i);
                    n.setNode(0, new StringAstNode(getVarName(n.getNode(0))));
                }
                processTree(node.getNode(0));
                scopes.pop();
                isMacroScope = false;
            }
            break;

            case AST_FOR: {
                // process main block
                scopes.addFirst(new HashMap<>());
                if (node.getNode(0) != null) {
                    node.setNode(0, new StringAstNode(getVarName(node.getNode(0))));
                }
                if (node.getNode(1) != null) {
                    node.setNode(1, new StringAstNode(getVarName(node.getNode(1))));
                }
                processTree(node.getNode(2));
                scopes.pop();
                // process collection
                processTree(node.getNode(3));
                // process else blocks
                for (int i = 4; i < node.size(); i += 2) {
                    if (node.getNode(i + 1) != null) {
                        processTree(node.getNode(i + 1));
                    }
                    scopes.addFirst(new HashMap<>());
                    processTree(node.getNode(i));
                    scopes.pop();
                }
            }
            break;

            case AST_IF: {
                for (int i = 0; i < node.size(); i += 2) {
                    if (node.getNode(i + 1) != null) {
                        processTree(node.getNode(i));
                    }
                    scopes.addFirst(new HashMap<>());
                    processTree(node.getNode(i));
                    scopes.pop();
                }
            }
            break;

            case AST_NODES: {
                scopes.addFirst(new HashMap<>());
                node.getNodes().forEach(this::processTree);
                scopes.pop();
            }
            break;

            default: {
                if (node.getType() == AstType.AST_NODELIST) {
                    node.getNodes().forEach(this::processTree);
                }
            }
        }
    }

    private String getVarName(ValueNode node) {
        Map<String, Var> scope = scopes.peek();

        String varName = String.valueOf(node.getValue());
        Var var = scope.get(varName);
        if (var == null) {
            var = new Var(varName);
            scope.put(varName, var);
        } else if (var.used) {
            var.addName(++nameCounter + "$SSA");
        }

        return var.lastName();
    }


    private String getRefName(final String name, boolean isMacroScope) {
        for (Map<String, Var> scope : scopes) {
            Var var = scope.get(name);
            if (var != null) {
                if (isMacroScope) {
                    var.used = true;
                }
                return var.lastName();
            }
        }
        return name;
    }


    private static class Var {
        boolean used = false;
        List<String> names = new ArrayList<>();

        public Var(String name) {
            names.add(name);
        }

        public void addName(String name) {
            names.add(name);
        }

        public String lastName() {
            return names.get(names.size() - 1);
        }
    }
}
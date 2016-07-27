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

package ru.histone.v2.java_compiler.bcompiler;

import com.squareup.javapoet.MethodSpec;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.NotImplementedException;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.exceptions.HistoneException;
import ru.histone.v2.parser.node.AstNode;
import ru.histone.v2.parser.node.AstType;
import ru.histone.v2.parser.node.ExpAstNode;
import ru.histone.v2.parser.node.ValueNode;

import java.util.List;

/**
 * @author Alexey Nevinsky
 */
public class TemplateProcessor {
    public MethodSpec.Builder process(MethodSpec.Builder builder, AstNode root) {
        return processNode(builder, root);
    }

    public MethodSpec.Builder processNode(MethodSpec.Builder builder, AstNode node) {
        if (node == null) {
            return builder;
        }

        if (node.hasValue()) {
            ValueNode valueNode = (ValueNode) node;
            Object v = valueNode.getValue();
            if (v instanceof String) {
                return builder.addCode("$T.getValue(\"" + v + "\")", EvalUtils.class);
            }
            return builder.addCode("$T.getValue(" + v + ")", EvalUtils.class);
        }

        ExpAstNode expNode = (ExpAstNode) node;
        switch (node.getType()) {
//            case AST_ARRAY:
//                return processArrayNode(expNode, context);
//            case AST_REGEXP:
//                return processRegExp(expNode);
//            case AST_THIS:
//                return processThisNode(context);
//            case AST_GLOBAL:
//                return processGlobalNode();
//            case AST_NOT:
//                return processNotNode(expNode, context);
//            case AST_AND:
//                return processAndNode(expNode, context);
//            case AST_OR:
//                return processOrNode(expNode, context);
//            case AST_TERNARY:
//                return processTernary(expNode, context);
//            case AST_ADD:
//                return processAddNode(expNode, context);
            case AST_SUB:
            case AST_MUL:
            case AST_DIV:
            case AST_MOD:
                return processArithmetical(builder, expNode);
//            case AST_USUB:
//                return processUnaryMinus(expNode, context);
//            case AST_LT:
//            case AST_GT:
//            case AST_LE:
//            case AST_GE:
//                return processRelation(expNode, context, STRING_EVAL_NODE_LEN_COMPARATOR);
//            case AST_EQ:
//            case AST_NEQ:
//                return processRelation(expNode, context, STRING_EVAL_NODE_STRONG_COMPARATOR);
//            case AST_REF:
//                return processReferenceNode(expNode, context);
//            case AST_CALL:
//                return processCall(expNode, context);
//            case AST_VAR:
//                return processVarNode(expNode, context);
//            case AST_IF:
//                return processIfNode(expNode, context);
//            case AST_FOR:
//                return processForNode(expNode, context);
//            case AST_WHILE:
//                return processWhileNode(expNode, context);
//            case AST_MACRO:
//                return processMacroNode(expNode, context);
//            case AST_RETURN:
//                return processReturnNode(expNode, context);
            case AST_NODES:
                return processNodeList(builder, expNode, true);
            case AST_NODELIST:
                return processNodeList(builder, expNode, false);
//            case AST_BOR:
//                return processBorNode(expNode, context);
//            case AST_BXOR:
//                return processBxorNode(expNode, context);
//            case AST_BAND:
//                return processBandNode(expNode, context);
//            case AST_SUPPRESS:
//                return processSuppressNode(expNode, context);
//            case AST_CONTINUE:
//                return processBreakContinueNode(expNode, false);
//            case AST_BREAK:
//                return processBreakContinueNode(expNode, true);
        }
        throw new HistoneException("Unknown AST Histone Type: " + node.getType());
    }

    private MethodSpec.Builder processArithmetical(MethodSpec.Builder builder, ExpAstNode node) {
        if (CollectionUtils.isNotEmpty(node.getNodes()) && node.getNodes().size() == 2) {
            builder = builder.addCode("StdLibrary." + getArithmeticalMethod(node) + "(");
            builder = processNode(builder, node.getNodes().get(0));
            builder = builder.addCode(",");
            builder = processNode(builder, node.getNodes().get(1));
            builder.addCode(")");
            return builder;
//
//            AstNode left = node.getNodes().get(0);
//            AstNode right = node.getNodes().get(1);
//
//
//            return processNode(builder, left)
//
//
//            return evaluateNode(node.getNodes().get(0), context).thenCompose(leftNode ->
//            {
//                if (isNumberNode(leftNode) || leftNode.getType() == HistoneType.T_STRING) {
//                    Double lValue = getValue(leftNode).orElse(null);
//                    if (lValue != null) {
//                        return evaluateNode(node.getNodes().get(1), context).thenCompose(rightNode -> {
//                            if (isNumberNode(rightNode) || rightNode.getType() == HistoneType.T_STRING) {
//                                return CompletableFuture.completedFuture(getValue(rightNode).orElse(null));
//                            }
//                            return CompletableFuture.completedFuture(null);
//                        }).thenCompose(rValue -> {
//                            if (rValue != null) {
//                                Double res = null;
//                                AstType type = node.getType();
//                                switch (type) {
//                                    case AST_SUB:
//                                        res = lValue - rValue;
//                                        break;
//                                    case AST_MUL:
//                                        res = lValue * rValue;
//                                        break;
//                                    case AST_DIV:
//                                        res = lValue / rValue;
//                                        break;
//                                    case AST_MOD:
//                                        res = lValue % rValue;
//                                        break;
//                                }
//                                return EvalUtils.getNumberFuture(res);
//                            }
//                            return EvalUtils.getValue(null);
//                        });
//                    }
//                }
//                return EvalUtils.getValue(null);
//            });
        }
        return builder;
    }

    private String getArithmeticalMethod(ExpAstNode node) {
        AstType type = node.getType();
        switch (type) {
            case AST_ADD:
                return "add";
            case AST_SUB:
                return "sub";
            case AST_MUL:
                return "mul";
            case AST_DIV:
                return "div";
            default:
                return "mod";
        }
    }

    private MethodSpec.Builder processNodeList(MethodSpec.Builder builder, ExpAstNode expNode, boolean createContext) {
        if (createContext) {
            //todo check this
            builder = builder.beginControlFlow("");
        }

        if (checkReturnNode(expNode.getNodes())) {
            throw new NotImplementedException("Oops! to do it");
        } else {
            builder = builder.addCode("CompletableFuture<StringBuilder> csb = CompletableFuture.completedFuture(new StringBuilder());\n");
            for (AstNode node : expNode.getNodes()) {
                builder = builder.addCode("csb = StdLibrary.append(ctx, csb, ");
                builder = processNode(builder, node);
                builder = builder.addCode(");\n");
            }
            builder.addStatement("return $T.asString(ctx, csb)", StdLibrary.class);
        }

        if (createContext) {
            builder = builder.endControlFlow();
        }
        return builder;
    }

    private boolean checkReturnNode(List<AstNode> nodes) {
        return nodes.stream().filter(n -> n.getType() == AstType.AST_RETURN).findFirst().isPresent();
    }
}

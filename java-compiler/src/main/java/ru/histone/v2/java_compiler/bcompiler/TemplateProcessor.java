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
import org.apache.commons.lang3.ObjectUtils;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.parser.node.*;

import java.util.Collections;
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
            if (v == null) {
                return builder.addCode("$T.getValue($T.NULL)", EvalUtils.class, ObjectUtils.class);
            }

            if (v instanceof String) {
                return builder.addCode("$T.getValue(\"" + v + "\")", EvalUtils.class);
            }
            return builder.addCode("$T.getValue(" + v + ")", EvalUtils.class);
        }

        ExpAstNode expNode = (ExpAstNode) node;
        switch (node.getType()) {
            case AST_ARRAY:
                return processArrayNode(builder, expNode);
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
            case AST_ADD:
            case AST_SUB:
            case AST_MUL:
            case AST_DIV:
            case AST_MOD:
            case AST_USUB:
                return processArithmetical(builder, expNode);
            case AST_LT:
            case AST_GT:
            case AST_LE:
            case AST_GE:
            case AST_EQ:
            case AST_NEQ:
                return processRelation(builder, expNode);
//            case AST_REF:
//                return processReferenceNode(expNode, context);
            case AST_CALL:
                return processCall(builder, expNode);
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
        return builder;
//        throw new HistoneException("Unknown AST Histone Type: " + node.getType());
    }

    private MethodSpec.Builder processRelation(MethodSpec.Builder builder, ExpAstNode node) {
        builder = builder.addCode("StdLibrary." + getRelationMethod(node) + "(ctx, ");
        builder = processNode(builder, node.getNodes().get(0));
        builder = builder.addCode(",");
        builder = processNode(builder, node.getNodes().get(1));
        builder.addCode(")");
        return builder;
    }

    private MethodSpec.Builder processArrayNode(MethodSpec.Builder builder, ExpAstNode node) {
        if (CollectionUtils.isEmpty(node.getNodes())) {
            return builder.addCode("StdLibrary.array()");
        }
        if (node.getNode(0).getType() == AstType.AST_VAR) {
            throw new RuntimeException("0_J do it");
//            return evalAllNodesOfCurrent(node, context).thenApply(evalNodes -> EvalUtils.createEvalNode(null));
        }

        builder.addCode("StdLibrary.array(");
        for (int i = 0; i < node.size(); i++) {
            process(builder, node.getNode(i));
            if (i != node.size() - 1) {
                builder.addCode(",");
            }
        }
        builder.addCode(")");
        return builder;
    }

    private MethodSpec.Builder processCall(MethodSpec.Builder builder, ExpAstNode expNode) {
        CallExpAstNode callNode = (CallExpAstNode) expNode;
        if (callNode.getCallType() == CallType.SIMPLE) {
            builder.addCode("StdLibrary.simpleCall(ctx,");
            //todo
            if (expNode.size() == 2) {
                builder.addCode("\"" + ((ValueNode) expNode.getNode(1)).getValue() + "\", $T.emptyList()", Collections.class);
            }
            builder.addCode(")");
        } else if (callNode.getCallType() == CallType.RTTI_M_GET) {
            builder.addCode("StdLibrary.mGet(ctx, ");
            for (int i = 0; i < expNode.size(); i++) {
                process(builder, expNode.getNode(i));
                if (i != expNode.size() - 1) {
                    builder.addCode(",");
                }
            }
            builder.addCode(")");
        } else {
            throw new NotImplementedException("!!!!");
        }
        return builder;
    }

    private MethodSpec.Builder processArithmetical(MethodSpec.Builder builder, ExpAstNode node) {
        if (CollectionUtils.isNotEmpty(node.getNodes()) && node.getNodes().size() == 2) {
            builder = builder.addCode("StdLibrary." + getArithmeticalMethod(node) + "(ctx, ");
            builder = processNode(builder, node.getNodes().get(0));
            builder = builder.addCode(",");
            builder = processNode(builder, node.getNodes().get(1));
            builder.addCode(")");
        } else {
            builder = builder.addCode("StdLibrary." + getArithmeticalMethod(node) + "(ctx, ");
            builder = processNode(builder, node.getNodes().get(0));
            builder.addCode(")");
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
            case AST_USUB:
                return "uSub";
            case AST_MUL:
                return "mul";
            case AST_DIV:
                return "div";
            default:
                return "mod";
        }
    }

    private String getRelationMethod(ExpAstNode node) {
        AstType type = node.getType();
        switch (type) {
            case AST_LT:
                return "lt";
            case AST_GT:
                return "gt";
            case AST_LE:
                return "le";
            case AST_GE:
                return "ge";
            case AST_EQ:
                return "eq";
            default:
                return "neq";
        }
    }

    private MethodSpec.Builder processNodeList(MethodSpec.Builder builder, ExpAstNode expNode, boolean createContext) {
        if (createContext) {
            //todo check this
            builder = builder.beginControlFlow("");
        }

        if (checkReturnNode(expNode.getNodes())) {
            //todo
//            throw new NotImplementedException("Oops! to do it");
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

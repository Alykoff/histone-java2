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
import org.apache.commons.lang3.ObjectUtils;
import ru.histone.v2.Constants;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.data.HistoneRegex;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.GlobalEvalNode;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.java_compiler.bcompiler.data.MacroFunction;
import ru.histone.v2.parser.node.*;
import ru.histone.v2.rtti.HistoneType;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Alexey Nevinsky
 */
public class TemplateProcessor {
    public void processTemplate(MethodSpec.Builder builder, AstNode root) {
        Params params = new Params(builder, root);
        addStatement(params, "CompletableFuture<StringBuilder> csb0 = CompletableFuture.completedFuture(new StringBuilder())");
        processNode(params);
        if (!checkReturnNode(((ExpAstNode) root).getNodes())) {
            addStatement(params, "return std.asString(ctx, csb0)");
        }
    }

    public void processNode(Params params) {
        if (params.node == null) {
            return;
        }

        if (params.node.hasValue()) {
            ValueNode valueNode = (ValueNode) params.node;
            Object v = valueNode.getValue();
            if (v == null) {
                params.builder.addCode("$T.getValue($T.NULL)", EvalUtils.class, ObjectUtils.class);
                return;
            }

            if (v instanceof String) {
                params.builder.addCode("$T.getValue($S)", EvalUtils.class, v);
                return;
            } else if (valueNode instanceof DoubleAstNode) {
                if (((Double) v).isInfinite() || ((Double) v).isNaN()) {
                    params.builder.addCode("$T.getValue($T.NULL)", EvalUtils.class, ObjectUtils.class);
                    return;
                }
            } else if (valueNode instanceof LongAstNode) {
                params.builder.addCode("$T.getValue(" + v + "L)", EvalUtils.class);
                return;
            }
            params.builder.addCode("$T.getValue(" + v + ")", EvalUtils.class);
            return;
        }

        switch (params.node.getType()) {
            case AST_ARRAY:
                processArrayNode(params);
                break;
            case AST_REGEXP:
                processRegExp(params);
                break;
            case AST_THIS:
                processThisNode(params);
                break;
            case AST_GLOBAL:
                processGlobalNode(params);
                break;
            case AST_NOT:
                processNotNode(params);
                break;
            case AST_AND:
                processLogical(params, true);
                break;
            case AST_OR:
                processLogical(params, false);
                break;
            case AST_TERNARY:
                processTernary(params);
                break;
            case AST_ADD:
            case AST_SUB:
            case AST_MUL:
            case AST_DIV:
            case AST_MOD:
            case AST_USUB:
                processArithmetical(params);
                break;
            case AST_LT:
            case AST_GT:
            case AST_LE:
            case AST_GE:
            case AST_EQ:
            case AST_NEQ:
                processRelation(params);
                break;
            case AST_REF:
                processReferenceNode(params);
                break;
            case AST_CALL:
                processCall(params);
                break;
            case AST_VAR:
                processVarNode(params);
                break;
            case AST_IF:
                processIfNode(params);
                break;
            case AST_FOR:
                processForNode(params.addCtx().addForCtx().incLoop());
                break;
            case AST_WHILE:
                processWhileNode(params.addCtx().addForCtx().incLoop());
                break;
            case AST_MACRO:
                processMacroNode(params.addMacroCtx().addCtx().incMacro());
                break;
            case AST_RETURN:
                processReturnNode(params);
            case AST_NODES:
                processNodes(params.addMacroCtx().addCtx().incMacro());
                break;
            case AST_NODELIST:
                processNodeList(params, false);
                break;
            case AST_BOR:
                processBorNode(params);
                break;
            case AST_BXOR:
                processBxorNode(params);
                break;
            case AST_BAND:
                processBandNode(params);
                break;
            case AST_SUPPRESS:
                processSuppressNode(params);
                break;
            case AST_CONTINUE:
                processContinueNode(params);
                break;
            case AST_BREAK:
                processBreakNode(params);
        }
    }

    private void processReturnNode(Params params) {
        addCode(params, "return csb%s.thenCompose(r%s -> ", params.macroCtxNum, params.macroCtxNum);
        processNode(params);
        addCode(params, ")\n.exceptionally($T.checkThrowable(null));\n", EvalUtils.class);
    }

    private void processBorNode(Params params) {
        addCode(params, "std.processBorNode(");
        processNode(params.withNode(0));
        addCode(params, " ,");
        processNode(params.withNode(1));
        addCode(params, ")");
    }

    private void processBxorNode(Params params) {
        addCode(params, "std.processBxorNode(");
        processNode(params.withNode(0));
        addCode(params, " ,");
        processNode(params.withNode(1));
        addCode(params, ")");
    }

    private void processBandNode(Params params) {
        addCode(params, "std.processBandNode(");
        processNode(params.withNode(0));
        addCode(params, " ,");
        processNode(params.withNode(1));
        addCode(params, ")");
    }

    private void processLogical(Params params, boolean negateCheck) {
        addCode(params, "std.processLogicalNode(");
        processNode(params.withNode(0));
        addCode(params, " ,");
        processNode(params.withNode(1));
        addCode(params, ", %s)", negateCheck);
    }

    private void processThisNode(Params params) {
        addCode(params, "ctx.getValue($T.THIS_CONTEXT_VALUE)", Constants.class);
    }

    private void processRegExp(Params params) {
//todo copypaste from evaluator
        ExpAstNode n = (ExpAstNode) params.node;
        final StringAstNode flagsNumNode = n.getNode(1);

        boolean isIgnoreCase = false;
        boolean isMultiline = false;
        boolean isGlobal = false;
        int flags = 0;

        if (flagsNumNode != null) {
            final String flagStr = flagsNumNode.getValue();

            isIgnoreCase = flagStr.contains("i");
            isMultiline = flagStr.contains("m");
            isGlobal = flagStr.contains("g");
        }

        final StringAstNode expNode = n.getNode(0);
        final String exp = expNode.getValue();
        final Pattern pattern = Pattern.compile(exp, flags);

        addCode(params, "$T.getValue(", EvalUtils.class);
        addCode(params, "new $T(", HistoneRegex.class);
        params.builder.addCode("$L, $L, $L, $T.compile($S, $L)))", isGlobal, isIgnoreCase, isMultiline, Pattern.class, exp, flags);
//        addCode(params, "%s, %s, %s, $T.compile(\"%s\", %s)))", Pattern.class, isGlobal, isIgnoreCase, isMultiline, exp, flags);

//        return new RegexEvalNode(new HistoneRegex(isGlobal, isIgnoreCase, isMultiline, pattern));
    }

    private void processSuppressNode(Params params) {
        processNode(params.withNode(0));
        addCode(params, ";\n");
    }

    private void processTernary(Params params) {
        ExpAstNode node = (ExpAstNode) params.node;

        addCode(params, "std.toBoolean(");
        processNode(params.with(node.getNode(0)));
        addCode(params, ") \n? ");
        processNode(params.with(node.getNode(1)));
        addCode(params, " \n: ");
        if (node.size() > 2) {
            processNode(params.with(node.getNode(2)));
        } else {
            addCode(params, "EvalUtils.getValue(null)", EvalUtils.class);
        }
    }

    private void processBreakNode(Params params) {
        addStatement(params, "break");
    }

    private void processContinueNode(Params params) {
        addStatement(params, "index%s++", params.forCtxNumber);
        addStatement(params, "continue");

    }

    private void processNotNode(Params params) {
        addCode(params, "std.asBooleanNot(ctx, ");
        processNode(params.withNode(0));
        addCode(params, ")");

    }

    private void processMacroNode(Params params) {
        ExpAstNode node = (ExpAstNode) params.node;

        addCode(params, "std.toMacro(args%s -> {\n", params.macroCounter.count);
        addCode(params, "CompletableFuture<$T> csb%s = CompletableFuture.completedFuture(new StringBuilder());\n", StringBuilder.class, params.macroCtxNum);
        addCode(params, "CompletableFuture<$T> v%s0 = args%s.get(0);\n", EvalNode.class, params.ctxNum - (Long) value(node.getNode(0)), params.macroCounter.count);

        long argsSize = 0;
        if (node.size() > 2) {
            argsSize = ((LongAstNode) node.getNode(2)).getValue();

            Map<Long, AstNode> defValue = new HashMap<>();
            for (int i = 0; i < (node.size() - 3) / 2; i++) {
                defValue.put(((LongAstNode) node.getNode(i * 2 + 3)).getValue() + 1, node.getNode(i * 2 + 4));
            }

            for (long i = 1; i < argsSize + 1; i++) {
                addCode(params, "CompletableFuture<EvalNode> v%s%s = args%s.get(%s)", params.ctxNum - (Long) value(node.getNode(0)), i, params.macroCounter.count, i);
                if (defValue.size() > 0) {
                    if (defValue.get(i) != null) {
                        addCode(params, "\n.thenCompose(v -> v.getType() == $T.T_UNDEFINED \n? ", HistoneType.class);
                        processNode(params.with(defValue.get(i)).decCtx());
                        addCode(params, " \n: CompletableFuture.completedFuture(v))");
                    }
                }
                addCode(params, ";\n");
            }
        }
        processNode(params.withNode(1));
        if (!node.getNode(1).hasValue() && !checkReturnNode(((ExpAstNode) node.getNode(1)).getNodes())) {
            addStatement(params, "return std.asString(ctx, csb%s)", params.macroCtxNum);
        }

        addCode(params, "}, " + argsSize + ")");
    }

    private void processGlobalNode(Params params) {
        addCode(params, "CompletableFuture.completedFuture(new $T())", GlobalEvalNode.class);
    }

    private void processVarNode(Params params) {
        ValueNode n = ((ExpAstNode) params.node).getNode(1);
        addCode(params, "CompletableFuture<$T> v%s%s = ", EvalNode.class, params.ctxNum, value(n));
        processNode(params.withNode(0));
        addCode(params, ";\n");
    }

    private void processReferenceNode(Params params) {
        ExpAstNode n = (ExpAstNode) params.node;
        addCode(params, "v%s%s", params.ctxNum - (Long) value(n.getNode(0)), value(n.getNode(1)));

    }

    private void processWhileNode(Params params) {
        addStatement(params, "int i%s = -1", params.ctxNum);
        addCode(params, "while (");
        if (params.node.size() > 1) {
            addCode(params, "std.toBoolean(");
            processNode(params.withNode(1));
            addCode(params, ")");
        } else {
            addCode(params, "true");
        }
        addCode(params, ") {\n");
        addStatement(params, "$T self%s = std.constructWhileSelfValue(++i%s)",
                MapEvalNode.class, params.ctxNum, params.ctxNum);
        addStatement(params, "CompletableFuture<$T> v%s0 = CompletableFuture.completedFuture(self%s)",
                EvalNode.class, params.ctxNum, params.ctxNum);
        processNode(params.withNode(0));

        addCode(params, "}\n");
    }

    private void processForNode(Params params) {
//        final String arrObjName = "arrObj" + params.ctxNum;
        addCode(params, "$T arrObj%s = (", EvalNode.class, params.loopCounter.count);
        processNode(params.withNode(3).decCtx());
        addCode(params, ").join();\n");
        beginControlFlow(params, "if (arrObj%s instanceof $T && ((MapEvalNode) arrObj%s).getValue().size() > 0)", MapEvalNode.class, params.loopCounter.count, params.loopCounter.count);
        addStatement(params, "$T arr%s = (MapEvalNode) arrObj%s", MapEvalNode.class, params.ctxNum, params.loopCounter.count);
        addStatement(params, "$T index%s = 0", Integer.class, params.forCtxNumber);
        addStatement(params, "$T size%s = arr%s.getValue().size()", Integer.class, params.ctxNum, params.ctxNum);

        ExpAstNode n = (ExpAstNode) params.node;

        final boolean hasKey = ((ValueNode) n.getNode(0)).getValue() != null;
        final boolean hasValue = ((ValueNode) n.getNode(1)).getValue() != null;


        beginControlFlow(params, "while (index%s < size%s)", params.forCtxNumber, params.ctxNum);
        addStatement(params, "MapEvalNode self%s = std.constructForSelfValue(arr%s, index%s, ((MapEvalNode) arrObj%s).getValue().size() -1)",
                MapEvalNode.class, params.ctxNum, params.ctxNum, params.forCtxNumber, params.loopCounter.count);
        addStatement(params, "CompletableFuture<EvalNode> v%s0 = CompletableFuture.completedFuture(self%s)", params.ctxNum, params.ctxNum);
        if (hasKey) {
            addStatement(params, "CompletableFuture<EvalNode> v%s%s= CompletableFuture.completedFuture(self%s.getProperty(\"key\"))",
                    params.ctxNum, value(n.getNode(0)), params.ctxNum);
        }
        if (hasValue) {
            addStatement(params, "CompletableFuture<EvalNode> v%s%s= CompletableFuture.completedFuture(self%s.getProperty(\"value\"))",
                    params.ctxNum, value(n.getNode(1)), params.ctxNum);
        }

        processNode(params.withNode(2));

        if (!n.hasValue() && !checkBreakContinueNode(((ExpAstNode) n.getNode(2)).getNodes()) && !checkReturnNode(((ExpAstNode) n.getNode(2)).getNodes())) {
            addStatement(params, "index%s++", params.forCtxNumber);
        }
        endControlFlow(params);

        int startIndex = 4;
        //todo replace with if node
        if (n.size() > startIndex) {
            for (int i = 0; i < (n.size() - startIndex) / 2; i++) {
                addCode(params, "} else if (std.toBoolean(");
                processNode(params.withNode(2 * i + startIndex + 1));
                addCode(params, ")) {\n");
                processNode(params.withNode(2 * i + startIndex).addCtx());
            }
            if ((n.size() - 4) % 2 == 1) {
                addCode(params, "} else {\n");
                processNode(params.withNode(n.size() - 1).addCtx());
            }
        }
        addCode(params, "}\n");
    }

    private <T> T value(AstNode node) {
        return (T) ((ValueNode) node).getValue();
    }

    private void addCode(Params params, String str, Class clazz, Object... args) {
        params.builder.addCode(String.format(str, args), clazz);
    }

    private void addCode(Params params, String str, Object... args) {
        params.builder.addCode(String.format(str, args));
    }

    private void addStatement(Params params, String str, Class clazz, Object... args) {
        params.builder.addStatement(String.format(str, args), clazz);
    }

    private void addStatement(Params params, String str, Object... args) {
        params.builder.addStatement(String.format(str, args));
    }

    private void beginControlFlow(Params params, String str, Object... args) {
        params.builder.beginControlFlow(String.format(str, args));
    }

    private void beginControlFlow(Params params, String str, Class clazz, Object... args) {
        params.builder.beginControlFlow(String.format(str, args), clazz);
    }

    private void endControlFlow(Params params) {
        params.builder.endControlFlow();
    }

    private void processIfNode(Params params) {
        ExpAstNode n = (ExpAstNode) params.node;
        for (int i = 0; i < n.size() / 2; i++) {
            if (i != 0) {
                addCode(params, "} else ");
            }
            addCode(params, "if (std.toBoolean(");
            processNode(params.withNode(2 * i + 1));
            addCode(params, ")) {\n");
            if (((ExpAstNode) params.node).getNode(2 * i).hasValue()) {
                ExpAstNode node = new ExpAstNode(AstType.AST_NODELIST, ((ExpAstNode) params.node).getNode(2 * i));
                processNode(params.with(node));
            } else {
                processNode(params.withNode(2 * i).addCtx());
            }
        }
        if (n.size() % 2 == 1) {
            addCode(params, "} else {\n");
            if (((ExpAstNode) params.node).getNode(n.size() - 1).hasValue()) {
                ExpAstNode node = new ExpAstNode(AstType.AST_NODELIST, ((ExpAstNode) params.node).getNode(n.size() - 1));
                processNode(params.with(node));
            } else {
                processNode(params.withNode(n.size() - 1).addCtx());
            }
        }
        addCode(params, "}\n");
    }

    private void processRelation(Params params) {
        addCode(params, "std.%s(ctx, ", getRelationMethod((ExpAstNode) params.node));
        processNode(params.withNode(0));
        addCode(params, ",");
        processNode(params.withNode(1));
        addCode(params, ")");

    }

    private void processArrayNode(Params params) {
        ExpAstNode n = (ExpAstNode) params.node;
        if (CollectionUtils.isEmpty(n.getNodes())) {
            addCode(params, "std.array()");
            return;
        }
        addCode(params, "std.array(");
        for (int i = 0; i < n.size(); i++) {
            processNode(params.withNode(i));
            if (i != n.size() - 1) {
                addCode(params, ",");
            }
        }
        addCode(params, ")");
    }

    private void processCall(Params params) {
        CallExpAstNode callNode = (CallExpAstNode) params.node;
        if (callNode.getCallType() == CallType.SIMPLE) {
            addCode(params, "std.simpleCall(ctx,");
            if (callNode.size() == 2) {
                if (callNode.getNode(0).getType() == AstType.AST_GLOBAL) {
                    addCode(params, "\"" + ((ValueNode) callNode.getNode(1)).getValue() + "\", $T.singletonList(", Collections.class);
                    addCode(params, "CompletableFuture.completedFuture(new $T()))", GlobalEvalNode.class);
                } else {
                    addCode(params, "\"" + ((ValueNode) callNode.getNode(1)).getValue() + "\", $T.singletonList(\n", Collections.class);
                    processNode(params.withNode(0));
                    addCode(params, "\n)");
                }
            } else {
                addCode(params, "\"" + ((ValueNode) callNode.getNode(1)).getValue() + "\", $T.asList(\n", Arrays.class);
                processNode(params.withNode(0));
                addCode(params, ",\n");
                for (int i = 2; i < callNode.size(); i++) {
                    processNode(params.withNode(i));
                    if (i != callNode.size() - 1) {
                        addCode(params, ",");
                    }
                }
                addCode(params, "\n)");
            }
            addCode(params, ")");
        } else if (callNode.getCallType() == CallType.RTTI_M_GET) {
            if (callNode.getNode(0).getType() == AstType.AST_THIS) {
                addCode(params, "std.getFromCtx(ctx, ");
                processNode(params.withNode(1));
            } else {
                addCode(params, "std.mGet(ctx, ");
                for (int i = 0; i < callNode.size(); i++) {
                    processNode(params.withNode(i));
                    if (i != callNode.size() - 1) {
                        addCode(params, ",");
                    }
                }
            }
            addCode(params, ")");
        } else if (!(callNode.getNode(0) instanceof ValueNode)) {
            addCode(params, "std.mCall(ctx, ");
            for (int i = 0; i < callNode.size(); i++) {
                processNode(params.withNode(i));
                if (i != callNode.size() - 1) {
                    addCode(params, ",");
                }
            }
            addCode(params, ")");
        } else {
            addCode(params, "null");
        }

    }

    private void processArithmetical(Params params) {
        ExpAstNode n = (ExpAstNode) params.node;
        addCode(params, "std.%s(ctx, ", getArithmeticalMethod(n));
        processNode(params.withNode(0));

        if (CollectionUtils.isNotEmpty(n.getNodes()) && n.getNodes().size() == 2) {
            addCode(params, ",");
            processNode(params.withNode(1));
        }

        addCode(params, ")");
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

    private void processNodes(Params params) {
        ExpAstNode n = (ExpAstNode) params.node;
        addCode(params, "(($T) args%s -> {", MacroFunction.class, params.macroCounter.count);
        addCode(params, "CompletableFuture<$T> csb%s = CompletableFuture.completedFuture(new StringBuilder());\n", StringBuilder.class, params.macroCtxNum);

        processNodeList(params, true);
        addCode(params, "}).apply($T.emptyList())", Collections.class);
    }

    private void processNodeList(Params params, boolean returnIsNeeded) {
        ExpAstNode n = (ExpAstNode) params.node;
        for (AstNode node : n.getNodes()) {
            if (checkIsCompleteNode(node)) {
                if (node.getType() == AstType.AST_RETURN) {
                    //                    processNode(params.with(node).withNode(0));
                    addCode(params, "return csb%s.thenCompose(r%s -> ", params.macroCtxNum, params.macroCtxNum);
                    processNode(params.with(node).withNode(0));
                    addCode(params, ")\n.exceptionally($T.checkThrowable(null));\n", EvalUtils.class);
                } else {
                    processNode(params.with(node));
                }
                break;
            }
            if (node.getType() == AstType.AST_CONTINUE || node.getType() == AstType.AST_BREAK) {
                processNode(params.with(node));
                break;
            }
            processNodeInNodeList(params.with(node));
        }

        if (!checkReturnNode(n.getNodes()) && returnIsNeeded) {
            addStatement(params, "return std.asString(ctx, csb%s)", params.macroCtxNum);
        }
    }

    private void processNodeInNodeList(Params params) {
        if (isPrintNode(params.node)) {
            addCode(params, "csb%s = ", params.macroCtxNum);
            addCode(params, "std.append(ctx, csb%s, ", params.macroCtxNum);
            processNode(params);
            addCode(params, ")");
            addCode(params, ";\n");
        } else {
            processNode(params);
        }

    }

    private boolean isPrintNode(AstNode node) {
        List<AstType> types = Arrays.asList(AstType.AST_FOR, AstType.AST_IF, AstType.AST_VAR, AstType.AST_CONTINUE,
                AstType.AST_BREAK, AstType.AST_SUPPRESS, AstType.AST_WHILE);
        return !types.contains(node.getType());
    }


    private boolean checkIfCompleteAndReturn(ExpAstNode node) {
        if (node.size() % 2 != 1) {
            return false;
        }
        int returnCount = 0;
        for (int i = 0; i < node.size(); i++) {
            if (i % 2 == 1) {
                continue;
            }
            AstNode n = node.getNode(i);
            if (!n.hasValue() && checkReturnNode(((ExpAstNode) n).getNodes())) {
                returnCount++;
            }
        }
        return returnCount == (node.size() / 2) + 1;
    }

    private boolean checkIsCompleteNode(AstNode node) {
        if (node.getType() == AstType.AST_RETURN) {
            return true;
        }
        if (node.getType() == AstType.AST_IF && checkIfCompleteAndReturn((ExpAstNode) node)) {
            return true;
        }
        return false;
    }

    private boolean checkReturnNode(List<AstNode> nodes) {
        for (AstNode node : nodes) {
            if (checkIsCompleteNode(node)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkBreakContinueNode(List<AstNode> nodes) {
        return nodes.stream()
                .filter(n -> n.getType() == AstType.AST_BREAK || n.getType() == AstType.AST_CONTINUE)
                .findFirst()
                .isPresent();
    }

    private static class Params {
        MethodSpec.Builder builder;
        AstNode node;
        int macroCtxNum = 0;
        int forCtxNumber = 0;
        int ctxNum = 0;
        Counter loopCounter = new Counter();
        Counter macroCounter = new Counter();

        Params(MethodSpec.Builder builder, AstNode node) {
            this.builder = builder;
            this.node = node;
        }

        Params with(AstNode node) {
            Params params = new Params(this.builder, node);
            params.macroCtxNum = this.macroCtxNum;
            params.ctxNum = this.ctxNum;
            params.forCtxNumber = this.forCtxNumber;
            params.loopCounter = this.loopCounter;
            params.macroCounter = this.macroCounter;
            return params;
        }

        Params withNode(int index) {
            Params params = new Params(this.builder, ((ExpAstNode) node).getNode(index));
            params.macroCtxNum = this.macroCtxNum;
            params.ctxNum = this.ctxNum;
            params.forCtxNumber = this.forCtxNumber;
            params.loopCounter = this.loopCounter;
            params.macroCounter = this.macroCounter;
            return params;
        }

        Params addCtx() {
            Params params = new Params(this.builder, node);
            params.macroCtxNum = this.macroCtxNum;
            params.ctxNum = this.ctxNum + 1;
            params.forCtxNumber = this.forCtxNumber;
            params.loopCounter = this.loopCounter;
            params.macroCounter = this.macroCounter;
            return params;
        }

        Params decCtx() {
            Params params = new Params(this.builder, node);
            params.macroCtxNum = this.macroCtxNum;
            params.ctxNum = this.ctxNum - 1;
            params.forCtxNumber = this.forCtxNumber;
            params.loopCounter = this.loopCounter;
            params.macroCounter = this.macroCounter;
            return params;
        }

        Params addMacroCtx() {
            Params params = new Params(this.builder, node);
            params.macroCtxNum = this.macroCtxNum + 1;
            params.ctxNum = this.ctxNum;
            params.forCtxNumber = this.forCtxNumber;
            params.loopCounter = this.loopCounter;
            params.macroCounter = this.macroCounter;
            return params;
        }

        Params addForCtx() {
            Params params = new Params(this.builder, node);
            params.macroCtxNum = this.macroCtxNum;
            params.ctxNum = this.ctxNum;
            params.forCtxNumber = this.forCtxNumber + 1;
            params.loopCounter = this.loopCounter;
            params.macroCounter = this.macroCounter;
            return params;
        }

        Params incLoop() {
            Params params = new Params(this.builder, node);
            params.macroCtxNum = this.macroCtxNum;
            params.ctxNum = this.ctxNum;
            params.forCtxNumber = this.forCtxNumber;
            params.loopCounter = this.loopCounter;
            params.loopCounter.count++;
            params.macroCounter = this.macroCounter;
            return params;
        }

        Params incMacro() {
            Params params = new Params(this.builder, node);
            params.macroCtxNum = this.macroCtxNum;
            params.ctxNum = this.ctxNum;
            params.forCtxNumber = this.forCtxNumber;
            params.loopCounter = this.loopCounter;
            params.macroCounter = this.macroCounter;
            params.macroCounter.count++;
            return params;
        }
    }

    private static class Counter {
        int count = 0;
    }
}

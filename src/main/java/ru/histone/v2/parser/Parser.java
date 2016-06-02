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

import org.apache.commons.lang3.StringUtils;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.exceptions.HistoneException;
import ru.histone.v2.exceptions.ParserException;
import ru.histone.v2.exceptions.SyntaxErrorException;
import ru.histone.v2.exceptions.UnexpectedTokenException;
import ru.histone.v2.parser.node.*;
import ru.histone.v2.parser.tokenizer.*;
import ru.histone.v2.utils.AstJsonProcessor;
import ru.histone.v2.utils.ParserUtils;
import ru.histone.v2.utils.Tuple;

import java.util.*;
import java.util.regex.Pattern;

import static ru.histone.v2.parser.node.AstType.*;
import static ru.histone.v2.parser.tokenizer.Tokens.*;

/**
 * Class used for validate and create AST tree from histone template. It doesn't have a state, so it you can create
 * only one instance.
 * Parser using {@link Tokenizer} for getting tokens from input string.
 *
 * @author Alexey Nevinsky
 * @author Gali Alykoff
 */
public class Parser {
    public static final String IDENTIFIER = "IDENTIFIER";
    private static final Pattern regexpFlagsPattern = Pattern.compile("^(?:([gim])(?!.*\\1))*$");
    private static final Optimizer optimizer = new Optimizer();

    public ExpAstNode process(String template, String baseURI) throws HistoneException {
        Tokenizer tokenizer = new Tokenizer(template, baseURI, ExpressionList.VALUES);
        TokenizerWrapper wrapper = new TokenizerWrapper(tokenizer);
        wrapper.enter();
        ExpAstNode result = getNodeList(wrapper);
        if (!next(wrapper, T_EOF)) {
            throw buildUnexpectedTokenException(wrapper, "EOF");
        }

        final Optimizer optimizer = new Optimizer();
        result = (ExpAstNode) optimizer.mergeStrings(result);

//        final Marker marker = new Marker();
//        marker.markReferences(result);
        wrapper.leave();
        return result;
    }

    private ExpAstNode getNodeList(TokenizerWrapper wrapper) throws ParserException {
        ExpAstNode result = new ExpAstNode(AST_NODELIST);
        AstNode node;
        wrapper = new TokenizerWrapper(wrapper);
        for (; ; ) {
            node = getStatement(wrapper);
            if (node.getType() == AST_T_BREAK) {
                break;
            } else if (node.getType() != AST_T_NOP) {
                if (node.getType() != AST_T_ARRAY) {
                    result.add(node);
                } else if (!node.hasValue()) {
                    result.addAll(((ExpAstNode) node).getNodes());
                }
            }
        }
        return result;
    }

    private AstNode getStatement(TokenizerWrapper wrapper) throws ParserException {
        if (next(wrapper, T_AST_START)) {
            return getASTStatement(wrapper);
        }
        if (next(wrapper, T_BLOCK_START)) {
            return getTemplateStatement(wrapper);
        }
        if (next(wrapper, T_LITERAL_START)) {
            return getLiteralStatement(wrapper);
        }
        if (next(wrapper, T_CMT_START)) {
            return getCommentStatement(wrapper);
        }
        if (!wrapper.test(T_EOF.getId()).isFound()) {
            return new StringAstNode(wrapper.next().firstValue());
        }
        return new ExpAstNode(AST_T_BREAK);
    }

    private AstNode getASTStatement(TokenizerWrapper wrapper) throws ParserException {
        final String baseURI = wrapper.getBaseURI();
        wrapper.setBaseURI("");
        final TokenizerWrapper cleanWrapper = wrapper.getCleanWrapper();
        final ExpAstNode result = new ExpAstNode(AST_NODELIST);
        while (!test(cleanWrapper, T_EOF) && !test(cleanWrapper, T_AST_END)) {
            final AstNode node = getStatement(cleanWrapper);
            final AstType type = node.getType();
            switch (type) {
                case AST_T_NOP: continue;
                case AST_T_BREAK: break;
                case AST_T_ARRAY:
                    final ExpAstNode expNode = (ExpAstNode) node;
                    result.addAll(expNode.getNodes());
                    continue;
                default: result.add(node);
            }
        }

        if (!next(cleanWrapper, T_AST_END)) {
            throw buildUnexpectedTokenException(cleanWrapper, "#}}");
        }
        wrapper.setBaseURI(baseURI);
        return new StringAstNode(
            AstJsonProcessor.write(optimizer.mergeStrings(result))
        );
    }

    private ExpAstNode getCommentStatement(TokenizerWrapper wrapper) throws ParserException {
        while (!test(wrapper, T_CMT_END) && !test(wrapper, T_EOF)) {
            wrapper.next();
        }
        if (!next(wrapper, T_CMT_END)) {
            throw buildUnexpectedTokenException(wrapper, "*}}");
        }
        return new ExpAstNode(AST_NOP);
    }

    private AstNode getTemplateStatement(TokenizerWrapper wrapper) throws ParserException {
        wrapper = new TokenizerWrapper(wrapper, Arrays.asList(T_SPACES.getId(), T_EOL.getId()));
        AstNode result;
        if (next(wrapper, T_IF)) {
            result = getIfStatement(wrapper);
        } else if (next(wrapper, T_FOR)) {
            result = getForStatement(wrapper);
        } else if (next(wrapper, T_WHILE)) {
            result = getWhileStatement(wrapper);
        } else if (next(wrapper, T_VAR)) {
            result = getVarStatement(wrapper);
        } else if (next(wrapper, T_MACRO)) {
            result = getMacroStatement(wrapper);
        } else if (next(wrapper, T_RETURN)) {
            result = getReturnStatement(wrapper);
        } else if (next(wrapper, T_SUPPRESS)) {
            result = getSuppressStatement(wrapper);
        } else if (next(wrapper, T_BREAK)) {
            result = getBreakContinueStatement(wrapper, true);
        } else if (next(wrapper, T_CONTINUE)) {
            result = getBreakContinueStatement(wrapper, false);
        } else if (test(wrapper, T_SLASH, T_STATEMENT, T_BLOCK_END)) {
            result = new ExpAstNode(AST_T_BREAK);
        } else if (test(wrapper, T_STATEMENT)) {
            result = new ExpAstNode(AST_T_BREAK);
        } else {
            result = getExpressionStatement(wrapper);
        }
        return result;
    }

    private AstNode getBreakContinueStatement(TokenizerWrapper wrapper, boolean isBreak) {
        if (!wrapper.isFor()) {
            throw buildSyntaxErrorException(wrapper, (isBreak ? "Break" : "Continue") + " statement must be only in loop!");
        }

        final ExpAstNode result = new ExpAstNode(isBreak ? AST_BREAK : AST_CONTINUE);
        if (!next(wrapper, T_BLOCK_END)) {
            throw buildUnexpectedTokenException(wrapper, "}}");
        }
        return result;
    }

    private AstNode getExpressionStatement(TokenizerWrapper wrapper) throws ParserException {
        wrapper = new TokenizerWrapper(wrapper, Arrays.asList(T_SPACES.getId(), T_EOL.getId()));
        final boolean isParentVar = wrapper.isVar();
        final boolean isParentFor = wrapper.isFor();
        wrapper.setVar(false);
        wrapper.setFor(false);
        if (next(wrapper, T_BLOCK_END)) {
            return new ExpAstNode(AST_T_NOP);
        }
        AstNode expression = getExpression(wrapper);
        if (!next(wrapper, T_BLOCK_END)) {
            throw buildUnexpectedTokenException(wrapper, "}}");
        }
//        AstNode res = new ExpAstNode(AST_EXPRESSION_STATEMENT, expression);
        wrapper.setFor(isParentFor);
        wrapper.setVar(isParentVar);
        return expression;
    }

    private ExpAstNode getSuppressStatement(TokenizerWrapper wrapper) {
        final ExpAstNode result = new ExpAstNode(AST_SUPPRESS, getExpression(wrapper));
        if (!next(wrapper, T_BLOCK_END)) {
            throw buildUnexpectedTokenException(wrapper, "}}");
        }
        return result;
    }

    private ExpAstNode getReturnStatement(TokenizerWrapper wrapper) throws ParserException {
        wrapper = new TokenizerWrapper(wrapper, Arrays.asList(T_SPACES.getId(), T_EOL.getId()));
        final boolean isParentVar = wrapper.isVar();
        final boolean isParentReturn = wrapper.isReturn();
        final boolean isParentFor = wrapper.isFor();
        wrapper.setVar(false);
        wrapper.setReturn(true);
        wrapper.setFor(false);

        final ExpAstNode result = new ExpAstNode(AST_RETURN);
        if (next(wrapper, T_BLOCK_END)) {
            result.add(getNodesStatement(wrapper, false));
            if (!next(wrapper, T_SLASH, T_RETURN)) {
                throw buildUnexpectedTokenException(wrapper, "{{/return}}");
            }
        } else {
            result.add(getExpression(wrapper));
        }

        if (!next(wrapper, T_BLOCK_END)) {
            throw buildUnexpectedTokenException(wrapper, "}}");
        }

        wrapper.setFor(isParentFor);
        wrapper.setReturn(isParentReturn);
        wrapper.setVar(isParentVar);
        return result;
    }

    private ExpAstNode getVarStatement(TokenizerWrapper wrapper) throws ParserException {
        wrapper = new TokenizerWrapper(wrapper, Arrays.asList(T_SPACES.getId(), T_EOL.getId()));
        final boolean isParentVar = wrapper.isVar();
        final boolean isParentReturn = wrapper.isReturn();
        final boolean isParentFor = wrapper.isFor();
        wrapper.setVar(true);
        wrapper.setReturn(false);
        wrapper.setFor(false);

        TokenizerResult name;
        ExpAstNode result;
        if (!test(wrapper, T_ID, T_EQ)) {
            name = wrapper.next(T_ID);
            if (!name.isFound()) {
                throw buildUnexpectedTokenException(wrapper, IDENTIFIER);
            }
            if (!next(wrapper, T_BLOCK_END)) {
                throw buildUnexpectedTokenException(wrapper, "}}");
            }
            result = new ExpAstNode(
                    AST_VAR,
                    getNodesStatement(wrapper, false),
                    new LongAstNode(wrapper.getVarName(name.firstValue()))
            );
            if (!next(wrapper, T_SLASH, T_VAR)) {
                throw buildUnexpectedTokenException(wrapper, "{{/var}}");
            }
        } else {
            result = new ExpAstNode(AST_T_ARRAY);
            do {
                name = wrapper.next(T_ID);
                if (!name.isFound()) {
                    throw buildUnexpectedTokenException(wrapper, IDENTIFIER);
                }
                if (!next(wrapper, T_EQ)) {
                    throw buildUnexpectedTokenException(wrapper, "=");
                }
                ExpAstNode varNode = new ExpAstNode(AST_VAR)
                        .add(getExpression(wrapper))
                        .add(new LongAstNode(wrapper.getVarName(name.firstValue())));
                result.add(varNode);
                if (!next(wrapper, T_COMMA)) {
                    break;
                }
            } while (!wrapper.test(T_EOF.getId()).isFound());
        }
        if (!next(wrapper, T_BLOCK_END)) {
            throw buildUnexpectedTokenException(wrapper, "}}");
        }

        wrapper.setFor(isParentFor);
        wrapper.setReturn(isParentReturn);
        wrapper.setVar(isParentVar);
        return result;
    }

    private ExpAstNode getNodesStatement(TokenizerWrapper wrapper, boolean nested) throws ParserException {
        ExpAstNode res = new ExpAstNode(AST_NODES);
        wrapper = new TokenizerWrapper(wrapper);
        AstNode node;

        wrapper.enter();

        for (; ; ) {
            if (nested && test(wrapper, T_BLOCK_END)) {
                break;
            }
            node = getStatement(wrapper);
            final AstType type = node.getType();
            if (type == AST_T_BREAK) {
                break;
            }
            if (type != AST_T_NOP) {
                if (type != AST_T_ARRAY) {
                    res.add(node);
                } else if (!node.hasValue()) {
                    final ExpAstNode expNode = (ExpAstNode) node;
                    res.addAll(expNode.getNodes());
                }
            }
        }
        if (nested && !next(wrapper, T_BLOCK_END)) {
            throw buildUnexpectedTokenException(wrapper, "}}");
        }

        wrapper.leave();

        return res;
    }

    private ExpAstNode getWhileStatement(TokenizerWrapper wrapper) throws ParserException {
        wrapper = new TokenizerWrapper(wrapper, Arrays.asList(T_SPACES.getId(), T_EOL.getId()));
        final boolean isParentReturn = wrapper.isReturn();
        final boolean isParentVar = wrapper.isVar();
        final boolean isParentFor = wrapper.isFor();
        wrapper.setFor(true);
        wrapper.setReturn(false);
        wrapper.setVar(false);

        final ExpAstNode node = new ExpAstNode(AST_WHILE);

        final AstNode expressionNode;
        if (test(wrapper, T_BLOCK_END)) {
            expressionNode = null;
        } else {
            expressionNode = getExpression(wrapper);
        }

        if (!next(wrapper, T_BLOCK_END)) {
            throw buildUnexpectedTokenException(wrapper, "}}");
        }

        wrapper.enter();
        wrapper.getVarName("self");
        node.add(getNodeList(wrapper));
        if (expressionNode != null) {
            node.add(expressionNode);
        }
        wrapper.leave();

        if (!next(wrapper, T_SLASH, T_WHILE, T_BLOCK_END)) {
            throw buildUnexpectedTokenException(wrapper, "{{/while}}");
        }

        wrapper.setFor(isParentFor);
        wrapper.setVar(isParentVar);
        wrapper.setReturn(isParentReturn);
        return node;
    }

    private ExpAstNode getForStatement(TokenizerWrapper wrapper) throws ParserException {
        wrapper = new TokenizerWrapper(wrapper, Arrays.asList(T_SPACES.getId(), T_EOL.getId()));

        final boolean isParentReturn = wrapper.isReturn();
        final boolean isParentVar = wrapper.isVar();
        final boolean isParentFor = wrapper.isFor();
        wrapper.setFor(true);
        wrapper.setReturn(false);
        wrapper.setVar(false);

        final ExpAstNode node = new ExpAstNode(AST_FOR);
        final TokenizerResult id = wrapper.next(T_ID);

        List<String> vars = new ArrayList<>(2);

        if (id.isFound()) {
            final String keyName = id.firstValue();
            if (next(wrapper, T_COLON)) {
                vars.add(keyName); //add key name
                final TokenizerResult valueName = wrapper.next(T_ID);
                if (valueName.isFound()) {
                    final String value = valueName.firstValue();
                    if (value.equals(keyName)) {
                        throw buildSyntaxErrorException(wrapper, "key and value must differ");
                    }
                    vars.add(value); //add value name
                } else {
                    throw buildUnexpectedTokenException(wrapper, IDENTIFIER);
                }
            } else {
                vars.add(null);//add null as key name
                vars.add(keyName);//add value name
            }
        } else {
            vars.add(null);//add 'null' as key name
            vars.add(null);//add 'null' as value name
        }


        if (!next(wrapper, T_IN)) {
            throw buildUnexpectedTokenException(wrapper, "in");
        }
        final AstNode expressionNode = getExpression(wrapper);
        if (!next(wrapper, T_BLOCK_END)) {
            throw buildUnexpectedTokenException(wrapper, "}}");
        }

        wrapper.enter();
        wrapper.getVarName("self");
        for (String name : vars) {
            if (name == null) {
                node.add(new StringAstNode(null));
            } else {
                node.add(new LongAstNode(wrapper.getVarName(name)));
            }
        }
        node.add(getNodeList(wrapper), expressionNode);
        wrapper.leave();

        while (next(wrapper, T_ELSEIF)) {
            final AstNode node2 = getExpression(wrapper);
            if (!next(wrapper, T_BLOCK_END)) {
                throw buildUnexpectedTokenException(wrapper, "}}");
            }
            wrapper.enter();
            node.add(getNodeList(wrapper), node2);
            wrapper.leave();
        }

        if (next(wrapper, T_ELSE)) {
            if (!next(wrapper, T_BLOCK_END)) {
                throw buildUnexpectedTokenException(wrapper, "}}");
            }

            wrapper.enter();
            node.add(getNodeList(wrapper));
            wrapper.leave();
        }

        if (!next(wrapper, T_SLASH, T_FOR, T_BLOCK_END)) {
            throw buildUnexpectedTokenException(wrapper, "{{/for}}");
        }

        wrapper.setFor(isParentFor);
        wrapper.setVar(isParentVar);
        wrapper.setReturn(isParentReturn);
        return node;
    }

    private ExpAstNode getIfStatement(TokenizerWrapper wrapper) throws ParserException {
        ExpAstNode node = new ExpAstNode(AST_IF);
        do {
            AstNode condition = getExpression(wrapper);
            if (!next(wrapper, T_BLOCK_END)) {
                throw buildUnexpectedTokenException(wrapper, "}}");
            }
            wrapper.enter();
            node.add(getNodeList(wrapper), condition);
            wrapper.leave();
        } while (next(wrapper, T_ELSEIF));

        if (next(wrapper, T_ELSE)) {
            if (!next(wrapper, T_BLOCK_END)) {
                throw buildUnexpectedTokenException(wrapper, "}}");
            }
            wrapper.enter();
            node.add(getNodeList(wrapper));
            wrapper.leave();
        }
        if (!next(wrapper, T_SLASH, T_IF, T_BLOCK_END)) {
            throw buildUnexpectedTokenException(wrapper, "{{/if}}");
        }

        return node;
    }

    private ExpAstNode getMacroStatement(TokenizerWrapper wrapper) throws ParserException {
        final ExpAstNode result = new ExpAstNode(AST_MACRO);
        final TokenizerResult nameTokenResult = wrapper.next(T_ID);
        final List<AstNode> inputVars = new ArrayList<>();
        final List<String> nameOfVars = new ArrayList<>();
        if (!nameTokenResult.isFound()) {
            throw buildUnexpectedTokenException(wrapper, IDENTIFIER);
        }
        List<Long> paramVars = new ArrayList<>();

        if (next(wrapper, T_LPAREN) && !next(wrapper, T_RPAREN)) {
            do {
                final TokenizerResult nameOfVarToken = wrapper.next(T_ID);
                if (!nameOfVarToken.isFound()) {
                    throw buildUnexpectedTokenException(wrapper, IDENTIFIER);
                }
                final String nameOfVar = nameOfVarToken.firstValue();
                if (nameOfVars.contains(nameOfVar)) {
                    throw buildSyntaxErrorException(wrapper, "duplicate argument name \"" + nameOfVar + "\"");
                } else {
                    nameOfVars.add(nameOfVar);
                }

                final ExpAstNode nopNode;
                if (next(wrapper, T_EQ)) {
                    nopNode = ParserUtils.createNopNode(nameOfVar, getExpression(wrapper));
                } else {
                    nopNode = ParserUtils.createNopNode(nameOfVar);
                }
                inputVars.add(nopNode);
            } while (next(wrapper, T_COMMA));

            if (!next(wrapper, T_RPAREN)) {
                throw buildUnexpectedTokenException(wrapper, ")");
            }
        }

        if (!next(wrapper, T_BLOCK_END)) {
            throw buildUnexpectedTokenException(wrapper, "}}");
        }

        wrapper.startMacro();
        wrapper.enter();
        wrapper.getVarName("self");

        nameOfVars.forEach(name -> paramVars.add(wrapper.getVarName(name)));

        result.add(getNodeList(wrapper));

        if (!next(wrapper, T_SLASH, T_MACRO, T_BLOCK_END)) {
            throw buildUnexpectedTokenException(wrapper, "{{/macro}}");
        }

        if (paramVars.size() > 0) {
            result.add(new LongAstNode(paramVars.size()));
        }
        if (!inputVars.isEmpty()) {
            for (int i = 0; i < paramVars.size(); i++) {
                ExpAstNode n = (ExpAstNode) inputVars.get(i);
                if (n.getNode(1) != null) {
                    result.add(new LongAstNode(i));
                    result.add(n.getNode(1));
                }
            }
        }

        wrapper.endMacro();
        wrapper.leave();

        final AstNode macroNameNode = new LongAstNode(wrapper.getVarName(nameTokenResult.firstValue()));
        return new ExpAstNode(AST_VAR).add(result).add(macroNameNode);
    }

    private AstNode getExpression(TokenizerWrapper wrapper) throws ParserException {
        if (test(wrapper, T_ARROW) ||
                test(wrapper, T_ID, T_ARROW) ||
                test(wrapper, T_LPAREN, T_RPAREN) ||
                test(wrapper, T_LPAREN, T_ID, T_COMMA) ||
                test(wrapper, T_LPAREN, T_ID, T_RPAREN, T_ARROW)) {
            return getMacroExpression(wrapper);
        }
        return getTernaryExpression(wrapper);
    }

    private String checkAndGetMacroVarName(
            TokenizerWrapper wrapper, List<String> names, TokenizerResult nameVarToken
    ) throws ParserException {
        final String newVarName = nameVarToken.firstValue();
        if (names.contains(newVarName)) {
            throw buildSyntaxErrorException(wrapper, "duplicate argument name \"" + newVarName + "\"");
        }
        return newVarName;
    }

    private ExpAstNode getMacroExpression(TokenizerWrapper wrapper) throws ParserException {
        final List<String> varStringNames = new ArrayList<>();

        TokenizerResult name = wrapper.next(T_ID);
        if (name.isFound()) {
            varStringNames.add(
                    checkAndGetMacroVarName(wrapper, varStringNames, name)
            );
        } else if (next(wrapper, T_LPAREN)) {
            if (!test(wrapper, T_RPAREN)) {
                do {
                    name = wrapper.next(T_ID);
                    if (name.isFound()) {
                        varStringNames.add(
                                checkAndGetMacroVarName(wrapper, varStringNames, name)
                        );
                    } else {
                        throw buildUnexpectedTokenException(wrapper, IDENTIFIER);
                    }
                } while (next(wrapper, T_COMMA));
            }
            if (!next(wrapper, T_RPAREN)) {
                throw buildUnexpectedTokenException(wrapper, ")");
            }
        }

        if (!next(wrapper, T_ARROW)) {
            throw buildUnexpectedTokenException(wrapper, "=>");
        }

        wrapper.startMacro();
        wrapper.enter();
        wrapper.getVarName("self");

        varStringNames.forEach(wrapper::getVarName);

        ExpAstNode result = createMacroNode(wrapper, varStringNames.size());

        wrapper.endMacro();
        wrapper.leave();

        return result;
    }

    private ExpAstNode createMacroNode(TokenizerWrapper wrapper, long size) throws ParserException {
        final ExpAstNode returnNode = new ExpAstNode(AST_RETURN).add(getExpression(wrapper));
        final ExpAstNode listNode = new ExpAstNode(AST_NODELIST).add(returnNode);
        final ExpAstNode res = new ExpAstNode(AST_MACRO).add(listNode);
        if (size > 0) {
            res.add(new LongAstNode(size));
        }
        return res;
    }

    private AstNode getTernaryExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getLogicalOrExpression(wrapper);
        while (next(wrapper, T_QUERY)) {
            final ExpAstNode newRes = new ExpAstNode(AST_TERNARY)
                    .add(res)
                    .add(getExpression(wrapper));
            if (next(wrapper, T_COLON)) {
                newRes.add(getExpression(wrapper));
            }
            res = newRes;
        }
        return res;
    }

    private AstNode getLogicalOrExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getLogicalAndExpression(wrapper);
        while (next(wrapper, T_OR)) {
            res = new ExpAstNode(AST_OR)
                    .add(res)
                    .add(getLogicalAndExpression(wrapper));
        }
        return res;
    }

    private AstNode getLogicalAndExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getBitwiseOrExpression(wrapper);
        while (next(wrapper, T_AND)) {
            res = new ExpAstNode(AST_AND)
                    .add(res)
                    .add(getBitwiseOrExpression(wrapper));
        }
        return res;
    }

    private AstNode getBitwiseOrExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getBitwiseXorExpression(wrapper);
        while (next(wrapper, T_BOR)) {
            res = new ExpAstNode(AST_BOR)
                    .add(res)
                    .add(getBitwiseXorExpression(wrapper));
        }
        return res;
    }

    private AstNode getBitwiseXorExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getBitwiseAndExpression(wrapper);
        while (next(wrapper, T_BXOR)) {
            ExpAstNode node = new ExpAstNode(AST_BXOR);
            res = node.add(res).add(getBitwiseAndExpression(wrapper));
        }
        return res;
    }

    private AstNode getBitwiseAndExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getEqualityExpression(wrapper);
        while (next(wrapper, T_BAND)) {
            res = new ExpAstNode(AST_BAND)
                    .add(res)
                    .add(getEqualityExpression(wrapper));
        }
        return res;
    }

    private AstNode getEqualityExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getRelationalExpression(wrapper);
        while (true) {
            ExpAstNode node;
            if (next(wrapper, T_EQ)) {
                node = new ExpAstNode(AST_EQ);
            } else if (next(wrapper, T_NEQ)) {
                node = new ExpAstNode(AST_NEQ);
            } else {
                break;
            }
            res = node.add(res)
                    .add(getRelationalExpression(wrapper));
        }
        return res;
    }

    private AstNode getRelationalExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getAdditiveExpression(wrapper);
        while (true) {
            ExpAstNode node;
            if (next(wrapper, T_LE)) {
                node = new ExpAstNode(AST_LE);
            } else if (next(wrapper, T_GE)) {
                node = new ExpAstNode(AST_GE);
            } else if (next(wrapper, T_LT)) {
                node = new ExpAstNode(AST_LT);
            } else if (next(wrapper, T_GT)) {
                node = new ExpAstNode(AST_GT);
            } else {
                break;
            }
            res = node.add(res).add(getAdditiveExpression(wrapper));
        }
        return res;
    }

    private AstNode getAdditiveExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getMultiplicativeExpression(wrapper);
        while (true) {
            ExpAstNode node;
            if (next(wrapper, T_PLUS)) {
                node = new ExpAstNode(AST_ADD);
            } else if (next(wrapper, T_MINUS)) {
                node = new ExpAstNode(AST_SUB);
            } else {
                break;
            }
            res = node.add(res)
                    .add(getMultiplicativeExpression(wrapper));
        }
        return res;
    }

    private AstNode getMultiplicativeExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getUnaryExpression(wrapper);
        while (true) {
            ExpAstNode node;
            if (next(wrapper, T_STAR)) {
                node = new ExpAstNode(AST_MUL);
            } else if (next(wrapper, T_SLASH)) {
                node = new ExpAstNode(AST_DIV);
            } else if (next(wrapper, T_MOD)) {
                node = new ExpAstNode(AST_MOD);
            } else {
                break;
            }
            res = node.add(res).add(getUnaryExpression(wrapper));
        }
        return res;
    }

    private AstNode getUnaryExpression(TokenizerWrapper wrapper) throws ParserException {
        if (next(wrapper, T_NOT)) {
            final AstNode node = getUnaryExpression(wrapper);
            return new ExpAstNode(AST_NOT).add(node);
        } else if (next(wrapper, T_MINUS)) {
            final AstNode node = getUnaryExpression(wrapper);
            return new ExpAstNode(AST_USUB).add(node);
        } else {
            return getMemberExpression(wrapper);
        }
    }

    private AstNode getMemberExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getPrimaryExpression(wrapper);
        while (true) {
            if (next(wrapper, T_DOT)) {
                if (!test(wrapper, T_PROP)) {
                    throw buildUnexpectedTokenException(wrapper, IDENTIFIER);
                }
                final StringAstNode propNode = new StringAstNode(wrapper.next().firstValue());
                res = new CallExpAstNode(CallType.RTTI_M_GET, res, propNode);
            } else if (next(wrapper, T_METHOD)) {
                if (!test(wrapper, T_PROP)) {
                    throw buildUnexpectedTokenException(wrapper, IDENTIFIER);
                }
                final StringAstNode methodName = new StringAstNode(wrapper.next().firstValue());
                res = new CallExpAstNode(CallType.SIMPLE, res, methodName).addAll(parseArgumentList(wrapper));
            } else if (next(wrapper, T_LBRACKET)) {
                res = new CallExpAstNode(CallType.RTTI_M_GET, res).addAll(parseExpressionList(wrapper));
                if (!next(wrapper, T_RBRACKET)) {
                    throw buildUnexpectedTokenException(wrapper, "]");
                }
            } else if (test(wrapper, T_LPAREN)) {
                res = new CallExpAstNode(CallType.RTTI_M_CALL, res).addAll(parseArgumentList(wrapper));
            } else {
                return res;
            }
        }
    }

    private List<AstNode> parseExpressionList(TokenizerWrapper wrapper) {
        List<AstNode> nodes = new ArrayList<>();
        do {
            nodes.add(getExpression(wrapper));
        } while (next(wrapper, T_COMMA));
        return nodes;
    }

    private List<AstNode> parseArgumentList(TokenizerWrapper wrapper) {
        List<AstNode> nodes = new ArrayList<>();
        if (next(wrapper, T_LPAREN) && !next(wrapper, T_RPAREN)) {
            nodes.addAll(parseExpressionList(wrapper));
            if (!next(wrapper, T_RPAREN)) {
                throw buildUnexpectedTokenException(wrapper, ")");
            }
        }
        return nodes;
    }

    private AstNode getPrimaryExpression(TokenizerWrapper wrapper) throws ParserException {
        if (next(wrapper, T_NULL)) {
            return new StringAstNode(null);
        } else if (next(wrapper, T_TRUE)) {
            return new BooleanAstNode(true);
        } else if (next(wrapper, T_FALSE)) {
            return new BooleanAstNode(false);
        } else if (next(wrapper, T_SLASH)) {
            return getRegexpLiteral(wrapper);
        } else if (next(wrapper, T_AST_START)) {
            return getASTStatement(wrapper);
        } else if (next(wrapper, T_LITERAL_START)) {
            return getLiteralStatement(wrapper);
        } else if (test(wrapper, T_SQUOTE)) {
            return getStringLiteral(wrapper);
        } else if (test(wrapper, T_DQUOTE)) {
            return getStringLiteral(wrapper);
        } else if (next(wrapper, T_LBRACKET)) {
            return getArrayExpression(wrapper);
        } else if (next(wrapper, T_BLOCK_START)) {
            return getNodesStatement(wrapper, true);
        } else if (next(wrapper, T_THIS)) {
            return new ExpAstNode(AST_THIS);
        } else if (next(wrapper, T_GLOBAL)) {
            return new ExpAstNode(AST_GLOBAL);
        } else if (test(wrapper, T_INT)) {
            return new LongAstNode(Long.parseLong(wrapper.next().first().getValue(), 10));
        } else if (test(wrapper, T_BIN)) {
            return new LongAstNode(Long.parseLong(wrapper.next().first().getValue().substring(2), 2));
        } else if (test(wrapper, T_HEX)) {
            return new LongAstNode(Long.parseLong(wrapper.next().first().getValue().substring(2), 16));
        } else if (test(wrapper, T_FLOAT)) {
            return getDoubleValue(wrapper);
        } else if (test(wrapper, T_REF)) {
            return getReferenceExpression(wrapper);
        } else if (next(wrapper, T_LPAREN)) {
            return getParenthesizedExpression(wrapper);
        } else {
            throw buildUnexpectedTokenException(wrapper, "EXPRESSION");
        }
    }

    private AstNode getDoubleValue(TokenizerWrapper wrapper) {
        Double value = Double.parseDouble(wrapper.next().first().getValue());
        if (EvalUtils.isInteger(value)) {
            return new LongAstNode(value.longValue());
        }
        return new DoubleAstNode(value);
    }

    private ExpAstNode getReferenceExpression(TokenizerWrapper wrapper) {
        String refName = wrapper.next().first().getValue();
        Tuple<Long, Long> ref = wrapper.getRefPair(refName);
        if (ref != null) {
            return new ExpAstNode(AST_REF, new LongAstNode(ref.getLeft()), new LongAstNode(ref.getRight()));
        } else {
            return new CallExpAstNode(CallType.SIMPLE, new ExpAstNode(AST_GLOBAL), new StringAstNode(refName))
                    .addAll(parseArgumentList(wrapper));
        }
    }

    private ExpAstNode getArrayExpression(TokenizerWrapper wrapper) throws ParserException {
        wrapper = new TokenizerWrapper(wrapper, Arrays.asList(T_SPACES.getId(), T_EOL.getId()));
        int counter = 0;
        ExpAstNode result = new ExpAstNode(AST_ARRAY);
        String key = null;
        AstNode value;
        Set<String> values = new LinkedHashSet<>();
        Map<String, AstNode> rawResult = new LinkedHashMap<>();

        do {
            while (next(wrapper, T_COMMA)) ;
            if (next(wrapper, T_RBRACKET)) {
                fillNodeFromMap(result, rawResult);
                return result;
            }
            final TokenizerResult tokenRes = wrapper.next(T_PROP.getId(), T_COLON.getId());
            if (tokenRes.isFound()) {
                key = tokenRes.firstValue();
                value = getExpression(wrapper);
            } else {
                final AstNode rawKey = getExpression(wrapper);
                final Optional<Object> rawKeyOption = Optional.of(rawKey)
                        .filter(AstNode::hasValue)
                        .map(node -> ((ValueNode) node).getValue());

                final boolean isStringOrNumberValue = rawKeyOption.isPresent()
                        && (ParserUtils.isStrongString(rawKeyOption.get()) || ParserUtils.isNumber(rawKeyOption.get()))
                        && next(wrapper, T_COLON);
                if (isStringOrNumberValue) {
                    final Object keyObject = rawKeyOption.get();
                    value = getExpression(wrapper);
                    if (ParserUtils.isStrongString(keyObject)) {
                        if (ParserUtils.isInt((String) keyObject)) {
                            key = Integer.valueOf((String) keyObject).toString();
                        } else {
                            key = (String) keyObject;
                        }
                    }

                    Optional<Double> floatKeyValue = ParserUtils.tryDouble(keyObject);
                    if (floatKeyValue.isPresent()) {
                        int intKeyValue = floatKeyValue.get().intValue();
                        if (intKeyValue < counter) {
                            key = intKeyValue + "";
                        } else {
                            counter = intKeyValue;
                            key = (counter++) + "";
                        }
                    }
                } else {
                    value = rawKey;
                    key = String.valueOf(counter++);
                }
            }

            if (values.contains(key)) {
                rawResult.put(key, value);
            } else {
                values.add(key);
                rawResult.put(key, value);
            }
        } while (next(wrapper, T_COMMA));

        if (!next(wrapper, T_RBRACKET)) {
            throw buildUnexpectedTokenException(wrapper, "]");
        }
        fillNodeFromMap(result, rawResult);

        return result;
    }

    private void fillNodeFromMap(ExpAstNode result, Map<String, AstNode> map) {
        for (Map.Entry<String, AstNode> entry : map.entrySet()) {
            result
                    .add(entry.getValue())
                    .add(new StringAstNode(entry.getKey()));
        }
    }

    private StringAstNode getStringLiteral(TokenizerWrapper wrapper) throws ParserException {
        String start = wrapper.next().first().getValue();
        wrapper = new TokenizerWrapper(wrapper);
        TokenizerResult fragment;
        final StringBuilder builder = new StringBuilder();
        while ((fragment = wrapper.next()).isFound()) {
            if (fragment.first().getTypes().contains(T_EOF.getId())) {
                throw buildSyntaxErrorException(wrapper, "unterminated string literal");
            }
            if (StringUtils.equals(fragment.first().getValue(), start)) {
                break;
            } else if (StringUtils.equals(fragment.first().getValue(), "\\")) {
                builder.append("\\").append(wrapper.next().first().getValue());
            } else {
                builder.append(fragment.first().getValue());
            }
        }
        return new StringAstNode(builder.toString()).escaped();
    }

    private StringAstNode getLiteralStatement(TokenizerWrapper wrapper) throws ParserException {
        wrapper = new TokenizerWrapper(wrapper, Arrays.asList());
        final StringBuilder builder = new StringBuilder("");
        while (!test(wrapper, T_EOF) && !test(wrapper, T_LITERAL_END)) {
            builder.append(wrapper.next().first().getValue());
        }
        if (!next(wrapper, T_LITERAL_END)) {
            throw buildUnexpectedTokenException(wrapper, "%}}");
        }
        return new StringAstNode(builder.toString());
    }

    private AstNode getParenthesizedExpression(TokenizerWrapper wrapper) throws ParserException {
        wrapper = new TokenizerWrapper(wrapper, Arrays.asList(T_SPACES.getId(), T_EOL.getId()));
        AstNode node = getExpression(wrapper);
        if (!next(wrapper, T_RPAREN)) {
            throw buildUnexpectedTokenException(wrapper, ")");
        }
        return node;
    }

    private ExpAstNode getRegexpLiteral(TokenizerWrapper wrapper) throws ParserException {
        final StringBuilder result = new StringBuilder("");
        boolean inCharSet = false;

        for (; ; ) {
            if (wrapper.test(T_EOF.getId()).isFound()) {
                break;
            }
            if (test(wrapper, T_EOL)) {
                break;
            }
            if (!inCharSet && test(wrapper, T_SLASH)) {
                break;
            }
            if (next(wrapper, T_BACKSLASH)) {
                result.append("\\");
            } else if (test(wrapper, T_LBRACKET)) {
                inCharSet = true;
            } else if (test(wrapper, T_RBRACKET)) {
                inCharSet = false;
            }
            result.append(wrapper.next().first().getValue());
        }

        if (!next(wrapper, T_SLASH)) {
            throw buildSyntaxErrorException(wrapper, "unterminated regexp literal");
        }

        try {
            Pattern.compile(result.toString());// validate regex
        } catch (Exception e) {
            throw buildSyntaxErrorException(wrapper, e.getMessage());
        }

        String flagStr = null;
        final TokenizerResult flagsTokenizerResult = wrapper.next(T_PROP);
        if (flagsTokenizerResult.isFound()) {
            flagStr = flagsTokenizerResult.firstValue();

            if (!regexpFlagsPattern.matcher(flagStr).find()) {
                final String msg = "invalid flags supplied to regular expression '" + flagStr + "'";
                throw buildSyntaxErrorException(wrapper, msg);
            }
        }

        ExpAstNode res = new ExpAstNode(AST_REGEXP)
                .add(new StringAstNode(result.toString()));
        if (StringUtils.isNotEmpty(flagStr)) {
            res.add(new StringAstNode(flagStr));
        }
        return res;
    }

    private boolean next(TokenizerWrapper wrapper, Tokens... tokens) {
        return wrapper.next(toIntArr(tokens)).isFound();
    }

    private boolean test(TokenizerWrapper wrapper, Tokens... tokens) {
        return wrapper.test(toIntArr(tokens)).isFound();
    }

    private Integer[] toIntArr(Tokens... tokens) {
        Integer[] ids = new Integer[tokens.length];
        for (Integer i = 0; i < tokens.length; i++) {
            ids[i] = tokens[i].getId();
        }
        return ids;
    }

    private ParserException buildUnexpectedTokenException(
            TokenizerWrapper wrapper, String expected
    ) throws ParserException {
        Token token = wrapper.next().first();
        int line = wrapper.getLineNumber(token.getIndex());
        String value = token.getTypes().contains(T_EOF.getId()) ? "EOF" : token.getValue();
        String message = "unexpected '" + value + "', expected '" + expected + "'";
        return new UnexpectedTokenException(message, wrapper.getBaseURI(), line);
    }

    private SyntaxErrorException buildSyntaxErrorException(TokenizerWrapper wrapper, String s) throws ParserException {
        Token token = wrapper.next().first();
        int line = wrapper.getLineNumber(token.getIndex());
        return new SyntaxErrorException(s, wrapper.getBaseURI(), line);
    }
}

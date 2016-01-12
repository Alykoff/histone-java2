package ru.histone.v2.parser;

import org.apache.commons.lang.StringUtils;
import ru.histone.tokenizer.BaseTokens;
import ru.histone.tokenizer.Token;
import ru.histone.tokenizer.Tokens;
import ru.histone.v2.parser.node.AstNode;
import ru.histone.v2.parser.node.AstRegexType;
import ru.histone.v2.parser.node.AstType;
import ru.histone.v2.parser.tokenizer.ExpressionList;
import ru.histone.v2.parser.tokenizer.Tokenizer;
import ru.histone.v2.parser.tokenizer.TokenizerResult;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Collections;
import java.util.regex.Pattern;

/**
 * Created by alexey.nevinsky on 24.12.2015.
 */
public class Parser {

    private static final int T_NOP = -1;
    private static final int T_BREAK = -2;
    private static final int T_ARRAY = -3;

    private static final Pattern regexpFlagsPattern = Pattern.compile("^(?:([gim])(?!.*\\1))*$");

    public AstNode process(String template, String baseURI) throws ParserException {
        Tokenizer tokenizer = new Tokenizer(template, baseURI, ExpressionList.VALUES);
        AstNode result = getNodeList(tokenizer);
//        if (!tokenizer.next(tokenizer.$EOF))
//            UnexpectedToken(tokenizer, 'EOF');
//        Optimizer(template);
//        markReferences(template);
        return result;
    }

    private AstNode getNodeList(Tokenizer tokenizer) throws ParserException {
        AstNode result = new AstNode(AstType.AST_NODELIST);
        AstNode node;
        for (; ; ) {
            node = getStatement(tokenizer);
            if (node.getType() == T_BREAK) {
                break;
            } else if (node.getType() != T_NOP) {
                //todo check this
                if (node.getType() != T_ARRAY) {
                    result.add(node);
                } else {
                    result.add(node);
                }
            }
        }
        return result;
    }

    private AstNode getStatement(Tokenizer tokenizer) throws ParserException {
        if (tokenizer.next(Tokens.T_BLOCK_START.getId()).isFound()) {
            return getTemplateStatement(tokenizer);
        }
        if (tokenizer.next(Tokens.T_LITERAL_START.getId()).isFound()) {
            return getLiteralStatement(tokenizer);
        }
        if (tokenizer.next(Tokens.T_CMT_START.getId()).isFound()) {
            return getCommentStatement(tokenizer);
        }
        if (!tokenizer.test(BaseTokens.T_EOF.getId()).isFound()) {
            return AstNode.forValue(tokenizer.next().first().getValue());
        }
        return new AstNode(T_BREAK);
    }

    private AstNode getCommentStatement(Tokenizer tokenizer) {
        throw new NotImplementedException();
    }

    private AstNode getTemplateStatement(Tokenizer tokenizer) throws ParserException {
        tokenizer.setIgnored(Collections.singletonList(Tokens.T_SPACES.getId()));
        AstNode result;
        if (next(tokenizer, Tokens.T_IF)) {
            result = getIfStatement(tokenizer);
        } else if (next(tokenizer, Tokens.T_FOR)) {
            result = getForStatement(tokenizer);
        } else if (next(tokenizer, Tokens.T_VAR)) {
            result = getVarStatement(tokenizer);
        } else if (next(tokenizer, Tokens.T_MACRO)) {
            result = getMacroExpression(tokenizer);
        } else if (next(tokenizer, Tokens.T_RETURN)) {
            result = getReturnStatement(tokenizer);
        } else if (next(tokenizer, Tokens.T_SUPRESS)) {
            result = getSupressStatement(tokenizer);
        } else if (next(tokenizer, Tokens.T_LISTEN)) {
            result = getListenStatement(tokenizer, AstType.AST_LISTEN);
        } else if (next(tokenizer, Tokens.T_TRIGGER)) {
            result = getListenStatement(tokenizer, AstType.AST_TRIGGER);
        } else if (test(tokenizer, Tokens.T_SLASH, Tokens.T_STATEMENT, Tokens.T_BLOCK_END)) {
            result = new AstNode(T_BREAK);
        } else if (test(tokenizer, Tokens.T_STATEMENT)) {
            result = new AstNode(T_BREAK);
        } else {
            result = getExpressionStatement(tokenizer);
        }
        tokenizer.setIgnored(Collections.emptyList());
        return result;
    }

    private AstNode getExpressionStatement(Tokenizer tokenizer) throws ParserException {
        if (next(tokenizer, Tokens.T_BLOCK_END)) {
            return new AstNode(T_NOP);
        }
        AstNode expression = getExpression(tokenizer);
        if (!next(tokenizer, Tokens.T_BLOCK_END)) {
            UnexpectedToken(tokenizer, "}}");
        }
        return expression;
    }

    private AstNode getListenStatement(Tokenizer tokenizer, AstType astListen) {
        throw new org.apache.commons.lang.NotImplementedException();
    }

    private AstNode getSupressStatement(Tokenizer tokenizer) {
        throw new org.apache.commons.lang.NotImplementedException();
    }

    private AstNode getReturnStatement(Tokenizer tokenizer) {
        throw new org.apache.commons.lang.NotImplementedException();
    }

    private AstNode getVarStatement(Tokenizer tokenizer) {
        throw new org.apache.commons.lang.NotImplementedException();
    }

    private AstNode getForStatement(Tokenizer tokenizer) {
        throw new org.apache.commons.lang.NotImplementedException();
    }

    private AstNode getIfStatement(Tokenizer tokenizer) throws ParserException {
        AstNode node = new AstNode(AstType.AST_IF);
        do {
            AstNode condition = getExpression(tokenizer);
            if (!tokenizer.next(Tokens.T_BLOCK_END.getId()).isFound()) {
                UnexpectedToken(tokenizer, "}}");
            }
            node.add(getNodeList(tokenizer), condition);
        } while (next(tokenizer, Tokens.T_ELSEIF));

        if (next(tokenizer, Tokens.T_ELSE)) {
            if (!next(tokenizer, Tokens.T_BLOCK_END)) {
                UnexpectedToken(tokenizer, "}}");
            }
            node.add(getNodeList(tokenizer));
        }
        if (!tokenizer.next(Tokens.T_SLASH.getId(), Tokens.T_IF.getId(), Tokens.T_BLOCK_END.getId()).isFound()) {
            UnexpectedToken(tokenizer, "{{/if}}");
        }

        return node;
    }

    private AstNode getExpression(Tokenizer tokenizer) throws ParserException {
        if (test(tokenizer, Tokens.T_ARROW) ||
            test(tokenizer, Tokens.T_ID, Tokens.T_ARROW) ||
            test(tokenizer, Tokens.T_LPAREN, Tokens.T_RPAREN) ||
            test(tokenizer, Tokens.T_LPAREN, Tokens.T_ID, Tokens.T_COMMA) ||
            test(tokenizer, Tokens.T_LPAREN, Tokens.T_ID, Tokens.T_RPAREN, Tokens.T_ARROW)) {
            return getMacroExpression(tokenizer);
        }
        return getTernaryExpression(tokenizer);
    }

    private AstNode getMacroExpression(Tokenizer tokenizer) throws ParserException {
        AstNode result = new AstNode(AstType.AST_NODELIST);

        TokenizerResult name = tokenizer.next(Tokens.T_ID.getId());
        if (name.isFound()) {
            result.add(new AstNode(AstType.AST_NOP).addValue(name.first().getValue()));
        } else if (next(tokenizer, Tokens.T_LPAREN)) {
            if (!test(tokenizer, Tokens.T_RPAREN)) {
                do {
                    name = tokenizer.next(Tokens.T_ID.getId());
                    if (name.isFound()) {
                        result.add(new AstNode(AstType.AST_NOP).addValue(name.first().getValue()));
                    } else {
                        UnexpectedToken(tokenizer, "IDENTIFIER");
                    }
                } while (next(tokenizer, Tokens.T_COMMA));
            }
            if (!next(tokenizer, Tokens.T_RPAREN)) {
                UnexpectedToken(tokenizer, ")");
            }
        }
        if (!next(tokenizer, Tokens.T_ARROW)) {
            UnexpectedToken(tokenizer, "=>");
        }
        if (result.getNodes().size() > 0) {
            //todo
//            result.unshift(result.length);
        }

        return createMacroNode(tokenizer).add(result);
    }

    private AstNode createMacroNode(Tokenizer tokenizer) throws ParserException {
        AstNode returnNode = new AstNode(AstType.AST_RETURN).add(getExpression(tokenizer));
        AstNode nodeList = new AstNode(AstType.AST_NODELIST).add(returnNode);
        AstNode node = new AstNode(AstType.AST_MACRO).add(nodeList);
        return node;
    }

    private AstNode getTernaryExpression(Tokenizer tokenizer) throws ParserException {
        AstNode res = getLogicalOrExpression(tokenizer);
        while (tokenizer.next(Tokens.T_QUERY.getId()).isFound()) {
            AstNode node = new AstNode(AstType.AST_TERNARY);
            res = node.add(res, getExpression(tokenizer));
            if (tokenizer.next(Tokens.T_COLON.getId()).isFound()) {
                res.add(getExpression(tokenizer));
            }
        }
        return res;
    }

    private AstNode getLogicalOrExpression(Tokenizer tokenizer) throws ParserException {
        AstNode res = getLogicalAndExpression(tokenizer);
        while (tokenizer.next(Tokens.T_OR.getId()).isFound()) {
            AstNode node = new AstNode(AstType.AST_OR);
            res = node.add(res);
            res.add(getLogicalAndExpression(tokenizer));
        }
        return res;
    }

    private AstNode getLogicalAndExpression(Tokenizer tokenizer) throws ParserException {
        AstNode res = getBitwiseOrExpression(tokenizer);
        while (tokenizer.next(Tokens.T_AND.getId()).isFound()) {
            AstNode node = new AstNode(AstType.AST_AND);
            res = node.add(res);
            res.add(getBitwiseOrExpression(tokenizer));
        }
        return res;
    }

    private AstNode getBitwiseOrExpression(Tokenizer tokenizer) throws ParserException {
        AstNode res = getBitwiseXorExpression(tokenizer);
        while (tokenizer.next(Tokens.T_BOR.getId()).isFound()) {
            AstNode node = new AstNode(AstType.AST_BOR);
            res = node.add(res);
            res.add(getBitwiseXorExpression(tokenizer));
        }
        return res;
    }

    private AstNode getBitwiseXorExpression(Tokenizer tokenizer) throws ParserException {
        AstNode res = getBitwiseAndExpression(tokenizer);
        while (tokenizer.next(Tokens.T_BXOR.getId()).isFound()) {
            AstNode node = new AstNode(AstType.AST_BXOR);
            res = node.add(res);
            res.add(getBitwiseAndExpression(tokenizer));
        }
        return res;
    }

    private AstNode getBitwiseAndExpression(Tokenizer tokenizer) throws ParserException {
        AstNode res = getEqualityExpression(tokenizer);
        while (tokenizer.next(Tokens.T_BAND.getId()).isFound()) {
            AstNode node = new AstNode(AstType.AST_BAND);
            res = node.add(res);
            res.add(getEqualityExpression(tokenizer));
        }
        return res;
    }

    private AstNode getEqualityExpression(Tokenizer tokenizer) throws ParserException {
        AstNode res = getRelationalExpression(tokenizer);
        while (test(tokenizer, Tokens.T_EQ) || test(tokenizer, Tokens.T_NEQ)) {
            AstNode node;
            if (next(tokenizer, Tokens.T_EQ)) {
                node = new AstNode(AstType.AST_EQ);
            } else {
                next(tokenizer, Tokens.T_NEQ);// we needed to read next token for right work
                node = new AstNode(AstType.AST_NEQ);
            }
            res = node.add(res);
            res.add(getRelationalExpression(tokenizer));
        }
        return res;
    }

    private AstNode getRelationalExpression(Tokenizer tokenizer) throws ParserException {
        AstNode res = getAdditiveExpression(tokenizer);
        while (tokenizer.next(Tokens.T_LE.getId()).isFound()
            || tokenizer.next(Tokens.T_GE.getId()).isFound()
            || tokenizer.next(Tokens.T_LT.getId()).isFound()
            || tokenizer.next(Tokens.T_GT.getId()).isFound()
            ) {
            AstNode node;
            if (tokenizer.next(Tokens.T_LE.getId()).isFound()) {
                node = new AstNode(AstType.AST_LE);
            } else if (tokenizer.next(Tokens.T_GE.getId()).isFound()) {
                node = new AstNode(AstType.AST_GE);
            } else if (tokenizer.next(Tokens.T_LT.getId()).isFound()) {
                node = new AstNode(AstType.AST_LT);
            } else {
                node = new AstNode(AstType.AST_GT);
            }
            res = node.add(res);
            res.add(getAdditiveExpression(tokenizer));
        }
        return res;
    }

    private AstNode getAdditiveExpression(Tokenizer tokenizer) throws ParserException {
        AstNode res = getMultiplicativeExpression(tokenizer);
        while (tokenizer.next(Tokens.T_PLUS.getId()).isFound() || tokenizer.next(Tokens.T_MINUS.getId()).isFound()) {
            AstNode node;
            if (tokenizer.next(Tokens.T_PLUS.getId()).isFound()) {
                node = new AstNode(AstType.AST_ADD);
            } else {
                node = new AstNode(AstType.AST_SUB);
            }
            res = node.add(res);
            res.add(getMultiplicativeExpression(tokenizer));
        }
        return res;
    }

    private AstNode getMultiplicativeExpression(Tokenizer tokenizer) throws ParserException {
        AstNode res = getUnaryExpression(tokenizer);
        while (tokenizer.next(Tokens.T_STAR.getId()).isFound()
            || tokenizer.next(Tokens.T_SLASH.getId()).isFound()
            || tokenizer.next(Tokens.T_MOD.getId()).isFound()
            ) {
            AstNode node;
            if (tokenizer.next(Tokens.T_STAR.getId()).isFound()) {
                node = new AstNode(AstType.AST_MUL);
            } else if (tokenizer.next(Tokens.T_SLASH.getId()).isFound()) {
                node = new AstNode(AstType.AST_DIV);
            } else {
                node = new AstNode(AstType.AST_MOD);
            }
            res = node.add(res);
            res.add(getUnaryExpression(tokenizer));
        }
        return res;
    }

    private AstNode getUnaryExpression(Tokenizer tokenizer) throws ParserException {
        AstNode res = getMemberExpression(tokenizer);
        while (tokenizer.next(Tokens.T_NOT.getId()).isFound() || tokenizer.next(Tokens.T_MINUS.getId()).isFound()) {
            AstNode node;
            if (tokenizer.next(Tokens.T_NOT.getId()).isFound()) {
                node = new AstNode(AstType.AST_NOT);
            } else {
                node = new AstNode(AstType.AST_USUB);
            }
            res = node.add(res);
            res.add(getMemberExpression(tokenizer));
        }
        return res;
    }

    private AstNode getMemberExpression(Tokenizer tokenizer) throws ParserException {
        AstNode res = getPrimaryExpression(tokenizer);

        while (true) {
            if (tokenizer.next(Tokens.T_DOT.getId()).isFound()) {
                res = new AstNode(AstType.AST_PROP, res);
                if (tokenizer.test(Tokens.T_PROP.getId()) != null) {
                    UnexpectedToken(tokenizer, "IDENTIFIER");
                }
                res.addValue(tokenizer.next().first().getValue());
            } else if (tokenizer.next(Tokens.T_METHOD.getId()).isFound()) {
                res = new AstNode(AstType.AST_METHOD, res);
                if (tokenizer.test(Tokens.T_PROP.getId()) != null) {
                    UnexpectedToken(tokenizer, "IDENTIFIER");
                }
                res.addValue(tokenizer.next().first().getValue());
            } else if (tokenizer.next(Tokens.T_LBRACKET.getId()).isFound()) {
                res = new AstNode(AstType.AST_PROP, res);
                res.add(getExpression(tokenizer));
                if (tokenizer.test(Tokens.T_RBRACKET.getId()) != null) {
                    UnexpectedToken(tokenizer, "]");
                }
            } else if (tokenizer.next(Tokens.T_LPAREN.getId()).isFound()) {
                res = new AstNode(AstType.AST_CALL, res);
                if (tokenizer.next(Tokens.T_RPAREN.getId()) != null) {
                    continue;
                }
                do {
                    res.add(getExpression(tokenizer));
                } while (tokenizer.next(Tokens.T_COMMA.getId()).isFound());
                if (tokenizer.test(Tokens.T_RPAREN.getId()) != null) {
                    UnexpectedToken(tokenizer, "]");
                }
            } else {
                return res;
            }
        }
    }

    private AstNode getPrimaryExpression(Tokenizer tokenizer) throws ParserException {
        if (next(tokenizer, Tokens.T_NULL)) {
            return AstNode.forValue(null);
        } else if (next(tokenizer, Tokens.T_TRUE)) {
            return AstNode.forValue(true);
        } else if (next(tokenizer, Tokens.T_FALSE)) {
            return AstNode.forValue(false);
        } else if (next(tokenizer, Tokens.T_SLASH)) {
            return getRegexpLiteral(tokenizer);
        } else if (next(tokenizer, Tokens.T_LITERAL_START)) {
            return getLiteralStatement(tokenizer);
        } else if (test(tokenizer, Tokens.T_SQUOTE, Tokens.T_DQUOTE)) {
            return getStringLiteral(tokenizer);
        } else if (next(tokenizer, Tokens.T_LBRACKET)) {
            return getArrayExpression(tokenizer);
        } else if (next(tokenizer, Tokens.T_BLOCK_START)) {
            throw new NotImplementedException();
//            NodesStatement
        } else if (next(tokenizer, Tokens.T_THIS)) {
            return new AstNode(AstType.AST_THIS);
        } else if (next(tokenizer, Tokens.T_GLOBAL)) {
            return new AstNode(AstType.AST_GLOBAL);
        } else if (test(tokenizer, Tokens.T_INT)) {
            return AstNode.forValue(Integer.parseInt(tokenizer.next().first().getValue(), 10));
        } else if (test(tokenizer, Tokens.T_BIN)) {
            return AstNode.forValue(Integer.parseInt(tokenizer.next().first().getValue().substring(2), 2));
        } else if (test(tokenizer, Tokens.T_HEX)) {
            return AstNode.forValue(Integer.parseInt(tokenizer.next().first().getValue().substring(2), 16));
        } else if (test(tokenizer, Tokens.T_FLOAT)) {
            return AstNode.forValue(Float.parseFloat(tokenizer.next().first().getValue()));
        } else if (test(tokenizer, Tokens.T_REF)) {
            AstNode node = new AstNode(AstType.AST_REF);
            node.addValue(tokenizer.next().first().getValue());
            return node;
        } else if (next(tokenizer, Tokens.T_LPAREN)) {
            return getParenthesizedExpression(tokenizer);
        } else {
            UnexpectedToken(tokenizer, "EXPRESSION");
        }
        //todo remove this thrown
        throw new NotImplementedException();
    }

    private AstNode getArrayExpression(Tokenizer tokenizer) throws ParserException {
        throw new NotImplementedException();
//        int counter = 0, values = 0;
//
//        Token key, value;
//
//        AstNode result = new AstNode(AstType.AST_ARRAY);
//        String key;
//        AstNode value;
//        do {
//            while (tokenizer.next(Tokens.T_COMMA.getId()) != null) ;
//            if (tokenizer.next(Tokens.T_RBRACKET.getId()) != null) {
//                return result;
//            }
//            Token t = tokenizer.next(Tokens.T_PROP.getId(), Tokens.T_COLON.getId());
//            if (t != null) {
//                key = t.getValue();
//                value = getExpression(tokenizer);
//            } else if ((Utils_isString(key = Expression(ctx)) || Utils_isNumber(key)) && ctx.next(Tokens.T_COLON)) {
//                value = getExpression(tokenizer);
//                if (Utils_isString(key) && validInteger.test(key)) {
//                    key = parseInt(key, 10);
//                }
//                if (Utils_isNumber(key)) {
//                    key = Math.floor(key);
//                    if (key < counter) {
//                        key = String(key);
//                    } else {
//                        counter = key, key = String(counter++);
//                    }
//                }
//            } else {
//                value = key;
//                key = String(counter++);
//            }
//            if (values.hasOwnProperty(key)) {
//                result[values[key]] = value;
//            } else {
//                values[key] = result.length;
//                result.push(value, key);
//            }
//        } while (tokenizer.next(Tokens.T_COMMA.getId()) != null);
//
//        if (tokenizer.next(Tokens.T_RBRACKET.getId()) == null) {
//            UnexpectedToken(tokenizer, "]");
//        }
//        return result;
    }

    private AstNode getStringLiteral(Tokenizer tokenizer) throws ParserException {
        AstNode res = AstNode.forValue("");
        String start = tokenizer.next().first().getValue();
        TokenizerResult fragment;
        while ((fragment = tokenizer.next()) != null) {
            if (fragment.first().getTypes().contains(BaseTokens.T_EOF.getId())) {
                SyntaxError(tokenizer, "unterminated string literal");
            }
            if (StringUtils.equals(fragment.first().getValue(), start)) {
                break;
            } else if (StringUtils.equals(fragment.first().getValue(), "\\")) {
                res.addValue("\\").addValue(tokenizer.next().first().getValue());
            } else {
                res.addValue(fragment.first().getValue());
            }
        }
        return res.escaped();
    }

    private AstNode getLiteralStatement(Tokenizer tokenizer) throws ParserException {
        AstNode node = AstNode.forValue("");
        while (tokenizer.test(BaseTokens.T_EOF.getId(), Tokens.T_LITERAL_END.getId()) == null) {
            node.addValue(tokenizer.next().first().getValue());
        }
        if (!next(tokenizer, Tokens.T_LITERAL_END)) {
            UnexpectedToken(tokenizer, "%}}");
        }
        return node;
    }

    private AstNode getParenthesizedExpression(Tokenizer tokenizer) throws ParserException {
        AstNode node = getExpression(tokenizer);
        if (!next(tokenizer, Tokens.T_RPAREN)) {
            UnexpectedToken(tokenizer, ")");
        }
        return node;
    }

    private AstNode getRegexpLiteral(Tokenizer tokenizer) throws ParserException {
        String result = "";
        boolean inCharSet = false;

        for (; ; ) {
            if (tokenizer.test(BaseTokens.T_EOF.getId()).isFound()) {
                break;
            }
            if (tokenizer.test(Tokens.T_EOL.getId()).isFound()) {
                break;
            }
            if (!inCharSet && tokenizer.test(Tokens.T_SLASH.getId()).isFound()) {
                break;
            }
            if (tokenizer.next(Tokens.T_BACKSLASH.getId()).isFound()) {
                result += "\\";
            } else if (tokenizer.test(Tokens.T_LBRACKET.getId()).isFound()) {
                inCharSet = true;
            } else if (tokenizer.test(Tokens.T_RBRACKET.getId()).isFound()) {
                inCharSet = false;
            }
            result += tokenizer.next().first().getValue();
        }

        if (!tokenizer.next(Tokens.T_SLASH.getId()).isFound()) {
            SyntaxError(tokenizer, "unterminated regexp literal");
        }

        try {
            Pattern.compile(result);
        } catch (Exception e) {
            SyntaxError(tokenizer, e.getMessage());
        }

        int flagNum = 0;
        Token flagToken = tokenizer.next(Tokens.T_PROP.getId()).first();
        if (flagToken != null) {
            String flagStr = flagToken.getValue();

            if (!regexpFlagsPattern.matcher(flagStr).find()) {
                SyntaxError(tokenizer, "invalid flags supplied to regular expression '" + flagStr + "'");
            }

            if (flagStr.contains("g")) {
                flagNum |= AstRegexType.RE_GLOBAL.getId();
            }
            if (flagStr.contains("m")) {
                flagNum |= AstRegexType.RE_MULTILINE.getId();
            }
            if (flagStr.contains("i")) {
                flagNum |= AstRegexType.RE_IGNORECASE.getId();
            }
        }

        AstNode res = new AstNode(AstType.AST_REGEXP);
        res.addValue(result).addValue(flagNum);
        return res;
    }

    private boolean next(Tokenizer tokenizer, Tokens... tokens) {
        return tokenizer.next(toIntArr(tokens)).isFound();
    }

    private boolean test(Tokenizer tokenizer, Tokens... tokens) {
        return tokenizer.test(toIntArr(tokens)).isFound();
    }

    private int[] toIntArr(Tokens... tokens) {
        int[] ids = new int[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            ids[i] = tokens[i].getId();
        }
        return ids;
    }

    private void UnexpectedToken(Tokenizer tokenizer, String expected) throws ParserException {
        Token token = tokenizer.next().first();
        int line = tokenizer.getLineNumber(token.getIndex());
        String value = token.getTypes().contains(BaseTokens.T_EOF.getId()) ? "EOF" : token.getValue();
        String message = "unexpected '" + value + "', expected '" + expected + "'";
        throw new ParserException(message, tokenizer.getBaseURI(), line);
    }

    private void SyntaxError(Tokenizer tokenizer, String s) throws ParserException {
        Token token = tokenizer.next().first();
        int line = tokenizer.getLineNumber(token.getIndex());
        throw new ParserException(s, tokenizer.getBaseURI(), line);
    }
}

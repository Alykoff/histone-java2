package ru.histone.v2.parser;

import org.apache.commons.lang.NotImplementedException;
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
import ru.histone.v2.parser.tokenizer.TokenizerWrapper;
import ru.histone.v2.utils.ParserUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
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
        TokenizerWrapper wrapper = new TokenizerWrapper(tokenizer);
        AstNode result = getNodeList(wrapper);
        if (!wrapper.next(BaseTokens.T_EOF.getId()).isFound())
            UnexpectedToken(wrapper, "EOF");
//        Optimizer(template);
//        markReferences(template);
        return result;
    }

    private AstNode getNodeList(TokenizerWrapper wrapper) throws ParserException {
        AstNode result = new AstNode(AstType.AST_NODELIST);
        AstNode node;
        wrapper = new TokenizerWrapper(wrapper);
        for (; ; ) {
            node = getStatement(wrapper);
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

    private AstNode getStatement(TokenizerWrapper wrapper) throws ParserException {
        if (next(wrapper, Tokens.T_BLOCK_START)) {
            return getTemplateStatement(wrapper);
        }
        if (next(wrapper, Tokens.T_LITERAL_START)) {
            return getLiteralStatement(wrapper);
        }
        if (next(wrapper, Tokens.T_CMT_START)) {
            return getCommentStatement(wrapper);
        }
        if (!wrapper.test(BaseTokens.T_EOF.getId()).isFound()) {
            return AstNode.forValue(wrapper.next().first().getValue());
        }
        return new AstNode(T_BREAK);
    }

    private AstNode getCommentStatement(TokenizerWrapper wrapper) {
        throw new NotImplementedException();
    }

    private AstNode getTemplateStatement(TokenizerWrapper wrapper) throws ParserException {
        wrapper = new TokenizerWrapper(wrapper, Collections.singletonList(Tokens.T_SPACES.getId()));
        AstNode result;
        if (next(wrapper, Tokens.T_IF)) {
            result = getIfStatement(wrapper);
        } else if (next(wrapper, Tokens.T_FOR)) {
            result = getForStatement(wrapper);
        } else if (next(wrapper, Tokens.T_VAR)) {
            result = getVarStatement(wrapper);
        } else if (next(wrapper, Tokens.T_MACRO)) {
            result = getMacroExpression(wrapper);
        } else if (next(wrapper, Tokens.T_RETURN)) {
            result = getReturnStatement(wrapper);
        } else if (next(wrapper, Tokens.T_SUPRESS)) {
            result = getSupressStatement(wrapper);
        } else if (next(wrapper, Tokens.T_LISTEN)) {
            result = getListenStatement(wrapper, AstType.AST_LISTEN);
        } else if (next(wrapper, Tokens.T_TRIGGER)) {
            result = getListenStatement(wrapper, AstType.AST_TRIGGER);
        } else if (test(wrapper, Tokens.T_SLASH, Tokens.T_STATEMENT, Tokens.T_BLOCK_END)) {
            result = new AstNode(T_BREAK);
        } else if (test(wrapper, Tokens.T_STATEMENT)) {
            result = new AstNode(T_BREAK);
        } else {
            result = getExpressionStatement(wrapper);
        }
        return result;
    }

    private AstNode getExpressionStatement(TokenizerWrapper wrapper) throws ParserException {
        if (next(wrapper, Tokens.T_BLOCK_END)) {
            return new AstNode(T_NOP);
        }
        AstNode expression = getExpression(wrapper);
        if (!next(wrapper, Tokens.T_BLOCK_END)) {
            UnexpectedToken(wrapper, "}}");
        }
        return expression;
    }

    private AstNode getListenStatement(TokenizerWrapper wrapper, AstType astListen) {
        throw new NotImplementedException();
    }

    private AstNode getSupressStatement(TokenizerWrapper wrapper) {
        throw new NotImplementedException();
    }

    private AstNode getReturnStatement(TokenizerWrapper wrapper) {
        throw new NotImplementedException();
    }

    private AstNode getVarStatement(TokenizerWrapper wrapper) {
        throw new NotImplementedException();
    }

    private AstNode getForStatement(TokenizerWrapper wrapper) throws ParserException {
        final AstNode node = new AstNode(AstType.AST_FOR);
        final TokenizerResult exp1 = wrapper.next(Tokens.T_ID.getId());
        if (exp1.isFound()) {
            if (next(wrapper, Tokens.T_COLON)) {
                node.addValue(exp1.getTokens());
                final TokenizerResult exp2 = wrapper.next(Tokens.T_ID.getId());
                if (exp2.isFound()) {
                    node.addValue(exp2.getTokens());
                } else {
                    UnexpectedToken(wrapper, "IDENTIFIER");
                }
            } else {
                node.addValue(null);
                node.addValue(exp1.getTokens());
            }
        } else {
            node.addValue(null);
            node.addValue(null);
        }

        if (!next(wrapper, Tokens.T_IN)) {
            UnexpectedToken(wrapper, "in");
        }

        do {
            final AstNode node2 = getExpression(wrapper);
            if (!next(wrapper, Tokens.T_BLOCK_END)) {
                UnexpectedToken(wrapper, "}}");
            }
            node.add(getNodeList(wrapper), node2);
        } while (next(wrapper, Tokens.T_ELSEIF));

        if (next(wrapper, Tokens.T_ELSE)) {
            if (!next(wrapper, Tokens.T_BLOCK_END)) {
                UnexpectedToken(wrapper, "}}");
            }
            node.add(getNodeList(wrapper));
        }

        if (!next(wrapper, Tokens.T_SLASH, Tokens.T_FOR, Tokens.T_BLOCK_END)) {
            UnexpectedToken(wrapper, "{{/for}}");
        }
        return node;
    }

    private AstNode getIfStatement(TokenizerWrapper wrapper) throws ParserException {
        AstNode node = new AstNode(AstType.AST_IF);
        do {
            AstNode condition = getExpression(wrapper);
            if (!next(wrapper, Tokens.T_BLOCK_END)) {
                UnexpectedToken(wrapper, "}}");
            }
            node.add(getNodeList(wrapper), condition);
        } while (next(wrapper, Tokens.T_ELSEIF));

        if (next(wrapper, Tokens.T_ELSE)) {
            if (!next(wrapper, Tokens.T_BLOCK_END)) {
                UnexpectedToken(wrapper, "}}");
            }
            node.add(getNodeList(wrapper));
        }
        if (!next(wrapper, Tokens.T_SLASH, Tokens.T_IF, Tokens.T_BLOCK_END)) {
            UnexpectedToken(wrapper, "{{/if}}");
        }

        return node;
    }

    private AstNode getExpression(TokenizerWrapper wrapper) throws ParserException {
        if (test(wrapper, Tokens.T_ARROW) ||
                test(wrapper, Tokens.T_ID, Tokens.T_ARROW) ||
                test(wrapper, Tokens.T_LPAREN, Tokens.T_RPAREN) ||
                test(wrapper, Tokens.T_LPAREN, Tokens.T_ID, Tokens.T_COMMA) ||
                test(wrapper, Tokens.T_LPAREN, Tokens.T_ID, Tokens.T_RPAREN, Tokens.T_ARROW)) {
            return getMacroExpression(wrapper);
        }
        return getTernaryExpression(wrapper);
    }

    private AstNode getMacroExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode result = new AstNode(AstType.AST_NODELIST);

        TokenizerResult name = wrapper.next(Tokens.T_ID.getId());
        if (name.isFound()) {
            result.add(new AstNode(AstType.AST_NOP).addValue(name.first().getValue()));
        } else if (next(wrapper, Tokens.T_LPAREN)) {
            if (!test(wrapper, Tokens.T_RPAREN)) {
                do {
                    name = wrapper.next(Tokens.T_ID.getId());
                    if (name.isFound()) {
                        result.add(new AstNode(AstType.AST_NOP).addValue(name.first().getValue()));
                    } else {
                        UnexpectedToken(wrapper, "IDENTIFIER");
                    }
                } while (next(wrapper, Tokens.T_COMMA));
            }
            if (!next(wrapper, Tokens.T_RPAREN)) {
                UnexpectedToken(wrapper, ")");
            }
        }
        if (!next(wrapper, Tokens.T_ARROW)) {
            UnexpectedToken(wrapper, "=>");
        }
        if (result.getNodes().size() > 0) {
            //todo
//            result.unshift(result.length);
        }

        return createMacroNode(wrapper).add(result);
    }

    private AstNode createMacroNode(TokenizerWrapper wrapper) throws ParserException {
        AstNode returnNode = new AstNode(AstType.AST_RETURN).add(getExpression(wrapper));
        AstNode nodeList = new AstNode(AstType.AST_NODELIST).add(returnNode);
        AstNode node = new AstNode(AstType.AST_MACRO).add(nodeList);
        return node;
    }

    private AstNode getTernaryExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getLogicalOrExpression(wrapper);
        while (next(wrapper, Tokens.T_QUERY)) {
            AstNode node = new AstNode(AstType.AST_TERNARY);
            res = node.add(res, getExpression(wrapper));
            if (next(wrapper, Tokens.T_COLON)) {
                res.add(getExpression(wrapper));
            }
        }
        return res;
    }

    private AstNode getLogicalOrExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getLogicalAndExpression(wrapper);
        while (next(wrapper, Tokens.T_OR)) {
            AstNode node = new AstNode(AstType.AST_OR);
            res = node.add(res);
            res.add(getLogicalAndExpression(wrapper));
        }
        return res;
    }

    private AstNode getLogicalAndExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getBitwiseOrExpression(wrapper);
        while (next(wrapper, Tokens.T_AND)) {
            AstNode node = new AstNode(AstType.AST_AND);
            res = node.add(res);
            res.add(getBitwiseOrExpression(wrapper));
        }
        return res;
    }

    private AstNode getBitwiseOrExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getBitwiseXorExpression(wrapper);
        while (next(wrapper, Tokens.T_BOR)) {
            AstNode node = new AstNode(AstType.AST_BOR);
            res = node.add(res);
            res.add(getBitwiseXorExpression(wrapper));
        }
        return res;
    }

    private AstNode getBitwiseXorExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getBitwiseAndExpression(wrapper);
        while (next(wrapper, Tokens.T_BXOR)) {
            AstNode node = new AstNode(AstType.AST_BXOR);
            res = node.add(res);
            res.add(getBitwiseAndExpression(wrapper));
        }
        return res;
    }

    private AstNode getBitwiseAndExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getEqualityExpression(wrapper);
        while (next(wrapper, Tokens.T_BAND)) {
            AstNode node = new AstNode(AstType.AST_BAND);
            res = node.add(res);
            res.add(getEqualityExpression(wrapper));
        }
        return res;
    }

    private AstNode getEqualityExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getRelationalExpression(wrapper);
        while (test(wrapper, Tokens.T_EQ) || test(wrapper, Tokens.T_NEQ)) {
            AstNode node;
            if (next(wrapper, Tokens.T_EQ)) {
                node = new AstNode(AstType.AST_EQ);
            } else {
                next(wrapper, Tokens.T_NEQ);// we needed to read next token for right work
                node = new AstNode(AstType.AST_NEQ);
            }
            res = node.add(res);
            res.add(getRelationalExpression(wrapper));
        }
        return res;
    }

    private AstNode getRelationalExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getAdditiveExpression(wrapper);
        while (next(wrapper, Tokens.T_LE)
                || next(wrapper, Tokens.T_GE)
                || next(wrapper, Tokens.T_LT)
                || next(wrapper, Tokens.T_GT)
                ) {
            AstNode node;
            if (next(wrapper, Tokens.T_LE)) {
                node = new AstNode(AstType.AST_LE);
            } else if (next(wrapper, Tokens.T_GE)) {
                node = new AstNode(AstType.AST_GE);
            } else if (next(wrapper, Tokens.T_LT)) {
                node = new AstNode(AstType.AST_LT);
            } else {
                node = new AstNode(AstType.AST_GT);
            }
            res = node.add(res);
            res.add(getAdditiveExpression(wrapper));
        }
        return res;
    }

    private AstNode getAdditiveExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getMultiplicativeExpression(wrapper);
        while (next(wrapper, Tokens.T_PLUS) || next(wrapper, Tokens.T_MINUS)) {
            AstNode node;
            if (next(wrapper, Tokens.T_PLUS)) {
                node = new AstNode(AstType.AST_ADD);
            } else {
                node = new AstNode(AstType.AST_SUB);
            }
            res = node.add(res);
            res.add(getMultiplicativeExpression(wrapper));
        }
        return res;
    }

    private AstNode getMultiplicativeExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getUnaryExpression(wrapper);
        while (next(wrapper, Tokens.T_STAR)
                || next(wrapper, Tokens.T_SLASH)
                || next(wrapper, Tokens.T_MOD)
                ) {
            AstNode node;
            if (next(wrapper, Tokens.T_STAR)) {
                node = new AstNode(AstType.AST_MUL);
            } else if (next(wrapper, Tokens.T_SLASH)) {
                node = new AstNode(AstType.AST_DIV);
            } else {
                node = new AstNode(AstType.AST_MOD);
            }
            res = node.add(res);
            res.add(getUnaryExpression(wrapper));
        }
        return res;
    }

    private AstNode getUnaryExpression(TokenizerWrapper wrapper) throws ParserException {
        if (next(wrapper, Tokens.T_NOT)) {
            AstNode node = getUnaryExpression(wrapper);
            return new AstNode(AstType.AST_NOT).add(node);
        } else if (next(wrapper, Tokens.T_MINUS)) {
            AstNode node = getUnaryExpression(wrapper);
            return new AstNode(AstType.AST_USUB).add(node);
        } else {
            return getMemberExpression(wrapper);
        }
    }

    private AstNode getMemberExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = getPrimaryExpression(wrapper);

        while (true) {
            if (next(wrapper, Tokens.T_DOT)) {
                res = new AstNode(AstType.AST_PROP, res);
                if (wrapper.test(Tokens.T_PROP.getId()) != null) {
                    UnexpectedToken(wrapper, "IDENTIFIER");
                }
                res.addValue(wrapper.next().first().getValue());
            } else if (next(wrapper, Tokens.T_METHOD)) {
                res = new AstNode(AstType.AST_METHOD, res);
                if (wrapper.test(Tokens.T_PROP.getId()) != null) {
                    UnexpectedToken(wrapper, "IDENTIFIER");
                }
                res.addValue(wrapper.next().first().getValue());
            } else if (next(wrapper, Tokens.T_LBRACKET)) {
                res = new AstNode(AstType.AST_PROP, res);
                res.add(getExpression(wrapper));
                if (wrapper.test(Tokens.T_RBRACKET.getId()) != null) {
                    UnexpectedToken(wrapper, "]");
                }
            } else if (next(wrapper, Tokens.T_LPAREN)) {
                res = new AstNode(AstType.AST_CALL, res);
                if (wrapper.next(Tokens.T_RPAREN.getId()) != null) {
                    continue;
                }
                do {
                    res.add(getExpression(wrapper));
                } while (next(wrapper, Tokens.T_COMMA));
                if (wrapper.test(Tokens.T_RPAREN.getId()) != null) {
                    UnexpectedToken(wrapper, "]");
                }
            } else {
                return res;
            }
        }
    }

    private AstNode getPrimaryExpression(TokenizerWrapper wrapper) throws ParserException {
        if (next(wrapper, Tokens.T_NULL)) {
            return AstNode.forValue(null);
        } else if (next(wrapper, Tokens.T_TRUE)) {
            return AstNode.forValue(true);
        } else if (next(wrapper, Tokens.T_FALSE)) {
            return AstNode.forValue(false);
        } else if (next(wrapper, Tokens.T_SLASH)) {
            return getRegexpLiteral(wrapper);
        } else if (next(wrapper, Tokens.T_LITERAL_START)) {
            return getLiteralStatement(wrapper);
        } else if (test(wrapper, Tokens.T_SQUOTE)) {
            return getStringLiteral(wrapper);
        } else if (test(wrapper, Tokens.T_DQUOTE)) {
            return getStringLiteral(wrapper);
        } else if (next(wrapper, Tokens.T_LBRACKET)) {
            return getArrayExpression(wrapper);
        } else if (next(wrapper, Tokens.T_BLOCK_START)) {
            throw new NotImplementedException();
//            NodesStatement
        } else if (next(wrapper, Tokens.T_THIS)) {
            return new AstNode(AstType.AST_THIS);
        } else if (next(wrapper, Tokens.T_GLOBAL)) {
            return new AstNode(AstType.AST_GLOBAL);
        } else if (test(wrapper, Tokens.T_INT)) {
            return AstNode.forValue(Integer.parseInt(wrapper.next().first().getValue(), 10));
        } else if (test(wrapper, Tokens.T_BIN)) {
            return AstNode.forValue(Integer.parseInt(wrapper.next().first().getValue().substring(2), 2));
        } else if (test(wrapper, Tokens.T_HEX)) {
            return AstNode.forValue(Integer.parseInt(wrapper.next().first().getValue().substring(2), 16));
        } else if (test(wrapper, Tokens.T_FLOAT)) {
            return AstNode.forValue(Float.parseFloat(wrapper.next().first().getValue()));
        } else if (test(wrapper, Tokens.T_REF)) {
            AstNode node = new AstNode(AstType.AST_REF);
            node.addValue(wrapper.next().first().getValue());
            return node;
        } else if (next(wrapper, Tokens.T_LPAREN)) {
            return getParenthesizedExpression(wrapper);
        } else {
            UnexpectedToken(wrapper, "EXPRESSION");
        }
        //todo remove this thrown
        throw new NotImplementedException();
    }

    private AstNode getArrayExpression(TokenizerWrapper wrapper) throws ParserException {
        int counter = 0;

        Map<String, Object> values = new HashMap<>();

//        Token key, value;

        AstNode result = new AstNode(AstType.AST_ARRAY);
//        String key;
//        AstNode value;
        AstNode key;
        AstNode value;
        Map<String, AstNode> map = new LinkedHashMap<>();


        do {
            while (next(wrapper, Tokens.T_COMMA)) ;
            if (next(wrapper, Tokens.T_RBRACKET)) {
                return result;
            }
            TokenizerResult tokenRes = wrapper.next(Tokens.T_PROP.getId(), Tokens.T_COLON.getId());
            if (tokenRes.isFound()) {
                map.put(tokenRes.firstValue(), getExpression(wrapper));
            } else {
                key = getExpression(wrapper);
                Object val = key.getValues().get(0);
                if ((ParserUtils.isString(val) || ParserUtils.isNumber(val)) && next(wrapper, Tokens.T_COLON)) {
                    value = getExpression(wrapper);
                    Object mapKey = val;
                    if (ParserUtils.isString(val) && ParserUtils.isInt((String) val)) {
                        mapKey = Integer.valueOf((String) val); //todo check this
                    }
                    if (ParserUtils.isDouble((String) val)) {
                        Double d = Double.parseDouble((String) val);
                        if (d.intValue() < counter) {
                            mapKey = d.intValue() + "";
                        } else {
                            counter = d.intValue();
                            mapKey = (counter++) + "";
                        }
                    }
                    map.put(mapKey + "", value);
                } else {
                    map.put((counter++) + "", key);
                }
            }


//            if (t != null) {
//                key = t.getValue();
//                value = getExpression(wrapper);
//            } else if ((Utils_isString(key = Expression(ctx)) || Utils_isNumber(key)) && ctx.next(Tokens.T_COLON)) {
//                value = getExpression(wrapper);
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
        } while (next(wrapper, Tokens.T_COMMA));

        if (!next(wrapper, Tokens.T_RBRACKET)) {
            UnexpectedToken(wrapper, "]");
        }
        for (Map.Entry<String, AstNode> entry : map.entrySet()) {
            result.add(AstNode.forValue(entry.getKey())).add(entry.getValue());
        }

        return result;
    }

    private boolean isStringOrNumber(AstNode node) {
        Object value = node.getValues().get(0);

        if (!(value instanceof String)) {
            return false;
        }

        try {
            Integer.parseInt((String) value);
            return true;
        } catch (Exception ignore) {

        }

        try {
            Double.parseDouble((String) value);
            return true;
        } catch (Exception ignore) {

        }
        return true;
    }

    private AstNode getStringLiteral(TokenizerWrapper wrapper) throws ParserException {
        AstNode res = new AstNode(Integer.MIN_VALUE);
        String start = wrapper.next().first().getValue();
        TokenizerResult fragment;
        while ((fragment = wrapper.next()).isFound()) {
            if (fragment.first().getTypes().contains(BaseTokens.T_EOF.getId())) {
                SyntaxError(wrapper, "unterminated string literal");
            }
            if (StringUtils.equals(fragment.first().getValue(), start)) {
                break;
            } else if (StringUtils.equals(fragment.first().getValue(), "\\")) {
                res.addValue("\\").addValue(wrapper.next().first().getValue());
            } else {
                res.addValue(fragment.first().getValue());
            }
        }
        return res.escaped();
    }

    private AstNode getLiteralStatement(TokenizerWrapper wrapper) throws ParserException {
        wrapper = new TokenizerWrapper(wrapper);
        AstNode node = AstNode.forValue("");
        while (wrapper.test(BaseTokens.T_EOF.getId(), Tokens.T_LITERAL_END.getId()) == null) {
            node.addValue(wrapper.next().first().getValue());
        }
        if (!next(wrapper, Tokens.T_LITERAL_END)) {
            UnexpectedToken(wrapper, "%}}");
        }
        return node;
    }

    private AstNode getParenthesizedExpression(TokenizerWrapper wrapper) throws ParserException {
        AstNode node = getExpression(wrapper);
        if (!next(wrapper, Tokens.T_RPAREN)) {
            UnexpectedToken(wrapper, ")");
        }
        return node;
    }

    private AstNode getRegexpLiteral(TokenizerWrapper wrapper) throws ParserException {
        String result = "";
        boolean inCharSet = false;

        for (; ; ) {
            if (wrapper.test(BaseTokens.T_EOF.getId()).isFound()) {
                break;
            }
            if (test(wrapper, Tokens.T_EOL)) {
                break;
            }
            if (!inCharSet && test(wrapper, Tokens.T_SLASH)) {
                break;
            }
            if (next(wrapper, Tokens.T_BACKSLASH)) {
                result += "\\";
            } else if (test(wrapper, Tokens.T_LBRACKET)) {
                inCharSet = true;
            } else if (test(wrapper, Tokens.T_RBRACKET)) {
                inCharSet = false;
            }
            result += wrapper.next().first().getValue();
        }

        if (!next(wrapper, Tokens.T_SLASH)) {
            SyntaxError(wrapper, "unterminated regexp literal");
        }

        try {
            Pattern.compile(result);
        } catch (Exception e) {
            SyntaxError(wrapper, e.getMessage());
        }

        int flagNum = 0;
        Token flagToken = wrapper.next(Tokens.T_PROP.getId()).first();
        if (flagToken != null) {
            String flagStr = flagToken.getValue();

            if (!regexpFlagsPattern.matcher(flagStr).find()) {
                SyntaxError(wrapper, "invalid flags supplied to regular expression '" + flagStr + "'");
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

    private void UnexpectedToken(TokenizerWrapper wrapper, String expected) throws ParserException {
        Token token = wrapper.next().first();
        int line = wrapper.getLineNumber(token.getIndex());
        String value = token.getTypes().contains(BaseTokens.T_EOF.getId()) ? "EOF" : token.getValue();
        String message = "unexpected '" + value + "', expected '" + expected + "'";
        throw new ParserException(message, wrapper.getBaseURI(), line);
    }

    private void SyntaxError(TokenizerWrapper wrapper, String s) throws ParserException {
        Token token = wrapper.next().first();
        int line = wrapper.getLineNumber(token.getIndex());
        throw new ParserException(s, wrapper.getBaseURI(), line);
    }
}

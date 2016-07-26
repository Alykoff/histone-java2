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

package ru.histone.v2.parser.tokenizer;

import ru.histone.v2.expression.BaseExpression;
import ru.histone.v2.expression.Expression;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Alexey Nevinsky
 */
public final class ExpressionList {
    public static final List<Expression> VALUES = Collections.unmodifiableList(Arrays.asList(
            new BaseExpression("null\\b", Tokens.T_PROP, Tokens.T_NULL),
            new BaseExpression("true\\b", Tokens.T_PROP, Tokens.T_TRUE),
            new BaseExpression("false\\b", Tokens.T_PROP, Tokens.T_FALSE),
            new BaseExpression("if\\b", Tokens.T_PROP, Tokens.T_STATEMENT, Tokens.T_IF),
            new BaseExpression("in\\b", Tokens.T_PROP, Tokens.T_STATEMENT, Tokens.T_IN),
            new BaseExpression("as\\b", Tokens.T_PROP, Tokens.T_STATEMENT, Tokens.T_AS),
            new BaseExpression("for\\b", Tokens.T_PROP, Tokens.T_STATEMENT, Tokens.T_FOR),
            new BaseExpression("while\\b", Tokens.T_PROP, Tokens.T_STATEMENT, Tokens.T_WHILE),
            new BaseExpression("var\\b", Tokens.T_PROP, Tokens.T_STATEMENT, Tokens.T_VAR),
            new BaseExpression("else\\b", Tokens.T_PROP, Tokens.T_STATEMENT, Tokens.T_ELSE),
            new BaseExpression("macro\\b", Tokens.T_PROP, Tokens.T_STATEMENT, Tokens.T_MACRO),
            new BaseExpression("elseif\\b", Tokens.T_PROP, Tokens.T_STATEMENT, Tokens.T_ELSEIF),
            new BaseExpression("return\\b", Tokens.T_PROP, Tokens.T_STATEMENT, Tokens.T_RETURN),
            new BaseExpression("listen\\b", Tokens.T_PROP, Tokens.T_STATEMENT, Tokens.T_LISTEN),
            new BaseExpression("trigger\\b", Tokens.T_PROP, Tokens.T_STATEMENT, Tokens.T_TRIGGER),
            new BaseExpression("break\\b", Tokens.T_PROP, Tokens.T_STATEMENT, Tokens.T_BREAK),
            new BaseExpression("continue\\b", Tokens.T_PROP, Tokens.T_STATEMENT, Tokens.T_CONTINUE),
            new BaseExpression("0[bB][0-1]+", Tokens.T_BIN),
            new BaseExpression("0[xX][0-9A-Fa-f]+", Tokens.T_HEX),
            new BaseExpression("(?:[0-9]*\\.)?[0-9]+[eE][+-]?[0-9]+", Tokens.T_FLOAT),
            new BaseExpression("[0-9]*\\.[0-9]+", Tokens.T_FLOAT),
            new BaseExpression("[0-9]+", Tokens.T_INT),
            new BaseExpression("this\\b", Tokens.T_PROP, Tokens.T_REF, Tokens.T_THIS),
            new BaseExpression("self\\b", Tokens.T_PROP, Tokens.T_REF),
            new BaseExpression("global\\b", Tokens.T_PROP, Tokens.T_REF, Tokens.T_GLOBAL),
            new BaseExpression("[_\\$a-zA-Z][_\\$a-zA-Z0-9]*", Tokens.T_PROP, Tokens.T_REF, Tokens.T_ID),
            new BaseExpression("[\\x0A\\x0D]+", Tokens.T_PROP, Tokens.T_EOL),
            new BaseExpression("[\\x09\\x20]+", Tokens.T_SPACES),
            new BaseExpression("\\{\\{%", Tokens.T_LITERAL_START),
            new BaseExpression("\\{\\{#", Tokens.T_AST_START),
            new BaseExpression("#\\}\\}", Tokens.T_AST_END),
            new BaseExpression("%\\}\\}", Tokens.T_LITERAL_END),
            new BaseExpression("\\{\\{\\*", Tokens.T_CMT_START),
            new BaseExpression("\\*\\}\\}", Tokens.T_CMT_END),
            new BaseExpression("\\}\\}", Tokens.T_BLOCK_END),
            new BaseExpression("\\{\\{", Tokens.T_BLOCK_START),
            new BaseExpression("->", Tokens.T_METHOD),
            new BaseExpression("=>", Tokens.T_ARROW),
            new BaseExpression("!=", Tokens.T_NEQ),
            new BaseExpression("\\^", Tokens.T_BXOR),
            new BaseExpression("\\|\\|", Tokens.T_OR),
            new BaseExpression("\\|", Tokens.T_BOR),
            new BaseExpression("\\&&", Tokens.T_AND),
            new BaseExpression("\\&", Tokens.T_BAND),
            new BaseExpression("!", Tokens.T_NOT),
            new BaseExpression("@", Tokens.T_SUPPRESS),
            new BaseExpression("\"", Tokens.T_DQUOTE),
            new BaseExpression("'", Tokens.T_SQUOTE),
            new BaseExpression("=", Tokens.T_EQ),
            new BaseExpression("%", Tokens.T_MOD),
            new BaseExpression(":", Tokens.T_COLON),
            new BaseExpression(",", Tokens.T_COMMA),
            new BaseExpression("\\?", Tokens.T_QUERY),
            new BaseExpression("<=", Tokens.T_LE),
            new BaseExpression(">=", Tokens.T_GE),
            new BaseExpression("<", Tokens.T_LT),
            new BaseExpression(">", Tokens.T_GT),
            new BaseExpression("\\.", Tokens.T_DOT),
            new BaseExpression("-", Tokens.T_MINUS),
            new BaseExpression("\\+", Tokens.T_PLUS),
            new BaseExpression("\\*", Tokens.T_STAR),
            new BaseExpression("\\/", Tokens.T_SLASH),
            new BaseExpression("\\\\", Tokens.T_BACKSLASH),
            new BaseExpression("\\(", Tokens.T_LPAREN),
            new BaseExpression("\\)", Tokens.T_RPAREN),
            new BaseExpression("\\[", Tokens.T_LBRACKET),
            new BaseExpression("\\]", Tokens.T_RBRACKET)
    ));

    private ExpressionList() {
    }
}

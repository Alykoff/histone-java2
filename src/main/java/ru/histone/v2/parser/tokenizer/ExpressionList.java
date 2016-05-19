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
import static ru.histone.v2.parser.tokenizer.Tokens.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Alexey Nevinsky
 */
public final class ExpressionList {
    public static final List<Expression> VALUES = Collections.unmodifiableList(Arrays.asList(
            new BaseExpression("null\\b", T_PROP, T_NULL),
            new BaseExpression("true\\b", T_PROP, T_TRUE),
            new BaseExpression("false\\b", T_PROP, T_FALSE),
            new BaseExpression("if\\b", T_PROP, T_STATEMENT, T_IF),
            new BaseExpression("in\\b", T_PROP, T_STATEMENT, T_IN),
            new BaseExpression("as\\b", T_PROP, T_STATEMENT, T_AS),
            new BaseExpression("for\\b", T_PROP, T_STATEMENT, T_FOR),
            new BaseExpression("while\\b", T_PROP, T_STATEMENT, T_WHILE),
            new BaseExpression("var\\b", T_PROP, T_STATEMENT, T_VAR),
            new BaseExpression("else\\b", T_PROP, T_STATEMENT, T_ELSE),
            new BaseExpression("macro\\b", T_PROP, T_STATEMENT, T_MACRO),
            new BaseExpression("elseif\\b", T_PROP, T_STATEMENT, T_ELSEIF),
            new BaseExpression("return\\b", T_PROP, T_STATEMENT, T_RETURN),
            new BaseExpression("listen\\b", T_PROP, T_STATEMENT, T_LISTEN),
            new BaseExpression("trigger\\b", T_PROP, T_STATEMENT, T_TRIGGER),
            new BaseExpression("break\\b", T_PROP, T_STATEMENT, T_BREAK),
            new BaseExpression("continue\\b", T_PROP, T_STATEMENT, T_CONTINUE),
            new BaseExpression("0[bB][0-1]+", T_BIN),
            new BaseExpression("0[xX][0-9A-Fa-f]+", T_HEX),
            new BaseExpression("(?:[0-9]*\\.)?[0-9]+[eE][+-]?[0-9]+", T_FLOAT),
            new BaseExpression("[0-9]*\\.[0-9]+", T_FLOAT),
            new BaseExpression("[0-9]+", T_INT),
            new BaseExpression("this\\b", T_PROP, T_REF, T_THIS),
            new BaseExpression("self\\b", T_PROP, T_REF),
            new BaseExpression("global\\b", T_PROP, T_REF, T_GLOBAL),
            new BaseExpression("[_\\$a-zA-Z][_\\$a-zA-Z0-9]*", T_PROP, T_REF, T_ID),
            new BaseExpression("[\\x0A\\x0D]+", T_PROP, T_EOL),
            new BaseExpression("[\\x09\\x20]+", T_SPACES),
            new BaseExpression("\\{\\{%", T_LITERAL_START),
            new BaseExpression("\\{\\{#", T_AST_START),
            new BaseExpression("#\\}\\}", T_AST_END),
            new BaseExpression("%\\}\\}", T_LITERAL_END),
            new BaseExpression("\\{\\{\\*", T_CMT_START),
            new BaseExpression("\\*\\}\\}", T_CMT_END),
            new BaseExpression("\\}\\}", T_BLOCK_END),
            new BaseExpression("\\{\\{", T_BLOCK_START),
            new BaseExpression("->", T_METHOD),
            new BaseExpression("=>", T_ARROW),
            new BaseExpression("!=", T_NEQ),
            new BaseExpression("\\^", T_BXOR),
            new BaseExpression("\\|\\|", T_OR),
            new BaseExpression("\\|", T_BOR),
            new BaseExpression("\\&&", T_AND),
            new BaseExpression("\\&", T_BAND),
            new BaseExpression("!", T_NOT),
            new BaseExpression("@", T_SUPPRESS),
            new BaseExpression("\"", T_DQUOTE),
            new BaseExpression("'", T_SQUOTE),
            new BaseExpression("=", T_EQ),
            new BaseExpression("%", T_MOD),
            new BaseExpression(":", T_COLON),
            new BaseExpression(",", T_COMMA),
            new BaseExpression("\\?", T_QUERY),
            new BaseExpression("<=", T_LE),
            new BaseExpression(">=", T_GE),
            new BaseExpression("<", T_LT),
            new BaseExpression(">", T_GT),
            new BaseExpression("\\.", T_DOT),
            new BaseExpression("-", T_MINUS),
            new BaseExpression("\\+", T_PLUS),
            new BaseExpression("\\*", T_STAR),
            new BaseExpression("\\/", T_SLASH),
            new BaseExpression("\\\\", T_BACKSLASH),
            new BaseExpression("\\(", T_LPAREN),
            new BaseExpression("\\)", T_RPAREN),
            new BaseExpression("\\[", T_LBRACKET),
            new BaseExpression("\\]", T_RBRACKET)
    ));

    private ExpressionList() {
    }
}

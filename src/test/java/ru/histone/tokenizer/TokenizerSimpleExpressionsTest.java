/**
 *    Copyright 2013 MegaFon
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package ru.histone.tokenizer;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;
import ru.histone.HistoneTokensHolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TokenizerSimpleExpressionsTest  {
    private static final String MDC_TEST_NAME = "testCaseName";
    private TokenizerFactory tokenizerFactory;

    @Before
    public void init() {
        this.tokenizerFactory = new TokenizerFactory(HistoneTokensHolder.getTokens());
    }

	@Test
	public void correctIdentifiers() {

		String input;
		Token token;

		input = "{{abc ident ad34de ApPp90O forvar varfor varforvar}}";

		OldTokenizer tokenizer = tokenizerFactory.match(input);

		MDC.put(MDC_TEST_NAME, "{{");
		token = tokenizer.next();
		assertEquals(TokenType.T_BLOCK_START, token.getType());
		assertEquals("{{", token.getContent());

		MDC.put(MDC_TEST_NAME, "abc");
		token = tokenizer.next();
		assertEquals(TokenType.EXPR_IDENT, token.getType());
		assertEquals("abc", token.getContent());

		MDC.put(MDC_TEST_NAME, "ident");
		token = tokenizer.next();
		assertEquals(TokenType.EXPR_IDENT, token.getType());
		assertEquals("ident", token.getContent());

		MDC.put(MDC_TEST_NAME, "ad34de");
		token = tokenizer.next();
		assertEquals(TokenType.EXPR_IDENT, token.getType());
		assertEquals("ad34de", token.getContent());

		MDC.put(MDC_TEST_NAME, "ApPp90O");
		token = tokenizer.next();
		assertEquals(TokenType.EXPR_IDENT, token.getType());
		assertEquals("ApPp90O", token.getContent());

		MDC.put(MDC_TEST_NAME, "forvar");
		token = tokenizer.next();
		assertEquals(TokenType.EXPR_IDENT, token.getType());
		assertEquals("forvar", token.getContent());

		MDC.put(MDC_TEST_NAME, "varfor");
		token = tokenizer.next();
		assertEquals(TokenType.EXPR_IDENT, token.getType());
		assertEquals("varfor", token.getContent());

		MDC.put(MDC_TEST_NAME, "varforvar");
		token = tokenizer.next();
		assertEquals(TokenType.EXPR_IDENT, token.getType());
		assertEquals("varforvar", token.getContent());

		MDC.put(MDC_TEST_NAME, "}}");
		token = tokenizer.next();
		assertEquals(TokenType.T_BLOCK_END, token.getType());
		assertEquals("}}", token.getContent());

		MDC.put(MDC_TEST_NAME, "EOF");
		assertTrue(tokenizer.isNext(TokenType.T_EOF));
	}

	@Test
	public void brackets() {

		String input;
		Token token;

		input = "{{()[]}}";

		OldTokenizer tokenizer = tokenizerFactory.match(input);

		MDC.put(MDC_TEST_NAME, "{{");
		token = tokenizer.next();
		assertEquals(TokenType.T_BLOCK_START, token.getType());
		assertEquals("{{", token.getContent());

		MDC.put(MDC_TEST_NAME, "(");
		token = tokenizer.next();
		assertEquals(TokenType.EXPR_LPAREN, token.getType());
		assertEquals("(", token.getContent());

		MDC.put(MDC_TEST_NAME, ")");
		token = tokenizer.next();
		assertEquals(TokenType.EXPR_RPAREN, token.getType());
		assertEquals(")", token.getContent());

		MDC.put(MDC_TEST_NAME, "[");
		token = tokenizer.next();
		assertEquals(TokenType.EXPR_LBRACKET, token.getType());
		assertEquals("[", token.getContent());

		MDC.put(MDC_TEST_NAME, "]");
		token = tokenizer.next();
		assertEquals(TokenType.EXPR_RBRACKET, token.getType());
		assertEquals("]", token.getContent());

		MDC.put(MDC_TEST_NAME, "}}");
		token = tokenizer.next();
		assertEquals(TokenType.T_BLOCK_END, token.getType());
		assertEquals("}}", token.getContent());

		MDC.put(MDC_TEST_NAME, "EOF");
		assertTrue(tokenizer.isNext(TokenType.T_EOF));
	}

	@Test
	public void comparators() {

		String input;
		Token token;

		input = "{{< > <= >= <> isNot != is mod not or and}}";

		OldTokenizer tokenizer = tokenizerFactory.match(input);

		MDC.put(MDC_TEST_NAME, "{{");
		token = tokenizer.next();
		assertEquals(TokenType.T_BLOCK_START, token.getType());
		assertEquals("{{", token.getContent());

		token = tokenizer.next();
		assertEquals(TokenType.EXPR_LESS_THAN, token.getType());
		assertEquals("<", token.getContent());

		token = tokenizer.next();
		assertEquals(TokenType.EXPR_GREATER_THAN, token.getType());
		assertEquals(">", token.getContent());

		token = tokenizer.next();
		assertEquals(TokenType.EXPR_LESS_OR_EQUAL, token.getType());
		assertEquals("<=", token.getContent());

		token = tokenizer.next();
		assertEquals(TokenType.EXPR_GREATER_OR_EQUAL, token.getType());
		assertEquals(">=", token.getContent());

		token = tokenizer.next();
		assertEquals(TokenType.EXPR_NOT_EQUAL, token.getType());
		assertEquals("<>", token.getContent());

        token = tokenizer.next();
        assertEquals(TokenType.EXPR_NOT_EQUAL, token.getType());
        assertEquals("isNot", token.getContent());

        token = tokenizer.next();
        assertEquals(TokenType.EXPR_NOT_EQUAL, token.getType());
        assertEquals("!=", token.getContent());

		token = tokenizer.next();
		assertEquals(TokenType.EXPR_IS, token.getType());
		assertEquals("is", token.getContent());

		token = tokenizer.next();
		assertEquals(TokenType.EXPR_MOD, token.getType());
		assertEquals("mod", token.getContent());

		token = tokenizer.next();
		assertEquals(TokenType.EXPR_NOT, token.getType());
		assertEquals("not", token.getContent());

		token = tokenizer.next();
		assertEquals(TokenType.EXPR_OR, token.getType());
		assertEquals("or", token.getContent());

		token = tokenizer.next();
		assertEquals(TokenType.EXPR_AND, token.getType());
		assertEquals("and", token.getContent());

		MDC.put(MDC_TEST_NAME, "}}");
		token = tokenizer.next();
		assertEquals(TokenType.T_BLOCK_END, token.getType());
		assertEquals("}}", token.getContent());

		MDC.put(MDC_TEST_NAME, "EOF");
		assertTrue(tokenizer.isNext(TokenType.T_EOF));
	}

	@Test
	public void constants() {

		String input;
		Token token;

		input = "{{null false true}}";

		OldTokenizer tokenizer = tokenizerFactory.match(input);

		MDC.put(MDC_TEST_NAME, "{{");
		token = tokenizer.next();
		assertEquals(TokenType.T_BLOCK_START, token.getType());
		assertEquals("{{", token.getContent());

		token = tokenizer.next();
		assertEquals(TokenType.EXPR_NULL, token.getType());
		assertEquals("null", token.getContent());

		token = tokenizer.next();
		assertEquals(TokenType.EXPR_FALSE, token.getType());
		assertEquals("false", token.getContent());

		token = tokenizer.next();
		assertEquals(TokenType.EXPR_TRUE, token.getType());
		assertEquals("true", token.getContent());

		MDC.put(MDC_TEST_NAME, "}}");
		token = tokenizer.next();
		assertEquals(TokenType.T_BLOCK_END, token.getType());
		assertEquals("}}", token.getContent());

		MDC.put(MDC_TEST_NAME, "EOF");
		assertTrue(tokenizer.isNext(TokenType.T_EOF));
	}

	@Test
	public void stringsSingleQuote() {

		String input;
		Token token;

		input = "{{'test'}}";

		OldTokenizer tokenizer = tokenizerFactory.match(input);

		MDC.put(MDC_TEST_NAME, "{{");
		token = tokenizer.next();
		assertEquals(TokenType.T_BLOCK_START, token.getType());
		assertEquals("{{", token.getContent());

		token = tokenizer.next();
		assertEquals(TokenType.EXPR_STRING, token.getType());
		assertEquals("'test'", token.getContent());

		MDC.put(MDC_TEST_NAME, "}}");
		token = tokenizer.next();
		assertEquals(TokenType.T_BLOCK_END, token.getType());
		assertEquals("}}", token.getContent());

		MDC.put(MDC_TEST_NAME, "EOF");
		assertTrue(tokenizer.isNext(TokenType.T_EOF));
	}

	@Test
	public void stringsDoubleQuote() {

		String input;
		Token token;

		input = "{{\"test\"}}";

		OldTokenizer tokenizer = tokenizerFactory.match(input);

		MDC.put(MDC_TEST_NAME, "{{");
		token = tokenizer.next();
		assertEquals(TokenType.T_BLOCK_START, token.getType());
		assertEquals("{{", token.getContent());

		token = tokenizer.next();
		assertEquals(TokenType.EXPR_STRING, token.getType());
		assertEquals("\"test\"", token.getContent());

		MDC.put(MDC_TEST_NAME, "}}");
		token = tokenizer.next();
		assertEquals(TokenType.T_BLOCK_END, token.getType());
		assertEquals("}}", token.getContent());

		MDC.put(MDC_TEST_NAME, "EOF");
		assertTrue(tokenizer.isNext(TokenType.T_EOF));
	}

	@Test
	public void operators() {

		String input;
		Token token;

		input = "{{+-/*.,:=}}";

		OldTokenizer tokenizer = tokenizerFactory.match(input);

		MDC.put(MDC_TEST_NAME, "{{");
		token = tokenizer.next();
		assertEquals(TokenType.T_BLOCK_START, token.getType());
		assertEquals("{{", token.getContent());

		token = tokenizer.next();
		assertEquals(TokenType.EXPR_ADD, token.getType());
		assertEquals("+", token.getContent());

		token = tokenizer.next();
		assertEquals(TokenType.EXPR_SUB, token.getType());
		assertEquals("-", token.getContent());

		token = tokenizer.next();
		assertEquals(TokenType.EXPR_DIV, token.getType());
		assertEquals("/", token.getContent());

		token = tokenizer.next();
		assertEquals(TokenType.EXPR_MUL, token.getType());
		assertEquals("*", token.getContent());

		token = tokenizer.next();
		assertEquals(TokenType.EXPR_DOT, token.getType());
		assertEquals(".", token.getContent());

		token = tokenizer.next();
		assertEquals(TokenType.EXPR_COMMA, token.getType());
		assertEquals(",", token.getContent());

		token = tokenizer.next();
		assertEquals(TokenType.EXPR_COLON, token.getType());
		assertEquals(":", token.getContent());

		token = tokenizer.next();
		assertEquals(TokenType.EXPR_ASSIGN, token.getType());
		assertEquals("=", token.getContent());

		MDC.put(MDC_TEST_NAME, "}}");
		token = tokenizer.next();
		assertEquals(TokenType.T_BLOCK_END, token.getType());
		assertEquals("}}", token.getContent());

		MDC.put(MDC_TEST_NAME, "EOF");
		assertTrue(tokenizer.isNext(TokenType.T_EOF));
	}

	@Test
	public void constructions() {

		String input;
		Token token;

		input = "{{if elseif else for in var macro}}";

		OldTokenizer tokenizer = tokenizerFactory.match(input);

		MDC.put(MDC_TEST_NAME, "{{");
		token = tokenizer.next();
		assertEquals(TokenType.T_BLOCK_START, token.getType());
		assertEquals("{{", token.getContent());

		token = tokenizer.next();
		assertEquals(TokenType.EXPR_IF, token.getType());
		assertEquals("if", token.getContent());

		token = tokenizer.next();
		assertEquals(TokenType.EXPR_ELSEIF, token.getType());
		assertEquals("elseif", token.getContent());

		token = tokenizer.next();
		assertEquals(TokenType.EXPR_ELSE, token.getType());
		assertEquals("else", token.getContent());

		token = tokenizer.next();
		assertEquals(TokenType.EXPR_FOR, token.getType());
		assertEquals("for", token.getContent());

		token = tokenizer.next();
		assertEquals(TokenType.EXPR_IN, token.getType());
		assertEquals("in", token.getContent());

		token = tokenizer.next();
		assertEquals(TokenType.EXPR_VAR, token.getType());
		assertEquals("var", token.getContent());

		token = tokenizer.next();
		assertEquals(TokenType.EXPR_MACRO, token.getType());
		assertEquals("macro", token.getContent());

		MDC.put(MDC_TEST_NAME, "}}");
		token = tokenizer.next();
		assertEquals(TokenType.T_BLOCK_END, token.getType());
		assertEquals("}}", token.getContent());

		MDC.put(MDC_TEST_NAME, "EOF");
		assertTrue(tokenizer.isNext(TokenType.T_EOF));
	}
}

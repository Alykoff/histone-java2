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

import static org.junit.Assert.*;

public class TokenizerGeneralTest {
    private static final String MDC_TEST_NAME = "testCaseName";
    private TokenizerFactory tokenizerFactory;

    @Before
    public void init() {
        this.tokenizerFactory = new TokenizerFactory(HistoneTokensHolder.getTokens());
    }

    @Test
    public void generalContexts() {
        String input;
        Token token;

        input = "fragment1{{ident}}fragment2{{* comment *}}fragment3 ";
        /*
         * fragment1{{ident}}fragment2{{* comment *}}fragment3 < | | | | | | | | | | 1 1012 1719 28 31 40 43 52
         */

        OldTokenizer tokenizer = tokenizerFactory.match(input);

        MDC.put(MDC_TEST_NAME, "fragment1");
        assertTrue(tokenizer.isNext(TokenType.T_FRAGMENT));
        token = tokenizer.next(TokenType.T_FRAGMENT);
        assertNotNull(token);
        assertEquals(1, token.getPos());
        assertEquals("fragment1", token.getContent());

        MDC.put(MDC_TEST_NAME, "{{");
        assertTrue(tokenizer.isNext(TokenType.T_BLOCK_START));
        token = tokenizer.next(TokenType.T_BLOCK_START);
        assertNotNull(token);
        assertEquals(10, token.getPos());
        assertEquals("{{", token.getContent());

        MDC.put(MDC_TEST_NAME, "ident");
        assertTrue(tokenizer.isNext(TokenType.EXPR_IDENT));
        token = tokenizer.next(TokenType.EXPR_IDENT);
        assertNotNull(token);
        assertEquals(12, token.getPos());
        assertEquals("ident", token.getContent());

        MDC.put(MDC_TEST_NAME, "}}");
        assertTrue(tokenizer.isNext(TokenType.T_BLOCK_END));
        token = tokenizer.next(TokenType.T_BLOCK_END);
        assertNotNull(token);
        assertEquals(17, token.getPos());
        assertEquals("}}", token.getContent());

        MDC.put(MDC_TEST_NAME, "fragment2");
        assertTrue(tokenizer.isNext(TokenType.T_FRAGMENT));
        token = tokenizer.next(TokenType.T_FRAGMENT);
        assertNotNull(token);
        assertEquals(19, token.getPos());
        assertEquals("fragment2", token.getContent());

        MDC.put(MDC_TEST_NAME, "{{*");
        assertTrue(tokenizer.isNext(TokenType.T_COMMENT_START));
        token = tokenizer.next(TokenType.T_COMMENT_START);
        assertNotNull(token);
        assertEquals(28, token.getPos());
        assertEquals("{{*", token.getContent());

        MDC.put(MDC_TEST_NAME, "comment");
        assertTrue(tokenizer.isNext(TokenType.T_FRAGMENT));
        token = tokenizer.next(TokenType.T_FRAGMENT);
        assertNotNull(token);
        assertEquals(31, token.getPos());
        assertEquals(" comment ", token.getContent());

        MDC.put(MDC_TEST_NAME, "*}}");
        assertTrue(tokenizer.isNext(TokenType.T_COMMENT_END));
        token = tokenizer.next(TokenType.T_COMMENT_END);
        assertNotNull(token);
        assertEquals(40, token.getPos());
        assertEquals("*}}", token.getContent());

        MDC.put(MDC_TEST_NAME, "fragment3");
        assertTrue(tokenizer.isNext(TokenType.T_FRAGMENT));
        token = tokenizer.next(TokenType.T_FRAGMENT);
        assertNotNull(token);
        assertEquals(43, token.getPos());
        assertEquals("fragment3 ", token.getContent());

        MDC.put(MDC_TEST_NAME, "EOF");
        assertTrue(tokenizer.isNext(TokenType.T_EOF));
    }
    
    @Test
    public void fragmentsWithNewLines() {
        String input;
        Token token;

        input = "fragment1\n{{ident}}\nfragment2\n";
        /*
         * fragment1{{ident}}fragment2{{* comment *}}fragment3 < | | | | | | | | | | 1 1012 1719 28 31 40 43 52
         */

        OldTokenizer tokenizer = tokenizerFactory.match(input);

        MDC.put(MDC_TEST_NAME, "fragment1");
        assertTrue(tokenizer.isNext(TokenType.T_FRAGMENT));
        token = tokenizer.next(TokenType.T_FRAGMENT);
        assertNotNull(token);
        assertEquals(1, token.getPos());
        assertEquals("fragment1\n", token.getContent());

        MDC.put(MDC_TEST_NAME, "{{");
        assertTrue(tokenizer.isNext(TokenType.T_BLOCK_START));
        token = tokenizer.next(TokenType.T_BLOCK_START);
        assertNotNull(token);
        assertEquals(11, token.getPos());
        assertEquals("{{", token.getContent());

        MDC.put(MDC_TEST_NAME, "ident");
        assertTrue(tokenizer.isNext(TokenType.EXPR_IDENT));
        token = tokenizer.next(TokenType.EXPR_IDENT);
        assertNotNull(token);
        assertEquals(13, token.getPos());
        assertEquals("ident", token.getContent());

        MDC.put(MDC_TEST_NAME, "}}");
        assertTrue(tokenizer.isNext(TokenType.T_BLOCK_END));
        token = tokenizer.next(TokenType.T_BLOCK_END);
        assertNotNull(token);
        assertEquals(18, token.getPos());
        assertEquals("}}", token.getContent());

        MDC.put(MDC_TEST_NAME, "fragment2");
        assertTrue(tokenizer.isNext(TokenType.T_FRAGMENT));
        token = tokenizer.next(TokenType.T_FRAGMENT);
        assertNotNull(token);
        assertEquals(20, token.getPos());
        assertEquals("\nfragment2\n", token.getContent());


        MDC.put(MDC_TEST_NAME, "EOF");
        assertTrue(tokenizer.isNext(TokenType.T_EOF));
    }

    @Test
    public void generalContextsWOIsNextCalls() {
        String input;
        Token token;

        input = "fragment1{{ident}}fragment2";
        /*
         * fragment1{{ident}}fragment2{{* comment *}}fragment3 < | | | | | | | | | | 1 1012 1719 28 31 40 43 52
         */

        OldTokenizer tokenizer = tokenizerFactory.match(input);

        MDC.put(MDC_TEST_NAME, "fragment1");
        token = tokenizer.next(TokenType.T_FRAGMENT);
        assertNotNull(token);
        assertEquals(1, token.getPos());
        assertEquals("fragment1", token.getContent());

        MDC.put(MDC_TEST_NAME, "{{");
        token = tokenizer.next(TokenType.T_BLOCK_START);
        assertNotNull(token);
        assertEquals(10, token.getPos());
        assertEquals("{{", token.getContent());

        MDC.put(MDC_TEST_NAME, "ident");
        token = tokenizer.next(TokenType.EXPR_IDENT);
        assertNotNull(token);
        assertEquals(12, token.getPos());
        assertEquals("ident", token.getContent());

        MDC.put(MDC_TEST_NAME, "}}");
        token = tokenizer.next(TokenType.T_BLOCK_END);
        assertNotNull(token);
        assertEquals(17, token.getPos());
        assertEquals("}}", token.getContent());

        MDC.put(MDC_TEST_NAME, "fragment2");
        token = tokenizer.next(TokenType.T_FRAGMENT);
        assertNotNull(token);
        assertEquals(19, token.getPos());
        assertEquals("fragment2", token.getContent());

        MDC.put(MDC_TEST_NAME, "EOF");
        assertTrue(tokenizer.isNext(TokenType.T_EOF));
    }

    @Test
    public void generalContextsNoArgNextCalls() {
        String input;
        Token token;

        input = "fragment1{{ident}}fragment2";

        OldTokenizer tokenizer = tokenizerFactory.match(input);

        MDC.put(MDC_TEST_NAME, "fragment1");
        token = tokenizer.next();
        assertNotNull(token);
        assertEquals(TokenType.T_FRAGMENT, token.getType());
        assertEquals(1, token.getPos());
        assertEquals("fragment1", token.getContent());

        MDC.put(MDC_TEST_NAME, "{{");
        token = tokenizer.next();
        assertNotNull(token);
        assertEquals(TokenType.T_BLOCK_START, token.getType());
        assertEquals(10, token.getPos());
        assertEquals("{{", token.getContent());

        MDC.put(MDC_TEST_NAME, "ident");
        token = tokenizer.next();
        assertNotNull(token);
        assertEquals(TokenType.EXPR_IDENT, token.getType());
        assertEquals(12, token.getPos());
        assertEquals("ident", token.getContent());

        MDC.put(MDC_TEST_NAME, "}}");
        token = tokenizer.next();
        assertNotNull(token);
        assertEquals(TokenType.T_BLOCK_END, token.getType());
        assertEquals(17, token.getPos());
        assertEquals("}}", token.getContent());

        MDC.put(MDC_TEST_NAME, "fragment2");
        token = tokenizer.next();
        assertNotNull(token);
        assertEquals(TokenType.T_FRAGMENT, token.getType());
        assertEquals(19, token.getPos());
        assertEquals("fragment2", token.getContent());

        MDC.put(MDC_TEST_NAME, "EOF");
        assertTrue(tokenizer.isNext(TokenType.T_EOF));
    }
    
    @Test
    public void test(){
        String input;
//        Token token;

        input = "{{var}}";

        OldTokenizer tokenizer = tokenizerFactory.match(input);
        
        
        tokenizer.next(TokenType.T_BLOCK_START);
        tokenizer.next(TokenType.EXPR_VAR);
        tokenizer.next(TokenType.EXPR_IDENT);
        
    }

}

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.util.Collections.unmodifiableMap;

/**
 * Constructs new tokenizer using List of tokens definitions
 */
public final class TokenizerFactory {
    private static final Logger log = LoggerFactory.getLogger(TokenizerFactory.class);

    private final Map<TokenContext, List<TokenDef>> tokens;
    private final Map<TokenContext, StringBuilder> regexpList;

    /**
     * Constructs tokenizer factory using list of tokens definitions<br/>
     * Tokens should be sorted in their 'importance' order
     *
     * @param tokens list of tokens definitions
     */
    public TokenizerFactory(List<TokenDef> tokens) {
        Map<TokenContext, List<TokenDef>> tokensLocal = new HashMap<TokenContext, List<TokenDef>>();
        Map<TokenContext, StringBuilder> regexpsLocal = new HashMap<TokenContext, StringBuilder>();

        final char t1 = '(';
        final char[] t2 = new char[]{')', '|'};

        for (TokenDef token : tokens) {
            Collection<TokenContext> contexts = token.getContexts();

            for (TokenContext context : contexts) {
                List<TokenDef> tokenDefs = tokensLocal.get(context);
                if (tokenDefs == null) {
                    tokenDefs = new ArrayList<TokenDef>();
                    tokensLocal.put(context, tokenDefs);
                }
                tokenDefs.add(token);

                StringBuilder regexpBuilder = regexpsLocal.get(context);
                if (regexpBuilder == null) {
                    regexpBuilder = new StringBuilder();
                    regexpsLocal.put(context, regexpBuilder);
                }
                regexpBuilder.append(t1).append(token.getRegexp()).append(t2);
            }
        }

        this.tokens = unmodifiableMap(tokensLocal);
        this.regexpList = unmodifiableMap(regexpsLocal);
    }

    /**
     * Constructs tokenizer for specified input sequence
     */
    public OldTokenizer match(CharSequence input) {
        return match(input, TokenContext.TEMPLATE);
    }

    /**
     * Constructs tokenizer for specified input sequence and starting tokens context
     */
    public OldTokenizer match(CharSequence input, TokenContext startContext) {
        log.debug("tokenize(): input={}, currentContext={}", new Object[]{input, startContext});
        if (startContext == TokenContext.NONE) {
            throw new IllegalArgumentException("Start context undefined");
        }
        OldTokenizer tokenizer = new OldTokenizer(tokens, regexpList);
        return tokenizer.tokenize(input, startContext);
    }

}

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.unmodifiableMap;

/**
 * Histone template tokenizer<br/>
 * OldTokenizer is based on regular expressions and returns tokens one by one. All tokens are configured in {@link ru.histone.HistoneTokensHolder}
 */
public class OldTokenizer {
    private static final Logger log = LoggerFactory.getLogger(OldTokenizer.class);
    private final Map<TokenContext, List<TokenDef>> tokens;
    private final Map<TokenContext, StringBuilder> regexpList;
    private CharSequence input;
    private int inputOffset = 0;
    private TokenContext currentContext = TokenContext.NONE;
    private OldToken currentToken = null;
    private OldToken tokenBuffer = null;
    private TokenDef tokenDefBuffer = null;
    private Map<TokenContext, Matcher> matchers = new HashMap<TokenContext, Matcher>();
    private TokenTransitionCallback.NestedLevelHolder nestLevel = new TokenTransitionCallback.NestedLevelHolder();

    /**
     * Constructs new tokenizer instance using specified tokens definitions and their regexps<br/>
     * It's important, that order of tokens and their regexps should be the same in {@code tokens} and {@code regexps} arguments
     *
     * @param tokens  map of tokens, separated by their token context
     * @param regexps map of tokens regexps, separaed by their token context
     */
    public OldTokenizer(Map<TokenContext, List<TokenDef>> tokens, Map<TokenContext, StringBuilder> regexps) {
        this.tokens = unmodifiableMap(tokens == null ? new HashMap<TokenContext, List<TokenDef>>() : tokens);
        this.regexpList = unmodifiableMap(regexps == null ? new HashMap<TokenContext, StringBuilder>() : regexps);
    }

    /**
     * Start tokenization of input character sequence using specified token context as current active
     *
     * @param input        input character sequence
     * @param startContext current active token context
     * @return current object instance
     */
    public OldTokenizer tokenize(CharSequence input, TokenContext startContext) {
        log.debug("tokenize(): input={}, currentContext={}", new Object[]{input, currentContext});
        this.currentContext = startContext;
        this.input = input;

        for (TokenContext context : tokens.keySet()) {
            StringBuilder sb = regexpList.get(context);
            Pattern pattern = Pattern.compile(sb.substring(0, sb.length() - 1));
            matchers.put(context, pattern.matcher(input));
        }

        return this;
    }

    /**
     * Return current token context
     *
     * @return current token context
     */
    public TokenContext getCurrentContext() {
        log.trace("getCurrentContext(): currentContext={}", currentContext);
        return currentContext;
    }

    /**
     * Check next token against specified token type<br/>
     *
     * @param type token identificator
     * @return true if next token is of specified token type, false otherwise
     */
    public boolean isNext(TokenType type) {
        log.trace("isNext()>>: type={}", type);
        if (currentToken == null) {
            currentToken = getNextToken();
            log.trace("isNext(): new currentToken={}", currentToken);
        }
        log.trace("isNext()<<: type={}, currentToken={}", new Object[]{type, currentToken});

        return currentToken.getType() == type;
    }

    /**
     * Return next token from input sequence
     *
     * @return next token
     */
    public OldToken next() {
        return next(null);
    }

    /**
     * Return next token, only if it's type of specified<br/>
     * If next token has same type as specified, then token cursor shifts and token will be returned<br/>
     * If next token has different token, then null will be returned and token cursor will keep it's position
     *
     * @param type token type
     * @return token if token type is the same, null in other case
     */
    public OldToken next(TokenType type) {
        log.trace("next()>>: type={}, currentToken={}", new Object[]{type, currentToken});
        OldToken result = null;
        if (type == null || isNext(type)) {
            if (currentToken == null) {
                log.trace("next(): new1 currentToken={}", currentToken);
                currentToken = getNextToken();
            }
            result = new OldToken(currentToken);
            currentToken = getNextToken();
            log.trace("next(): new2 currentToken={}", currentToken);
        }
        log.trace("next()<<: result={}", result);
        return result;
    }

    /**
     * Switch current token context to new one
     *
     * @param context new token context
     */
    private void switchContext(TokenContext context) {
        log.trace("switchContext(): oldContext={}, newContext={}", new Object[]{this.currentContext, context});
        this.currentContext = context;
    }

    // Implementation functions
    private OldToken getNextToken() {
        OldToken result = null;

        log.trace("getNextToken()>>: tokenBuffer={}, tokenDefBuffer={}, inputOffset={}, input.length={}", new Object[]{tokenBuffer, tokenDefBuffer, inputOffset, input.length()});

        if (tokenBuffer != null) {
            log.trace("getNextToken(): tokenBuffer not empty, returning it's content");
            result = tokenBuffer;

            TokenContext transition = tokenDefBuffer.getTransitionCallback().getTransition(nestLevel);
            if (transition != null) {
                switchContext(transition);
            }

            tokenBuffer = null;
            tokenDefBuffer = null;
        } else if (inputOffset == input.length()) {
            log.trace("getNextToken(): end of file reached");
            result = OldToken.EOF_TOKEN;
        } else {
            log.trace("getNextToken(): searching for more tokens via regexp");
            Matcher m = getMatcher(getCurrentContext());

            if (m.find(inputOffset)) {
                log.trace("getNextToken(): m.group()={}, m.start()={}, m.end()={}, m.groupCount()={}", new Object[]{m.group(), m.start(), m.end(), m.groupCount()});
                for (int i = 1; i <= m.groupCount(); i++) {
                    if (m.group(0).equals(m.group(i))) {
                        TokenDef def = tokens.get(getCurrentContext()).get(i - 1);
                        tokenDefBuffer = def;
                        log.trace("getNextToken(): def={}", def);

                        if (m.start() - inputOffset > 0) {
                            // fragment
                            result = new OldToken(TokenType.T_FRAGMENT, inputOffset + 1, input.subSequence(inputOffset, m.start()).toString());
                            tokenBuffer = new OldToken(def.getType(), m.start() + 1, m.group(0));
                        } else {

                            if (def.getKind() == TokenKind.TOKEN) {
                                result = new OldToken(def.getType(), m.start() + 1, m.group(0));
                            } else if (def.getKind() == TokenKind.LITERAL) {
                                result = new OldToken(def.getType(), m.start() + 1, m.group(0));
                            } else if (def.getKind() == TokenKind.IGNORE) {
                                inputOffset = m.end();
                                result = getNextToken();
                            }

                            TokenContext transition = def.getTransitionCallback().getTransition(nestLevel);
                            if (transition != null) {
                                switchContext(transition);
                            }
                        }
                        inputOffset = m.end();

                        // break from for loop
                        break;
                    }
                }
            } else {
                result = new OldToken(TokenType.T_FRAGMENT, inputOffset + 1, input.subSequence(inputOffset, input.length()).toString());
                inputOffset = input.length();
            }
        }

        log.debug("getNextToken(): result={}, currentToken={}, currentContext={}",new Object[]{result,this.currentToken,this.currentContext});

        log.trace("getNextToken()<<: result={}", new Object[]{result});
        return result;
    }

    private Matcher getMatcher(TokenContext currentContext) {
        return matchers.get(currentContext);
    }

//	public void reset() {
//		log.debug("reset()");
//		input = null;
//		inputOffset = 0;
//		currentToken = null;
//		tokenBuffer = null;
//		tokenDefBuffer = null;
//
//		matchers.clear();
//	}

    public int getLineNumber() {
        int lineNumber = 1;
        int offset = inputOffset - 1;
        for (; offset > 0; offset--) {
            int code = input.charAt(offset);
            if (code == '\n' || code == '\r' || code == '\f') {
                lineNumber++;
            }
        }
        return lineNumber;
    }

    public int getColumnNumber() {
        int columnNumber = 1;
        int offset = inputOffset - 1;
        for (; offset > 0; offset--) {
            int code = input.charAt(offset);
            if (code == '\n' || code == '\r' || code == '\f') {
                columnNumber = 1;
            } else {
                columnNumber++;
            }
        }
        return columnNumber;
    }

    public String getInput() {
        return input.toString();
    }
}

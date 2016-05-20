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

import org.apache.commons.lang3.ArrayUtils;
import ru.histone.v2.expression.Expression;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static ru.histone.v2.parser.tokenizer.Tokens.T_EOF;
import static ru.histone.v2.parser.tokenizer.Tokens.T_ERROR;

/**
 * Histone template tokenizer<br/>
 * Tokenizer is based on regular expressions and returns tokens one by one. All tokens are configured in {@link ru.histone.v2.parser.tokenizer.Tokenizer}
 *
 * @author Alexey Nevinsky
 */
public class Tokenizer {
    private final List<Expression> expressions;

    private final String input;
    private String baseURI;
    private final Matcher matcher;
    private final int inputLen;
    private int lastIndex = 0;

    private Queue<Token> buffer;

    public Tokenizer(String input, String baseURI, List<Expression> expressions) {
        this.expressions = new ArrayList<>(expressions.size());
        this.expressions.addAll(expressions);

        buffer = new LinkedList<>();
        this.input = input;
        this.baseURI = baseURI;
        this.inputLen = input.length();
        Pattern mainPattern = Pattern.compile(expressions.stream().map(Expression::getExpression).collect(Collectors.joining("|")));
        matcher = mainPattern.matcher(input);
    }

    public String getBaseURI() {
        return baseURI;
    }


    public Tokenizer setBaseURI(String baseURI) {
        this.baseURI = baseURI;
        return this;
    }

    public void readTokenToBuffer() {
        int checkIndex = this.inputLen;

        if (lastIndex >= checkIndex) {
            // return T_EOF if we reached end of file
            buffer.add(new Token(T_EOF.getId(), checkIndex));
            return;
        }

        if (matcher.find(lastIndex)) {
            if (matcher.start() > lastIndex) {
                buffer.add(new Token(input.substring(lastIndex, matcher.start()), T_ERROR.getId(), lastIndex));
            }

            int i = 1;
            while (i < matcher.groupCount() && matcher.group(i) == null) {
                i++;
            }

            buffer.add(new Token(matcher.group(i), expressions.get(i - 1).getIds(), lastIndex));
            lastIndex = matcher.end();
        } else {
            // return T_ERROR token in case if we couldn't match anything
            buffer.add(new Token(input.substring(lastIndex), T_ERROR.getId(), lastIndex));
            lastIndex = checkIndex;
        }
    }

    public Token getTokenFromBuffer(int offset, List<Integer> ignored) {
        int toRead = offset - buffer.size() + 1;
        while (toRead-- > 0) {
            readTokenToBuffer();
        }

        Token token = ((LinkedList<Token>) buffer).get(offset);
        if (ignored.size() > 0 && compareToken(token, ignored)) {
            Token res = new Token(token.getValue(), token.getTypes(), token.getIndex());
            res.setIsIgnored(true);
            return res;
        } else {
            return token;
        }
    }

    private TokenizerResult getTokenA(boolean consume, List<Integer> ignored) {
        int offset = 0;
        Token token;
        if (consume) {
            do {
                token = getTokenFromBuffer(0, ignored);
                buffer.remove();
            } while (token.isIgnored());
        } else {
            do {
                token = getTokenFromBuffer(offset++, ignored);
            } while (token.isIgnored());
        }
        return new TokenizerResult(token);
    }

    private TokenizerResult getTokenB(boolean consume, List<Integer> ignored, Integer... selector) {
        int count = 0, index = 0;
        List<Token> result = new ArrayList<>();

        do {
            Token token = getTokenFromBuffer(count++, ignored);
            if (!token.isIgnored() && compareToken(token, selector[index])) {
                result.add(token);
                index++;
            } else if (!token.isIgnored()) {
                return new TokenizerResult(false);
            }
        } while (index < selector.length);

        if (!consume) {
            return new TokenizerResult(true);
        }

        while (count-- > 0) {
            buffer.remove();
        }

        return new TokenizerResult(result);
    }

    private boolean compareToken(Token token, List<Integer> selectors) {
        Integer[] arr = new Integer[selectors.size()];
        return compareToken(token, selectors.toArray(arr));
    }

    private boolean compareToken(Token token, Integer... selectors) {
        if (selectors == null || token == null) {
            return false;
        }
        for (int selector : selectors) {
            if (token.getTypes().contains(selector)) {
                return true;
            }
        }

        return false;
    }

    public TokenizerResult next(List<Integer> ignored, Integer... selector) {
        if (ArrayUtils.isNotEmpty(selector)) {
            return getTokenB(true, ignored, selector);
        }
        return getTokenA(true, ignored);
    }

    public TokenizerResult test(List<Integer> ignored, Integer... selector) {
        if (ArrayUtils.isNotEmpty(selector)) {
            return getTokenB(false, ignored, selector);
        }
        return getTokenA(false, ignored);
    }

    public int getLineNumber(long index) {
        int pos = -1;
        int lineNumber = 1;
        while (++pos < index) {
            char code = input.charAt(pos);
            if (code == '\r' || code == '\n') {
                lineNumber++;
            }
        }

        return lineNumber;
    }
}

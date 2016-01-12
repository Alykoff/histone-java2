package ru.histone.v2.parser.tokenizer;

import org.apache.commons.lang.ArrayUtils;
import ru.histone.expression.Expression;
import ru.histone.tokenizer.BaseTokens;
import ru.histone.tokenizer.Token;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by alexey.nevinsky on 21.12.2015.
 */
public class Tokenizer {
    private final List<Expression> expressions;

    private final String input;
    private final String baseURI;
    private final Pattern mainPattern;
    private final Matcher matcher;
    private final int inputLen;
    private int lastIndex = 0;

    private Queue<Token> buffer;
    private List<Integer> ignored;

    private int lastTokenId = 1;
//    public Tokenizer(int capacity) {
//        expressions = new ArrayList<Expression>(capacity);
//    }

    public Tokenizer(String input, String baseURI, List<Expression> expressions) {
        this.expressions = new ArrayList<>(expressions.size());
        this.expressions.addAll(expressions);

        buffer = new LinkedList<>();
        ignored = new ArrayList<>();
        this.input = input;
        this.baseURI = baseURI;
        this.inputLen = input.length();
        mainPattern = Pattern.compile(expressions.stream().map(Expression::getExpression).collect(Collectors.joining("|")));
        matcher = mainPattern.matcher(input);
    }

    public void setIgnored(List<Integer> ignored) {
        this.ignored = ignored;
    }

    public String getBaseURI() {
        return baseURI;
    }

    public void readTokenToBuffer() {
        int checkIndex = this.inputLen;

        if (lastIndex >= checkIndex) {
            // return T_EOF if we reached end of file
            buffer.add(new Token(BaseTokens.T_EOF.getId(), checkIndex));
            return;
        }

        if (matcher.find(lastIndex)) {
            if (matcher.start() > lastIndex) {
                buffer.add(new Token(input.substring(lastIndex, matcher.start()), BaseTokens.T_ERROR.getId(), lastIndex));
            }

            int i = 1;
            while (i < matcher.groupCount() - 1 && matcher.group(i) == null) {
                i++;
            }

            buffer.add(new Token(matcher.group(i), expressions.get(i - 1).getIds(), lastIndex));
            lastIndex = matcher.end();
        } else {
            // return T_ERROR token in case if we couldn't match anything
            buffer.add(new Token(input.substring(lastIndex), BaseTokens.T_ERROR.getId(), lastIndex));
            lastIndex = checkIndex;
        }
    }

    public Token getTokenFromBuffer(int offset) {
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

    private TokenizerResult getTokenA(boolean consume) {
        int offset = 0;
        Token token;
        if (consume) {
            do {
                token = getTokenFromBuffer(0);
                buffer.remove();
            } while (token.isIgnored());
        } else {
            do {
                token = getTokenFromBuffer(offset++);
            } while (token.isIgnored());
        }
        return new TokenizerResult(token);
    }

    private TokenizerResult getTokenB(boolean consume, int... selector) {

        Token token;
        int end = 0, y = 0, index = 0;
        List<Token> result = new ArrayList<>();

        for (; ; ) {
            token = getTokenFromBuffer(end++);
            y++;
            if (compareToken(token, selector[index])) {
                result.add(token);
                if (++index >= selector.length) {
                    break;
                }
            } else if (!token.isIgnored()) {
                return new TokenizerResult(false);
            }
        }

        if (!consume) {
            return new TokenizerResult(true);
        }

        while (y-- > 0) {
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

    public TokenizerResult next(int... selector) {
        if (ArrayUtils.isNotEmpty(selector)) {
            return getTokenB(true, selector);
        }
        return getTokenA(true);
    }

    public TokenizerResult test(int... selector) {
        if (ArrayUtils.isNotEmpty(selector)) {
            return getTokenB(false, selector);
        }
        return getTokenA(false);
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

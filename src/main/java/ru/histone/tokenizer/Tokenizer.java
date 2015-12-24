package ru.histone.tokenizer;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.NotImplementedException;
import ru.histone.expression.Expression;

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
    private List<Token> ignored;

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


    public void add(Expression expression) {
        expressions.add(expression);
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

        Token token = buffer.peek();
        if (ignored.size() > 0 /*&&*/) {
            throw new NotImplementedException();
        } else {
            return token;
        }
    }

    private Token getTokenA(boolean consume) {
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
        return token;
    }

    private Token getTokenB(boolean consume, String... selector) {
        throw new NotImplementedException();
    }

    public Token next(String... selector) {
        if (ArrayUtils.isNotEmpty(selector)) {
            return getTokenB(true, selector);
        }
        return getTokenA(true);
    }

    public Token test(String... selector) {
        if (ArrayUtils.isNotEmpty(selector)) {
            return getTokenB(false, selector);
        }
        return getTokenA(false);
    }
}

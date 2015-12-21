package ru.histone.tokenizer;

import ru.histone.expression.Expression;

import java.util.ArrayList;
import java.util.List;
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
    private final int inputLen;


//    public Tokenizer(int capacity) {
//        expressions = new ArrayList<Expression>(capacity);
//    }

    public Tokenizer(String input, String baseURI, List<Expression> expressions) {
        this.expressions = new ArrayList<>(expressions.size());
        this.expressions.addAll(expressions);

        this.input = input;
        this.baseURI = baseURI;
        this.inputLen = input.length();
        mainPattern = Pattern.compile(expressions.stream().map(Expression::getExpression).collect(Collectors.joining("|")));
//        this.regexp = regexp;
//
//        this.buffer =[];
//        this.tokenIds =[];
//        this.ignoredTokens = null;
//
//        var i, c, name, lastTokenId = 1,
//            tokenIds = this.tokenIds;
//
//        for (i = 0; i < names.length; i++) {
//
//            name = names[i];
//
//            if (!Utils_isUndefined(name)) {
//
//
//                tokenIds.push(name);
//
//
//            } else tokenIds.push(T_TOKEN);
//        }
    }


    public void add(Expression expression) {
        expressions.add(expression);
    }
}

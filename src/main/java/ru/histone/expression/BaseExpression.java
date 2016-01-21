package ru.histone.expression;

import static ru.histone.tokenizer.Tokens.T_TOKEN;
import ru.histone.tokenizer.Tokens;
import ru.histone.utils.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by alexey.nevinsky on 21.12.2015.
 */
public class BaseExpression implements Expression {

    private final List<Integer> ids;
    private final String expression;

    public BaseExpression(String expression, Integer... ids) {
        if (ids != null && ids.length > 0) {
            this.ids = Arrays.asList(ids);
        } else {
            this.ids = Arrays.asList(T_TOKEN.getId());
        }
        Assert.notNull(expression);
        this.expression = expression;
    }

    public BaseExpression(String expression, Tokens... tokens) {
        this.ids = Arrays.stream(tokens).map(Tokens::getId).collect(Collectors.toList());
        Assert.notNull(expression);
        this.expression = expression;
    }

    @Override
    public List<Integer> getIds() {
        return ids;
    }

    @Override
    public String getExpression() {
        return "(" + expression + ")";
    }
}

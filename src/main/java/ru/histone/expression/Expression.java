package ru.histone.expression;

import java.util.List;

/**
 * Created by alexey.nevinsky on 21.12.2015.
 */
public interface Expression {
    List<Integer> getIds();

    String getExpression();
}

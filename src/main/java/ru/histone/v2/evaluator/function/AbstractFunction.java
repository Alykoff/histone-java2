package ru.histone.v2.evaluator.function;

import ru.histone.v2.evaluator.Function;

import java.util.concurrent.Executor;

/**
 * Created by inv3r on 27/01/16.
 */
public abstract class AbstractFunction implements Function {
    protected final Executor executor;

    public AbstractFunction(Executor executor) {
        this.executor = executor;
    }
}

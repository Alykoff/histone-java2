package ru.histone.v2.evaluator.node;

import java.util.UUID;

/**
 * Created by inv3r on 19/01/16.
 */
public class EvalNode<T> {
    protected UUID id = UUID.randomUUID();
    protected T value;

    public EvalNode(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}

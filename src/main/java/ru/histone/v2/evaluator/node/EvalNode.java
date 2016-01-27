package ru.histone.v2.evaluator.node;

import java.util.UUID;
import java.util.concurrent.Future;

/**
 * Created by inv3r on 19/01/16.
 */
public class EvalNode<T> {
    protected UUID id = UUID.randomUUID();
    protected T value;
    protected boolean isAsync;
    protected Future future;

    public EvalNode(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public String asString() {
        return String.valueOf(value);
    }

    public final boolean isAsync() {
        return isAsync;
    }

    public void setAsync(boolean async) {
        isAsync = async;
    }

    @Override
    public String toString() {
        return "{\"EvalNode\": {" +
//                "\"id\": \"" + id +
                "\", \"value\": \"" + value +
                "\"}}";
    }
}

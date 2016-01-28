package ru.histone.v2.evaluator.node;

/**
 * Created by inv3r on 19/01/16.
 */
public class EvalNode<T> {
    protected T value;

    public EvalNode(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "{\"EvalNode\": {" +
                "\", \"value\": \"" + value +
                "\"}}";
    }
}

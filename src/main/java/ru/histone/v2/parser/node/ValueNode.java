package ru.histone.v2.parser.node;

import java.io.Serializable;

/**
 *
 * Created by gali.alykoff on 20/01/16.
 */
public abstract class ValueNode<T> extends AstNode implements Serializable {
    protected T value;

    public ValueNode() {
        super(AstType.AST_VALUE_NODE);
    }

    public ValueNode(T value) {
        this();
        this.value = value;
    }

    public boolean hasValue() {
        return true;
    }

    public T getValue() {
       return value;
    }

    public ValueNode<T> setValue(T value) {
        this.value = value;
        return this;
    }

    public int size() {
        return 0;
    }
}

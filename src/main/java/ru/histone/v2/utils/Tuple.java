package ru.histone.v2.utils;

import ru.histone.v2.evaluator.node.EvalNode;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author gali.alykoff on 09/02/16.
 */
public class Tuple<L, R> implements Serializable {
    private final L left;
    private final R right;

    public static <L, R> Tuple<L, R> create(L left, R right) {
        return new Tuple<>(left, right);
    }

    public Tuple(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L getLeft() {
        return this.left;
    }

    public R getRight() {
        return this.right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        return Objects.equals(left, tuple.left) &&
                Objects.equals(right, tuple.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    @Override
    public String toString() {
        return "Tuple{" +
                "left=" + left +
                ", right=" + right +
                '}';
    }
}

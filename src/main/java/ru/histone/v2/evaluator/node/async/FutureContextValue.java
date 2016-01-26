package ru.histone.v2.evaluator.node.async;

/**
 * Created by inv3r on 25/01/16.
 */
public class FutureContextValue {
    private String name;

    public FutureContextValue(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FutureContextValue{");
        sb.append("name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

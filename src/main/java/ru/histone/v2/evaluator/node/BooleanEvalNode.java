package ru.histone.v2.evaluator.node;

/**
 * Created by inv3r on 14/01/16.
 */
public class BooleanEvalNode extends EvalNode<Boolean> {
    public BooleanEvalNode(Boolean res) {
        super(res);
    }

    public BooleanEvalNode neg() {
        this.value = !value;
        return this;
    }
}

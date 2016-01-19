package ru.histone.v2.evaluator.node;

/**
 * Created by inv3r on 14/01/16.
 */
public class BooleanAstNode extends EvalNode<Boolean> {
    public BooleanAstNode(Boolean res) {
        super(res);
    }

    public BooleanAstNode neg() {
        this.value = !value;
        return this;
    }
}

package ru.histone.v2.evaluator.node;

/**
 * Created by inv3r on 19/01/16.
 */
public class FloatEvalNode extends EvalNode<Float> {
    public FloatEvalNode(Float val) {
        super(val);
        if (val == null) {
            throw new NullPointerException();
        }
    }
}

package ru.histone.v2.evaluator.node;

/**
 * Created by inv3r on 14/01/16.
 */
public class LongEvalNode extends EvalNode<Long> {

    public LongEvalNode(Long value) {
        super(value);
        if (value == null) {
            throw new NullPointerException();
        }
    }
}

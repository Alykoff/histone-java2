package ru.histone.v2.evaluator.node;

import org.apache.commons.lang.ObjectUtils;

/**
 * Created by inv3r on 15/01/16.
 */
public class NullEvalNode extends EvalNode<ObjectUtils.Null> {
    public NullEvalNode() {
        super(ObjectUtils.NULL);
    }

    @Override
    public ObjectUtils.Null getValue() {
        return ObjectUtils.NULL;
    }
}

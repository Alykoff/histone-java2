package ru.histone.v2.evaluator.node;

import org.apache.commons.lang.ObjectUtils;

/**
 * Created by inv3r on 15/01/16.
 */
public class NullAstNode extends EvalNode<ObjectUtils.Null> {
    public NullAstNode() {
        super(ObjectUtils.NULL);
    }

    @Override
    public ObjectUtils.Null getValue() {
        return null;
    }
}

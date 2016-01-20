package ru.histone.v2.parser.node;

import java.io.Serializable;

/**
 * Created by gali.alykoff on 20/01/16.
 */
public class FloatAstNode extends ValueNode<Float> implements Serializable {
    public FloatAstNode(Float value) {
        super(value);
    }
}

package ru.histone.v2.parser.node;

import java.io.Serializable;

/**
 *
 * Created by gali.alykoff on 20/01/16.
 */
public class DoubleAstNode extends ValueNode<Double> implements Serializable {
    public DoubleAstNode(Double value) {
        super(value);
    }
}

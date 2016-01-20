package ru.histone.v2.parser.node;

import java.io.Serializable;

/**
 *
 * Created by gali.alykoff on 20/01/16.
 */
public class BooleanAstNode extends ValueNode<Boolean> implements Serializable {
    public BooleanAstNode(Boolean value) {
        super(value);
    }
}

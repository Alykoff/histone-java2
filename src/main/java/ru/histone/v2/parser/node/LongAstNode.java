package ru.histone.v2.parser.node;

import java.io.Serializable;

/**
 *
 * Created by gali.alykoff on 20/01/16.
 */
public class LongAstNode extends ValueNode<Long> implements Serializable {
    public LongAstNode(Long value) {
        super(value);
    }
    public LongAstNode(Integer value) {
        super(new Long(value));
    }
}

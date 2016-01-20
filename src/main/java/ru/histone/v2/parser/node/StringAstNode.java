package ru.histone.v2.parser.node;

import java.io.Serializable;

/**
 *
 * Created by gali.alykoff on 20/01/16.
 */
public class StringAstNode extends ValueNode<String> implements Serializable {
    public StringAstNode(String value) {
        super(value);
    }

    public StringAstNode escaped() {
        // TODO
        return this;
    }
}

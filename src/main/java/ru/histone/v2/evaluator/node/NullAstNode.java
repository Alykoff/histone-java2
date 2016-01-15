package ru.histone.v2.evaluator.node;

import ru.histone.v2.parser.node.AstNode;

/**
 * Created by inv3r on 15/01/16.
 */
public class NullAstNode extends AstNode {
    public NullAstNode() {
        super(Integer.MIN_VALUE);
    }

    @Override
    public Object getValue() {
        return null;
    }
}

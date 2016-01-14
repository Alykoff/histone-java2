package ru.histone.v2.evaluator.node;

import ru.histone.v2.parser.node.AstNode;

/**
 * Created by inv3r on 14/01/16.
 */
public class BooleanAstNode extends AstNode<Boolean> {
    public BooleanAstNode(Boolean res) {
        super(Integer.MIN_VALUE);
        values.add(res);
    }

    @Override
    public Boolean getValue() {
        return values.get(0);
    }

    public BooleanAstNode neg() {
        values.set(0, !values.get(0));
        return this;
    }
}

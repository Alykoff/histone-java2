package ru.histone.v2.evaluator.node;

import ru.histone.v2.parser.node.AstNode;

/**
 * Created by inv3r on 14/01/16.
 */
public class IntAstNode extends AstNode<Integer> {
    public IntAstNode(Integer value) {
        super(Integer.MIN_VALUE);
        values.add(value);
    }

    @Override
    public Integer getValue() {
        return values.get(0);
    }
}

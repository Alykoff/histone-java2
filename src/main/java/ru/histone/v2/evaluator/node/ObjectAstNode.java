package ru.histone.v2.evaluator.node;


import ru.histone.v2.parser.node.AstNode;

/**
 * Created by inv3r on 18/01/16.
 */
public class ObjectAstNode extends AstNode {
    public ObjectAstNode(Object value) {
        super(Integer.MIN_VALUE);
        values.add(value);
    }

    @Override
    public Object getValue() {
        return values.get(0);
    }
}

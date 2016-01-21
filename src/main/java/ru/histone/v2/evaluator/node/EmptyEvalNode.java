package ru.histone.v2.evaluator.node;

/**
 * Created by inv3r on 19/01/16.
 */
public class EmptyEvalNode extends EvalNode<String> {
    public static final EmptyEvalNode INSTANCE = new EmptyEvalNode();

    private EmptyEvalNode() {
        super(null);
    }

    @Override
    public String asString() {
        return "";
    }
}

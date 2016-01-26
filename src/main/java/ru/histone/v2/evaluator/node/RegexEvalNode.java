package ru.histone.v2.evaluator.node;

import ru.histone.v2.evaluator.data.HistoneRegex;

import java.io.Serializable;

/**
 *
 * Created by gali.alykoff on 25/01/16.
 */
public class RegexEvalNode extends EvalNode<HistoneRegex> implements Serializable {
    public RegexEvalNode(HistoneRegex value) {
        super(value);
        if (value == null) {
            throw new NullPointerException();
        }
    }
}

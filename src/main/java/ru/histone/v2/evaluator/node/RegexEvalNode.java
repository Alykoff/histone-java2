package ru.histone.v2.evaluator.node;

import ru.histone.v2.evaluator.data.RegexPattern;

import java.io.Serializable;

/**
 *
 * Created by gali.alykoff on 25/01/16.
 */
public class RegexEvalNode extends EvalNode<RegexPattern> implements Serializable {
    public RegexEvalNode(RegexPattern value) {
        super(value);
        if (value == null) {
            throw new NullPointerException();
        }
    }
}

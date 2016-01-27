package ru.histone.v2.evaluator.node;

import ru.histone.v2.evaluator.data.HistoneMacro;

import java.io.Serializable;

/**
 *
 * Created by gali.alykoff on 27/01/16.
 */
public class MacroEvalNode extends EvalNode<HistoneMacro> implements Serializable {
    public MacroEvalNode(HistoneMacro value) {
        super(value);
        if (value == null) {
            throw new NullPointerException();
        }
    }
}

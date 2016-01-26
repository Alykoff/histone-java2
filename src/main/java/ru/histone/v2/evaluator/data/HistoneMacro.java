package ru.histone.v2.evaluator.data;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.node.EvalNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by gali.alykoff on 25/01/16.
 */
public class HistoneMacro implements Serializable, Cloneable {
    private List<Long> params = new ArrayList<>();
    private EvalNode body;
    private Context context;
    private List<EvalNode> args;

    public HistoneMacro(List<Long> params) {
        this.params = params;
    }
}

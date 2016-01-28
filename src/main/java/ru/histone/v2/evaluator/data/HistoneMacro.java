package ru.histone.v2.evaluator.data;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.parser.node.AstNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by gali.alykoff on 25/01/16.
 */
public class HistoneMacro implements Serializable, Cloneable {
    private AstNode body;
    private Context context;
    private List<String> args = new ArrayList<>();

    public HistoneMacro(List<String> args, AstNode body, Context context) {
        this.args.addAll(args);
        this.body = body;
        this.context = context;
    }

    public AstNode getBody() {
        return body;
    }

    public void setBody(AstNode body) {
        this.body = body;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }
}

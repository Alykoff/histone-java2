package ru.histone.v2.evaluator.data;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.Evaluator;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.parser.node.AstNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * Created by gali.alykoff on 25/01/16.
 */
public class HistoneMacro implements Serializable, Cloneable {
    private AstNode body;
    private Context context;
    private List<String> args = new ArrayList<>();
    private Evaluator evaluator;

    public HistoneMacro(List<String> args, AstNode body, Context context, Evaluator evaluator) {
        this.args.addAll(args);
        this.body = body;
        this.context = context;
        this.evaluator = evaluator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoneMacro that = (HistoneMacro) o;
        return Objects.equals(body, that.body) &&
                Objects.equals(context, that.context) &&
                Objects.equals(args, that.args) &&
                Objects.equals(evaluator, that.evaluator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(body, context, args, evaluator);
    }

    @Override
    public String toString() {
        return "{\"HistoneMacro\": {" +
                "\"body\":" + body +
                ", \"context\":" + context +
                ", \"args:\"" + args + "\"}}";
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

    public Evaluator getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(Evaluator evaluator) {
        this.evaluator = evaluator;
    }
}

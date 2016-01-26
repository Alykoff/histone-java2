package ru.histone.v2.evaluator.function.regex;

import ru.histone.v2.evaluator.Function;
import ru.histone.v2.evaluator.data.HistoneRegex;
import ru.histone.v2.evaluator.node.*;
import ru.histone.v2.exceptions.GlobalFunctionExecutionException;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * Created by gali.alykoff on 26/01/16.
 */
public class Test implements Function, Serializable {
    public static final String NAME = "test";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public EvalNode execute(List<EvalNode> args) throws GlobalFunctionExecutionException {
        try {
            final HistoneRegex regexHistone = (HistoneRegex) args.get(0).getValue();
            final EvalNode evalNode = args.get(1);
            final String exp = String.valueOf(evalNode.getValue());
            final Pattern pattern = regexHistone.getPattern();
//            final boolean isGlobal = regexHistone.isGlobal();

            final Matcher matcher = pattern.matcher(exp);
            final boolean result = matcher.find();

            return new BooleanEvalNode(result);
        } catch (Exception e) {
            throw new GlobalFunctionExecutionException(e);
        }
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isClear() {
        return true;
    }
}

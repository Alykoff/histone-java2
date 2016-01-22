package ru.histone.v2.rtti;

import org.apache.commons.lang.NotImplementedException;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.Function;
import ru.histone.v2.evaluator.node.*;

import static ru.histone.v2.rtti.HistoneType.*;

/**
 *
 * Created by gali.alykoff on 22/01/16.
 */
public interface Irtti {
    HistoneType getType(EvalNode node);

    void callSync(EvalNode node, String funcName, Context context, Object... args);

    Function getFunc(HistoneType type, String funcName);

    void register(HistoneType type, String funcName, Function func);

    void unregistered(HistoneType type, String funcName);
}

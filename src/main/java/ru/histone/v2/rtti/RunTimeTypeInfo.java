package ru.histone.v2.rtti;

import org.apache.commons.lang.NotImplementedException;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.Function;
import ru.histone.v2.evaluator.function.any.ToJson;
import ru.histone.v2.evaluator.function.array.Keys;
import ru.histone.v2.evaluator.function.array.Size;
import ru.histone.v2.evaluator.function.global.LoadJson;
import ru.histone.v2.evaluator.function.global.Range;
import ru.histone.v2.evaluator.function.regex.Test;
import ru.histone.v2.evaluator.node.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static ru.histone.v2.rtti.HistoneType.*;

/**
 * RTTI
 * Created by gali.alykoff on 22/01/16.
 */
public class RunTimeTypeInfo implements Irtti, Serializable {
    private Map<HistoneType, Map<String, Function>> userTypes = new ConcurrentHashMap<>();
    private Map<HistoneType, Map<String, Function>> typeMembers = new ConcurrentHashMap<>();

    public RunTimeTypeInfo() {
        for (HistoneType type : HistoneType.values()) {
            typeMembers.put(type, new HashMap<>());
            userTypes.put(type, new HashMap<>());
        }

        registerCommonFunctions();
    }

    public HistoneType getType(EvalNode node) {
        if (node == null) {
            throw new NullPointerException();
        }
        if (node instanceof NullEvalNode) {
            return T_NULL;
        } else if (node instanceof FloatEvalNode) {
            final Float valueFloat = ((FloatEvalNode) node).getValue();
            if (valueFloat == null || Float.isNaN(valueFloat) || !Float.isFinite(valueFloat)) {
                return T_UNDEFINED;
            }
            return T_NUMBER;
        } else if (node instanceof LongEvalNode) {
            return T_NUMBER;
        } else if (node instanceof MapEvalNode) {
            return T_ARRAY;
        } else if (node instanceof BooleanEvalNode) {
            return T_BOOLEAN;
        } else if (node instanceof StringEvalNode) {
            return T_STRING;
        } else if (node instanceof EmptyEvalNode) {
            return T_UNDEFINED;
        } else if (node instanceof RegexEvalNode) {
            return T_REGEXP;
        }
        // T_MACRO, T_GLOBAL
        throw new NotImplementedException(node.toString());
    }

    private void registerCommonFunctions() {
        registerForAlltypes(new ToJson());

        registerCommon(T_ARRAY, new Size());
        registerCommon(T_ARRAY, new Keys(true));
        registerCommon(T_ARRAY, new Keys(false));

        registerCommon(T_GLOBAL, new Range());
        registerCommon(T_GLOBAL, new LoadJson());

        registerCommon(T_REGEXP, new Test());
    }

    private void registerForAlltypes(Function function) {
        for (HistoneType type : HistoneType.values()) {
            registerCommon(type, function);
        }
    }

    private void registerCommon(HistoneType type, Function function) {
        typeMembers.get(type).put(function.getName(), function);
    }

    public void callSync(EvalNode node, String funcName, Context context, Object... args) {
        throw new NotImplementedException();
    }

    public Function getFunc(HistoneType type, String funcName) {
        Function f = userTypes.get(type).get(funcName);
        if (f == null) {
            return typeMembers.get(type).get(funcName);
        }
        return f;
    }

    public void register(HistoneType type, String funcName, Function func) {
        throw new NotImplementedException();
    }

    public void unregistered(HistoneType type, String funcName) {
        throw new NotImplementedException();
    }
}

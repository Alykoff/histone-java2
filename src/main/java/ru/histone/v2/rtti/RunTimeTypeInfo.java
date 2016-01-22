package ru.histone.v2.rtti;

import org.apache.commons.lang.NotImplementedException;
import ru.histone.v2.evaluator.Function;
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
public class RunTimeTypeInfo implements Serializable {
    private Map<HistoneType, Map<String, Function>> userTypes = new ConcurrentHashMap<>();
    private Map<HistoneType, Map<String, Function>> typeMembers = new ConcurrentHashMap<>();

    public RunTimeTypeInfo() {
        typeMembers.put(T_UNDEFINED, new HashMap<>());
        typeMembers.put(T_NULL, new HashMap<>());
        typeMembers.put(T_BOOLEAN, new HashMap<>());
        typeMembers.put(T_NUMBER, new HashMap<>());
        typeMembers.put(T_STRING, new HashMap<>());
        typeMembers.put(T_REGEXP, new HashMap<>());
        typeMembers.put(T_MACRO, new HashMap<>());
        typeMembers.put(T_ARRAY, new HashMap<>());
        typeMembers.put(T_GLOBAL, new HashMap<>());
    }

    public RunTimeTypeInfo(Map<HistoneType, Map<String, Function>> userTypes) {
        this();
        this.userTypes.putAll(userTypes);
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
        }
        // T_MACRO, T_GLOBAL, T_REGEXP
        throw new NotImplementedException();
    }




}

package ru.histone.v2.rtti;

import ru.histone.v2.evaluator.Function;

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



}

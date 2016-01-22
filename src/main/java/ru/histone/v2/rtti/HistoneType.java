package ru.histone.v2.rtti;

/**
 *
 * Created by gali.alykoff on 22/01/16.
 */
public enum HistoneType {
    T_BASE(0),
    T_UNDEFINED(1),
    T_NULL(2),
    T_BOOLEAN(3),
    T_NUMBER(4),
    T_STRING(5),
    T_REGEXP(6),
    T_MACRO(7),
    T_ARRAY(8),
    T_GLOBAL(9);

    private int id;

    HistoneType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}

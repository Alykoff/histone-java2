package ru.histone.v2.parser.node;

/**
 * Created by alexey.nevinsky on 24.12.2015.
 */
public enum SupportAstType {
    TEXT_CACHE(0),
    JSON_CACHE(1),
    TPL_RESULT_CACHE(2),
    TPL_AST_CACHE(3);

    private final int id;

    SupportAstType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}

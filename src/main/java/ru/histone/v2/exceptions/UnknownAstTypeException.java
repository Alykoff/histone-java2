package ru.histone.v2.exceptions;

import ru.histone.HistoneException;

/**
 *
 * Created by gali.alykoff on 20/01/16.
 */
public class UnknownAstTypeException extends HistoneException {
    public static final String UNKNOWN_AST_TYPE_MSG = "Unknown ast type";

    public UnknownAstTypeException() {
        super(UNKNOWN_AST_TYPE_MSG);
    }

    public UnknownAstTypeException(int type) {
        super(UNKNOWN_AST_TYPE_MSG + ": '" + type + "'");
    }

    public UnknownAstTypeException(String message) {
        super(message);
    }

    public UnknownAstTypeException(Throwable cause) {
        super(UNKNOWN_AST_TYPE_MSG, cause);
    }

    public UnknownAstTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}

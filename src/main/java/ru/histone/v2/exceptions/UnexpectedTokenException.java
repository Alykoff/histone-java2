package ru.histone.v2.exceptions;

import ru.histone.v2.parser.ParserException;

/**
 *
 * Created by gali.alykoff on 21/01/16.
 */
public class UnexpectedTokenException extends ParserException {
    public UnexpectedTokenException(String message, String baseURI, int line) {
        super(message, baseURI, line);
    }
}

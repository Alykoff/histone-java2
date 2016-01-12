package ru.histone.v2.parser;

import ru.histone.HistoneException;

/**
 * Created by alexey.nevinsky on 24.12.2015.
 */
public class ParserException extends HistoneException {
    private final String baseURI;
    private final int line;

    public ParserException(String message, String baseURI, int line) {
        super(message);
        this.baseURI = baseURI;
        this.line = line;
    }

    public String getBaseURI() {
        return baseURI;
    }

    public int getLine() {
        return line;
    }
}

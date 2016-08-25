package ru.histone.v2.spring.processors;

import ru.histone.v2.exceptions.ParserException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Aleksander Melnichnikov
 */
public class ParsingHistoneProcessingExceptionProcessor extends HistoneProcessingExceptionProcessor {

    @Override
    protected boolean supports(Class<? extends Exception> type) {
        return ParserException.class.isAssignableFrom(type);
    }

    @Override
    protected void processInternal(HttpServletRequest request, HttpServletResponse response,
                                   Exception exception, String templateLocation) throws Exception {
        throw new RuntimeException("Error evaluating histone template '" + templateLocation + "' in line "
                + ((ParserException) exception).getLine() + ": " + exception.getMessage(), exception);
    }
}


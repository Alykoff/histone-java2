package ru.histone.v2.spring.processors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Aleksander Melnichnikov
 */
public class DefaultHistoneProcessingExceptionProcessor extends HistoneProcessingExceptionProcessor {

    @Override
    protected boolean supports(Class<? extends Exception> type) {
        return Exception.class.isAssignableFrom(type);
    }

    @Override
    protected void processInternal(HttpServletRequest request, HttpServletResponse response,
                                   Exception exception, String templateLocation) throws Exception {
        throw new RuntimeException("Error evaluating histone template '" + templateLocation + "': "
                + exception.getMessage(), exception);
    }
}

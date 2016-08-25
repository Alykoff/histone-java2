package ru.histone.v2.spring.processors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Aleksander Melnichnikov
 */
public abstract class HistoneProcessingExceptionProcessor {

    protected HistoneProcessingExceptionProcessor next;

    public final void process(HttpServletRequest request, HttpServletResponse response,
                              Exception exception, String templateLocation) throws Exception {
        Exception cause = getRootCause(exception);
        if (supports(cause.getClass())) {
            processInternal(request, response, cause, templateLocation);
            return;
        }
        if (next != null) {
            next.process(request, response, cause, templateLocation);
        }
    }

    public HistoneProcessingExceptionProcessor setNext(HistoneProcessingExceptionProcessor processor) {
        next = processor;
        return processor;
    }

    protected abstract boolean supports(Class<? extends Exception> type);

    protected Exception getRootCause(Exception exception) {
        Throwable cause = exception.getCause();
        while (cause != null) {
            exception = (Exception) cause;
            cause = exception.getCause();
        }
        return exception;
    }

    protected abstract void processInternal(HttpServletRequest request, HttpServletResponse response,
                                            Exception exception, String templateLocation) throws Exception;

}

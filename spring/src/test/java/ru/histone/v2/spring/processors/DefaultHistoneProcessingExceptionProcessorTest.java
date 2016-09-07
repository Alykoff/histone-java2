package ru.histone.v2.spring.processors;

import org.junit.Test;
import ru.histone.v2.exceptions.ParserException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author Aleksander Melnichnikov
 */
public class DefaultHistoneProcessingExceptionProcessorTest {

    private DefaultHistoneProcessingExceptionProcessor postProcessor =
            new DefaultHistoneProcessingExceptionProcessor();

    @Test
    public void testSupports_supported() throws Exception {
        assertTrue(postProcessor.supports(Exception.class));
        assertTrue(postProcessor.supports(RuntimeException.class));
        assertTrue(postProcessor.supports(ParserException.class));
    }


    @Test
    public void testProcessInternal() throws Exception {
        try {
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            postProcessor.process(request, response, new Exception("Some exception occured"), "someLocation");
            fail("Exception should be thrown");
        } catch (Exception ex) {
            assertThat(ex.getMessage(),
                    is("Error evaluating histone template 'someLocation': Some exception occured"));
        }
    }
}
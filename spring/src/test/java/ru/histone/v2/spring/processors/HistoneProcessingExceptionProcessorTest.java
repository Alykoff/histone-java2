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
public class HistoneProcessingExceptionProcessorTest {

    private HistoneProcessingExceptionProcessor exceptionProcessor;
    private HttpServletRequest request = mock(HttpServletRequest.class);
    private HttpServletResponse response = mock(HttpServletResponse.class);

    {
        HistoneProcessingExceptionProcessor processor =
                new ParsingHistoneProcessingExceptionProcessor();
        processor.setNext(new DefaultHistoneProcessingExceptionProcessor());
        exceptionProcessor = processor;
    }

    @Test
    public void testProcess() throws Exception {
        try {
            exceptionProcessor.process(request, response, new Exception("Some exception occured"), "someLocation");
            fail("Exception should be thrown");
        } catch (Exception ex) {
            assertThat(ex.getMessage(),
                    is("Error evaluating histone template 'someLocation': Some exception occured"));
        }
        try {
            exceptionProcessor.process(request, response, new ParserException("parsing was failed", "uri", 0), "someLocation");
            fail("Exception should be thrown");
        } catch (Exception ex) {
            assertThat(ex.getMessage(),
                    is("Error evaluating histone template 'someLocation' in line 0: parsing was failed"));
        }
    }

    @Test
    public void testGetRootCause() throws Exception {
        Exception result =
                exceptionProcessor.getRootCause(new Exception("WrongMessage0",
                        new RuntimeException("WrongMessage1", new IllegalArgumentException("CorrectMessage"))));
        assertEquals(IllegalArgumentException.class, result.getClass());
        assertThat(result.getMessage(), is("CorrectMessage"));
    }

}
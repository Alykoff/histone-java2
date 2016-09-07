package ru.histone.v2.spring.processors;

import org.junit.jupiter.api.Test;
import ru.histone.v2.exceptions.ParserException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author Aleksander Melnichnikov
 */
public class ParsingHistoneProcessingExceptionProcessorTest {

    private ParsingHistoneProcessingExceptionProcessor postProcessor =
            new ParsingHistoneProcessingExceptionProcessor();

    @Test
    public void testSupports_supported() throws Exception {
        assertTrue(postProcessor.supports(ParserException.class));
    }

    @Test
    public void testSupports_not_supported() throws Exception {
        assertFalse(postProcessor.supports(Exception.class));
    }

    @Test
    public void testProcessInternal() throws Exception {
        try {
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            postProcessor.process(request, response, new ParserException("parsing was failed", "uri", 0), "someLocation");
            fail("Exception should be thrown");
        } catch (Exception ex) {
            assertThat(ex.getMessage(),
                    is("Error evaluating histone template 'someLocation' in line 0: parsing was failed"));
        }
    }
}
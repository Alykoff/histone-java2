package ru.histone.v2.spring.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.View;
import ru.histone.v2.spring.processors.DefaultHistoneProcessingExceptionProcessor;
import ru.histone.v2.spring.processors.HistoneProcessingExceptionProcessor;
import ru.histone.v2.spring.processors.ParsingHistoneProcessingExceptionProcessor;

import java.util.Locale;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * @author Aleksander Melnichnikov
 */
public class HistoneViewResolverTest extends HistoneSpringTestSupport {

    @Autowired
    protected HistoneViewResolver histoneViewResolver;

    @Test
    public void testCreateView() throws Exception {
        View view = histoneViewResolver.createView("testTemplate", Locale.getDefault());
        assertEquals(view.getClass(), HistoneView.class);
        HistoneView templateView = (HistoneView) view;
        assertThat(templateView.getUrl(), is("/WEB-INF/templates/testTemplate.tpl"));
        assertNotNull(templateView.getTemplateLocation());
    }

    @Test
    public void testRequiredViewClass() throws Exception {
        Class<?> clazz = histoneViewResolver.requiredViewClass();
        assertEquals(clazz, HistoneView.class);
    }

    @Test
    public void testBuildView() throws Exception {
        View view = histoneViewResolver.createView("testTemplate", Locale.getDefault());
        assertEquals(view.getClass(), HistoneView.class);
        HistoneView templateView = (HistoneView) view;
        assertThat(templateView.getUrl(), is("/WEB-INF/templates/testTemplate.tpl"));
        assertNotNull(templateView.getTemplateLocation());
        assertNotNull(templateView.getHistone());
        assertNotNull(templateView.getHistoneProcessingExceptionProcessor());
    }

    @Test
    public void testInitExceptionProcessingChain() throws Exception {
        HistoneProcessingExceptionProcessor histoneProcessingExceptionProcessor =
                histoneViewResolver.initExceptionProcessingChain();
        assertNotNull(histoneProcessingExceptionProcessor);
    }

    @Test
    public void testAddExceptionProcessor() throws Exception {
        HistoneProcessingExceptionProcessor histoneProcessingExceptionProcessor =
                histoneViewResolver.initExceptionProcessingChain();
        assertEquals(histoneProcessingExceptionProcessor.getClass(), ParsingHistoneProcessingExceptionProcessor.class);
        histoneViewResolver.addExceptionProcessor(new DefaultHistoneProcessingExceptionProcessor());
        histoneProcessingExceptionProcessor = histoneViewResolver.getProcessorChain();
        assertEquals(histoneProcessingExceptionProcessor.getClass(), DefaultHistoneProcessingExceptionProcessor.class);
    }
}
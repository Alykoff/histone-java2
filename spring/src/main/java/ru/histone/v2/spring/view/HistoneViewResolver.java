package ru.histone.v2.spring.view;

import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import ru.histone.v2.spring.HistoneSpringEngine;
import ru.histone.v2.spring.processors.DefaultHistoneProcessingExceptionProcessor;
import ru.histone.v2.spring.processors.HistoneProcessingExceptionProcessor;
import ru.histone.v2.spring.processors.HistoneTemplatePostProcessor;
import ru.histone.v2.spring.processors.ParsingHistoneProcessingExceptionProcessor;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @author Aleksander Melnichnikov
 */

public class HistoneViewResolver extends UrlBasedViewResolver {

    private static final String DEFAULT_ENCODING = "UTF-8";
    protected String templateLocation = "";
    protected String encoding;
    protected HistoneSpringEngine histone;
    protected HistoneProcessingExceptionProcessor processorChain = initExceptionProcessingChain();
    protected List<HistoneTemplatePostProcessor> postProcessors = Collections.emptyList();

    @PostConstruct
    public void postConstruct() {
        if (histone == null) {
            this.histone = getApplicationContext().getBean(HistoneSpringEngine.class);
        }
    }

    @Override
    protected View createView(String viewName, Locale locale) throws Exception {
        return super.createView(viewName, locale);
    }

    @Override
    protected Class<?> requiredViewClass() {
        return HistoneView.class;
    }

    @Override
    protected HistoneView buildView(String viewName) throws Exception {
        HistoneView view = (HistoneView) super.buildView(viewName);
        view.setEncoding(encoding == null ? DEFAULT_ENCODING : encoding);
        view.setTemplateLocation(templateLocation);
        view.setHistone(histone);
        view.setHistoneProcessingExceptionProcessor(processorChain);
        view.setHistoneTemplatePostProcessors(postProcessors);
        return view;
    }

    protected HistoneProcessingExceptionProcessor initExceptionProcessingChain() {
        ParsingHistoneProcessingExceptionProcessor exceptionProcessor = new ParsingHistoneProcessingExceptionProcessor();
        exceptionProcessor.setNext(new DefaultHistoneProcessingExceptionProcessor());
        return exceptionProcessor;
    }

    public HistoneViewResolver addExceptionProcessor(HistoneProcessingExceptionProcessor exceptionProcessor) {
        exceptionProcessor.setNext(processorChain);
        processorChain = exceptionProcessor;
        return this;
    }

    public String getTemplateLocation() {
        return templateLocation;
    }

    public void setTemplateLocation(String templateLocation) {
        this.templateLocation = templateLocation;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public HistoneSpringEngine getHistone() {
        return histone;
    }

    public void setHistone(HistoneSpringEngine histone) {
        this.histone = histone;
    }

    public List<HistoneTemplatePostProcessor> getPostProcessors() {
        return postProcessors;
    }

    public void setPostProcessors(List<HistoneTemplatePostProcessor> postProcessors) {
        this.postProcessors = postProcessors;
    }

    public HistoneProcessingExceptionProcessor getProcessorChain() {
        return processorChain;
    }

    public void setProcessorChain(HistoneProcessingExceptionProcessor processorChain) {
        this.processorChain = processorChain;
    }
}

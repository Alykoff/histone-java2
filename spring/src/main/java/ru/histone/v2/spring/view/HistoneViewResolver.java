package ru.histone.v2.spring.view;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.Assert;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import ru.histone.v2.Histone;
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
@Setter
@Getter
public class HistoneViewResolver extends UrlBasedViewResolver {

    private static final String DEFAULT_ENCODING = "UTF-8";
    protected String templateLocation = "";
    protected String encoding;
    protected boolean cachingEnabled;
    protected Histone histone;
    protected HistoneProcessingExceptionProcessor processorChain = initExceptionProcessingChain();
    protected List<HistoneTemplatePostProcessor> postProcessors = Collections.emptyList();

    @PostConstruct
    public void postConstruct() {
        Histone histone = getApplicationContext().getBean(Histone.class);
        Assert.notNull(histone, "Histone bean should present in Application Context");
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


}

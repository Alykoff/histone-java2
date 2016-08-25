package ru.histone.v2.spring.view;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import ru.histone.v2.evaluator.data.HistoneMacro;
import ru.histone.v2.evaluator.data.HistoneRegex;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.spring.HistoneSpringEngine;
import ru.histone.v2.spring.processors.HistoneProcessingExceptionProcessor;
import ru.histone.v2.spring.processors.HistoneTemplatePostProcessor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Aleksander Melnichnikov
 */
public class HistoneView extends AbstractUrlBasedView {

    private static final Logger log = LoggerFactory.getLogger(HistoneView.class);
    protected String encoding;
    protected String templateLocation;
    protected HistoneSpringEngine histone;
    protected HistoneProcessingExceptionProcessor histoneProcessingExceptionProcessor;
    protected List<HistoneTemplatePostProcessor> histoneTemplatePostProcessors;

    @Override
    protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String templatePath = this.templateLocation + getUrl();
        try (ByteArrayOutputStream baos = createTemporaryOutputStream()) {
            log.trace("Processing page with location " + templatePath);
            long startTime = System.currentTimeMillis();
            String templateData = postProcessTemplate(
                    histone.processTemplate(templatePath, getUrl(), filterHistoneParams(model), encoding), request, response
            );
            baos.write(templateData.getBytes(encoding));
            writeToResponse(response, baos);
            long endTime = System.currentTimeMillis();
            log.trace("Page processed for " + (endTime - startTime) + "ms");
        } catch (Exception ex) {
            histoneProcessingExceptionProcessor.process(request, response, ex, templatePath);
        }

    }

    protected Map<String, Object> filterHistoneParams(Map<String, Object> paramMap) {
        return paramMap.entrySet().stream()
                .filter(entry ->
                        entry.getValue() == null ||
                                entry.getValue().equals(ObjectUtils.NULL) ||
                                entry.getValue() instanceof Boolean ||
                                entry.getValue() instanceof Integer ||
                                entry.getValue() instanceof Float ||
                                entry.getValue() instanceof Double ||
                                entry.getValue() instanceof Long ||
                                entry.getValue() instanceof String ||
                                entry.getValue() instanceof Map ||
                                entry.getValue() instanceof HistoneRegex ||
                                entry.getValue() instanceof HistoneMacro ||
                                entry.getValue() instanceof EvalNode)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    protected String postProcessTemplate(String templateData, HttpServletRequest request, HttpServletResponse response) {
        for (HistoneTemplatePostProcessor histoneTemplatePostProcessor : histoneTemplatePostProcessors) {
            templateData = histoneTemplatePostProcessor.postProcess(templateData, request, response);
        }
        return templateData;
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

    public List<HistoneTemplatePostProcessor> getHistoneTemplatePostProcessors() {
        return histoneTemplatePostProcessors;
    }

    public void setHistoneTemplatePostProcessors(List<HistoneTemplatePostProcessor> histoneTemplatePostProcessors) {
        this.histoneTemplatePostProcessors = histoneTemplatePostProcessors;
    }

    public HistoneProcessingExceptionProcessor getHistoneProcessingExceptionProcessor() {
        return histoneProcessingExceptionProcessor;
    }

    public void setHistoneProcessingExceptionProcessor(HistoneProcessingExceptionProcessor histoneProcessingExceptionProcessor) {
        this.histoneProcessingExceptionProcessor = histoneProcessingExceptionProcessor;
    }
}

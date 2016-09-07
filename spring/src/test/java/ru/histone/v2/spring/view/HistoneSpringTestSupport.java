package ru.histone.v2.spring.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.AnnotationConfigWebContextLoader;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import ru.histone.v2.spring.HistoneSpringEngine;
import ru.histone.v2.spring.processors.HistoneTemplatePostProcessor;
import ru.histone.v2.spring.view.config.HistoneTestConfig;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * @author Aleksander Melnichnikov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = HistoneTestConfig.class, loader = AnnotationConfigWebContextLoader.class)
@WebAppConfiguration("/target/test-classes/webapp")
public abstract class HistoneSpringTestSupport {

    @Autowired
    protected WebApplicationContext webApplicationContext;
    @Autowired
    protected HistoneSpringEngine histone;

    protected MockMvc mockMvc;

    @BeforeEach
    public void before() throws Exception {
        this.mockMvc = webAppContextSetup(this.webApplicationContext).build();
        HistoneSpringTestController histoneSpringTestController = new HistoneSpringTestController();
        HistoneViewResolver viewResolver = histoneViewResolver(histone);
        this.webApplicationContext.getAutowireCapableBeanFactory().autowireBean(histoneSpringTestController);
        this.mockMvc = standaloneSetup(histoneSpringTestController)
                .setViewResolvers(viewResolver).build();
    }

    protected HistoneViewResolver histoneViewResolver(HistoneSpringEngine histone) {
        HistoneViewResolver histoneViewResolver = new HistoneViewResolver();
        histoneViewResolver.setHistone(histone);
        histoneViewResolver.setPrefix("/WEB-INF/templates/");
        histoneViewResolver.setSuffix(".tpl");
        histoneViewResolver.setPostProcessors(postProcessors());
        histoneViewResolver.setViewClass(HistoneView.class);
        return histoneViewResolver;
    }

    protected List<HistoneTemplatePostProcessor> postProcessors() {
        HistoneTemplatePostProcessor removeATagPostProcessor =
                (template, request1, response) -> template.replaceAll("<a.*>(.*)</a>", "");
        HistoneTemplatePostProcessor removePTagPostProcessor =
                (template, request1, response) -> template.replaceAll("<p.*>(.*)</p>", "");
        return Arrays.asList(removeATagPostProcessor, removePTagPostProcessor);
    }
}

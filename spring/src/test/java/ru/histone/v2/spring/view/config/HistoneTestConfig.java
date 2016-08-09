package ru.histone.v2.spring.view.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ru.histone.v2.Histone;
import ru.histone.v2.spring.HistoneSpringEngine;
import ru.histone.v2.spring.view.HistoneView;
import ru.histone.v2.spring.view.HistoneViewResolver;

/**
 * @author Aleksander Melnichnikov
 */
@Configuration
@ComponentScan("ru.histone.v2.spring.view.controller")
public class HistoneTestConfig {

    @Bean
    public Histone histone() {
        return new HistoneSpringEngine();
    }

    @Bean
    protected HistoneViewResolver histoneViewResolver(Histone histone) {
        HistoneViewResolver histoneViewResolver = new HistoneViewResolver();
        histoneViewResolver.setHistone(histone);
        histoneViewResolver.setPrefix("/WEB-INF/templates/");
        histoneViewResolver.setSuffix(".tpl");
        histoneViewResolver.setViewClass(HistoneView.class);
        return histoneViewResolver;
    }
}

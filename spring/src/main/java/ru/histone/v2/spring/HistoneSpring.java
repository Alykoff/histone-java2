package ru.histone.v2.spring;

import org.springframework.util.Assert;
import org.springframework.web.context.ServletContextAware;
import ru.histone.v2.Histone;
import ru.histone.v2.evaluator.Converter;
import ru.histone.v2.evaluator.Evaluator;
import ru.histone.v2.evaluator.resource.loader.FileLoader;
import ru.histone.v2.parser.Parser;
import ru.histone.v2.property.DefaultPropertyHolder;
import ru.histone.v2.rtti.RunTimeTypeInfo;
import ru.histone.v2.spring.resource.SpringSchemaResourceLoader;
import ru.histone.v2.spring.resource.loader.ClassPathLoader;
import ru.histone.v2.spring.resource.loader.ServletContextLoader;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * @author Aleksander Melnichnikov
 */
public class HistoneSpring extends Histone implements HistoneSpringEngine, ServletContextAware {

    protected ServletContext servletContext;

    public HistoneSpring() {
        super(new ForkJoinPool());
    }

    public HistoneSpring(Locale locale, Executor executor) {
        super(executor);
        this.locale = locale;
    }

    @Override
    protected void initializeHistone(Executor executor) {
        logger.info("Initializing Histone2 engine, implementation: " + getClass() + ". With executor: " + executor.getClass());
        this.converter = new Converter();
        this.evaluator = new Evaluator(converter);
        this.parser = new Parser();
        this.executor = executor;
        this.propertyHolder = new DefaultPropertyHolder();
        this.resourceLoader = new SpringSchemaResourceLoader();
        this.locale = Locale.getDefault();
        this.runTimeTypeInfo = new RunTimeTypeInfo(executor, resourceLoader, evaluator, parser);
        logger.info("Initialization finished");
        logger.info("================================================================");
    }

    @PostConstruct
    protected void postConstruct() {
        Assert.notNull(servletContext, "HistoneSpring must run within application context");
        //Init default loaders
        resourceLoader.addLoader(new ServletContextLoader(servletContext));
        resourceLoader.addLoader(new FileLoader());
        resourceLoader.addLoader(new ClassPathLoader());
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}

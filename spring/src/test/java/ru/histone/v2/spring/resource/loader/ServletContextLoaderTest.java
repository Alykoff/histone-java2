package ru.histone.v2.spring.resource.loader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.AnnotationConfigWebContextLoader;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;
import ru.histone.v2.evaluator.resource.Resource;
import ru.histone.v2.evaluator.resource.loader.Loader;
import ru.histone.v2.spring.view.config.HistoneTestConfig;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * @author Aleksander Melnichnikov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = HistoneTestConfig.class, loader = AnnotationConfigWebContextLoader.class)
@WebAppConfiguration("/target/test-classes/webapp")
public class ServletContextLoaderTest {

    @Autowired
    protected WebApplicationContext webApplicationContext;

    private Loader servletContextLoader;

    @PostConstruct
    public void initLoader() {
        servletContextLoader = new ServletContextLoader(webApplicationContext.getServletContext());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testLoadResource_existing_0() throws Exception {
        URI existingResourceURI = URI.create("/WEB-INF/templates/testTemplate.tpl");
        Resource existingResource = (Resource) servletContextLoader.loadResource(existingResourceURI,
                Collections.EMPTY_MAP).join();
        assertNotNull(existingResource);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testLoadResource_existing_1() throws Exception {
        URI existingResourceURI = URI.create("WEB-INF/templates/testTemplate.tpl");
        Resource existingResource = (Resource) servletContextLoader.loadResource(existingResourceURI,
                Collections.EMPTY_MAP).join();
        assertNotNull(existingResource);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testLoadResource_notExisting() throws Exception {
        URI notExistingResourceURI = URI.create("servletContextResourceNotExist.tpl");
        try {
            servletContextLoader.loadResource(notExistingResourceURI,
                    Collections.EMPTY_MAP).join();
            fail("Exception should be thrown");
        } catch (Exception ex) {
            assertThat(ex.getMessage(),
                    is("Error with ResourceInputStream for file 'servletContextResourceNotExist.tpl'"));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testLoadResource_badSchema_0() throws Exception {
        URI badSchemaURI = URI.create("classPath:/WEB-INF/templates/testTemplate.tpl");
        Resource resource = (Resource) servletContextLoader.loadResource(badSchemaURI,
                Collections.EMPTY_MAP).join();
        assertNull(resource);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testLoadResource_badSchema_1() throws Exception {
        URI badSchemaURI = URI.create("file:/WEB-INF/templates/testTemplate.tpl");
        Resource resource = (Resource) servletContextLoader.loadResource(badSchemaURI,
                Collections.EMPTY_MAP).join();
        assertNull(resource);
    }
}
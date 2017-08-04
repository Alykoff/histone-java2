package ru.histone.v2.spring.resource;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import ru.histone.v2.evaluator.resource.Resource;
import ru.histone.v2.spring.resource.loader.ServletContextLoader;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.histone.v2.spring.resource.loader.ClassPathLoaderTest.createCtx;

/**
 * @author Aleksander Melnichnikov
 */
public class SpringSchemaResourceLoaderTest {

    private static final String PATH_SERVLET_CONTEXT_RESOURCE = "WEB-INF/templates/testTemplate.tpl";
    private static final String INPUT_STREAM_CONTENT = "<div>content</div>";
    private ServletContext servletContextMock;

    @Before
    public void initMocks() {
        servletContextMock = mock(ServletContext.class);
        when(servletContextMock.getResourceAsStream(PATH_SERVLET_CONTEXT_RESOURCE))
                .thenReturn(IOUtils.toInputStream(INPUT_STREAM_CONTENT));
    }

    @Test
    public void testLoad_noResourceLoaders() {
        SpringSchemaResourceLoader resourceLoader = new SpringSchemaResourceLoader();
        try {
            resourceLoader.load(createCtx(), PATH_SERVLET_CONTEXT_RESOURCE, null, Collections.EMPTY_MAP).join();
            fail("Exception should be thrown");
        } catch (Exception ex) {
            assertThat(ex.getMessage(),
                       is("ru.histone.v2.exceptions.ResourceLoadException: No loaders find, register loader"));
        }
    }

    @Test
    public void testLoad_hrefIsNull_0() {
        SpringSchemaResourceLoader resourceLoader = new SpringSchemaResourceLoader();
        try {
            resourceLoader.load(createCtx(), null, PATH_SERVLET_CONTEXT_RESOURCE, Collections.EMPTY_MAP).join();
            fail("Exception should be thrown");
        } catch (Exception ex) {
            assertThat(ex.getMessage(),
                       is("ru.histone.v2.exceptions.ResourceLoadException: HREF is null. Unsupported value"));
        }
    }

    @Test
    public void testLoad_hrefIsNull_1() {
        SpringSchemaResourceLoader resourceLoader = new SpringSchemaResourceLoader();
        try {
            resourceLoader.load(createCtx(), null, null, Collections.EMPTY_MAP).join();
            fail("Exception should be thrown");
        } catch (Exception ex) {
            assertThat(ex.getMessage(),
                       is("ru.histone.v2.exceptions.ResourceLoadException: HREF is null. Unsupported value"));
        }
    }

    @Test
    public void testLoad() throws IOException {
        SpringSchemaResourceLoader resourceLoader = new SpringSchemaResourceLoader();
        resourceLoader.addLoader(new ServletContextLoader(servletContextMock));
        Resource resource = resourceLoader.load(createCtx(), PATH_SERVLET_CONTEXT_RESOURCE, null, null).join();
        assertNotNull(resource);
        assertThat(resource.getBaseHref(), is(PATH_SERVLET_CONTEXT_RESOURCE));
        assertThat(resource.getContent(), is(INPUT_STREAM_CONTENT));
        assertThat(resource.getContentType(), is("TEXT"));
    }

}
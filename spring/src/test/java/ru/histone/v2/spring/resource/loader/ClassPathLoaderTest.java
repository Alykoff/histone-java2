package ru.histone.v2.spring.resource.loader;

import org.junit.Test;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.resource.Resource;
import ru.histone.v2.evaluator.resource.loader.Loader;

import java.net.URI;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * @author Aleksander Melnichnikov
 */
public class ClassPathLoaderTest {

    private Loader classPathLoader = new ClassPathLoader();

    @Test
    @SuppressWarnings("unchecked")
    public void testLoadResource_existing_0() throws Exception {
        URI existingResourceURI = URI.create("classpath:/classpath/classpathResource.tpl");
        Resource existingResource = (Resource) classPathLoader.loadResource(createCtx(), existingResourceURI,
                                                                            Collections.EMPTY_MAP).join();
        assertNotNull(existingResource);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testLoadResource_existing_1() throws Exception {
        URI existingResourceURI = URI.create("classpath:classpath/classpathResource.tpl");
        Resource existingResource = (Resource) classPathLoader.loadResource(createCtx(), existingResourceURI,
                                                                            Collections.EMPTY_MAP).join();
        assertNotNull(existingResource);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testLoadResource_notExisting() throws Exception {
        URI notExistingResourceURI = URI.create("classpath:/classpathResourceNotExist.tpl");
        try {
            classPathLoader.loadResource(createCtx(), notExistingResourceURI,
                                         Collections.EMPTY_MAP).join();
            fail("Exception should be thrown");
        } catch (Exception ex) {
            assertThat(ex.getMessage(),
                       is("Error with ResourceInputStream for file 'classpathResourceNotExist.tpl'"));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testLoadResource_badSchema_0() throws Exception {
        URI badSchemaURI = URI.create("file:/classpathResource.tpl");
        Resource resource = (Resource) classPathLoader.loadResource(createCtx(), badSchemaURI,
                                                                    Collections.EMPTY_MAP).join();
        assertNull(resource);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testLoadResource_badSchema_1() throws Exception {
        URI badSchemaURI = URI.create("classpath/classpathResource.tpl");
        Resource resource = (Resource) classPathLoader.loadResource(createCtx(), badSchemaURI,
                                                                    Collections.EMPTY_MAP).join();
        assertNull(resource);
    }

    public static Context createCtx() {
        return Context.createRoot("", null, null, null);
    }
}
package ru.histone.v2;

import org.junit.Test;
import ru.histone.HistoneException;

/**
 * Created by inv3r on 15/01/16.
 */
public class ConcreteTest extends BaseTest {

    @Test
    public void spacesTest() throws HistoneException {
        doTest("a {{if true}} true {{/if}} b", "a  true  b");
    }
}

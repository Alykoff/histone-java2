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

    @Test
    public void minusTest() throws HistoneException {
        doTest("a {{if -10}} true {{/if}} b", "a  true  b");
    }

    @Test
    public void expressionTest() throws HistoneException {
        doTest("a {{if [foo: 1]}} true {{/if}} b", "a  true  b");
    }
}

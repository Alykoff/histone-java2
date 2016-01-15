package ru.histone.v2;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.histone.HistoneException;
import ru.histone.v2.test.TestRunner;
import ru.histone.v2.test.dto.HistoneTestCase;

import java.util.List;

/**
 * Created by alexey.nevinsky on 25.12.2015.
 */
public class ParserTest extends BaseTest {

    private static final Logger LOG = LoggerFactory.getLogger(ParserTest.class);

    private void runTest(String filePath) throws HistoneException {
        List<HistoneTestCase> cases = TestRunner.loadTestCase(filePath);
        for (HistoneTestCase histoneTestCase : cases) {
            System.out.println("Run test '" + histoneTestCase.getName() + "'");
            for (HistoneTestCase.Case testCase : histoneTestCase.getCases()) {
                System.out.println("Expression: " + testCase.getInput());
                doTest(testCase.getInput(), testCase.getExpectedResult());
            }
        }
    }

    @Test
    public void ifTest() throws HistoneException {
        runTest("base/ifStatement.json");
    }
}

package ru.histone.v2;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.histone.HistoneException;
import ru.histone.v2.test.TestRunner;
import ru.histone.v2.test.dto.HistoneTestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * Created by alexey.nevinsky on 25.12.2015.
 */
@RunWith(Parameterized.class)
public class ParserTest extends BaseTest {
    private static final Logger LOG = LoggerFactory.getLogger(ParserTest.class);
    public static final String BASE_IF_STATEMENT_JSON = "base/ifStatement.json";

    private String input;
    private String expected;
    private Integer index;

    public ParserTest(Integer index, String input, String expected) {
        this.index = index;
        this.input = input;
        this.expected = expected;
    }

    @Parameterized.Parameters(name = "{index}: `{1}`")
    public static Collection<Object[]> data() {
        final List<Object[]> result = new ArrayList<>();
        final List<HistoneTestCase> histoneTestCases = TestRunner.loadTestCase(BASE_IF_STATEMENT_JSON);
        int i = 0;
        for (HistoneTestCase histoneTestCase : histoneTestCases) {
            System.out.println("Run test '" + histoneTestCase.getName() + "'");
            for (HistoneTestCase.Case testCase : histoneTestCase.getCases()) {
                System.out.println("Expression: " + testCase.getInput());
                result.add(new Object[] {
                        i++, testCase.getInput(), testCase.getExpectedResult()
                });
            }
        }
        return result;
    }

    @Test
    public void test() throws HistoneException {
        doTest(input, expected);
    }
}

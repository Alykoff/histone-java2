package ru.histone.v2;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import ru.histone.HistoneException;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.Evaluator;
import ru.histone.v2.parser.Parser;
import ru.histone.v2.parser.ParserException;
import ru.histone.v2.parser.node.AstNode;
import ru.histone.v2.test.TestRunner;
import ru.histone.v2.test.dto.HistoneTestCase;
import ru.histone.v2.utils.ParserUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by alexey.nevinsky on 25.12.2015.
 */
@RunWith(Parameterized.class)
public class EvaluatorTest {

    private String input;
    private HistoneTestCase.Case expected;
    private Integer index;

    public EvaluatorTest(Integer index, String input, HistoneTestCase.Case expected) {
        this.index = index;
        this.input = input;
        this.expected = expected;
    }

    @Parameterized.Parameters(name = "{index}: `{1}`")
    public static Collection<Object[]> data() throws IOException, URISyntaxException {
        final List<Object[]> result = new ArrayList<>();
        final List<HistoneTestCase> histoneTestCases = TestRunner.loadTestCases();
        int i = 0;
        for (HistoneTestCase histoneTestCase : histoneTestCases) {
            System.out.println("Run test '" + histoneTestCase.getName() + "'");
            for (HistoneTestCase.Case testCase : histoneTestCase.getCases()) {
                System.out.println("Expression: " + testCase.getInput());
                result.add(new Object[]{
                        i++, testCase.getInput(), testCase
                });
            }
        }
        return result;
    }

    @Test
    public void test() throws HistoneException {
        doTest(input, expected);
    }

    private void doTest(String input, HistoneTestCase.Case testCase) throws HistoneException {
        Parser parser = new Parser();
        Context context = new Context();
        Evaluator evaluator = new Evaluator();
        try {
            AstNode root = parser.process(input, "");
            if (testCase.getExpectedAST() != null) {
                Assert.assertEquals(ParserUtils.astToString(root), testCase.getExpectedAST());
            }
            if (testCase.getExpectedResult() != null) {
                String result = evaluator.process("", root, context);
                Assert.assertEquals(testCase.getExpectedResult(), result);
            }
        } catch (ParserException ex) {
            if (testCase.getExpectedException() != null) {
                HistoneTestCase.ExpectedException e = testCase.getExpectedException();
                Assert.assertEquals(ex.getLine(), e.getLine());
                Assert.assertEquals(ex.getMessage(), "unexpected '" + e.getFound() + "', expected '" + e.getExpected() + "'");
            } else {
                throw new RuntimeException(ex);
            }
        }
    }
}

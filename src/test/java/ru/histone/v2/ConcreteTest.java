package ru.histone.v2;

import org.junit.Assert;
import org.junit.Test;
import ru.histone.HistoneException;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.Evaluator;
import ru.histone.v2.parser.Parser;
import ru.histone.v2.parser.ParserException;
import ru.histone.v2.parser.node.AstNode;
import ru.histone.v2.test.dto.HistoneTestCase;
import ru.histone.v2.utils.ParserUtils;

/**
 * Created by inv3r on 19/01/16.
 */
public class ConcreteTest {
    @Test
    public void concreteTest() throws HistoneException {
        HistoneTestCase.Case testCase = new HistoneTestCase.Case();
        testCase.setExpectedAST("[\"a \", [1001, \"x\", [101, 10]], \" f\"]");
        doTest("a {{var simple = 10}} b {{simple}} c", testCase);
    }

    private void doTest(String input, HistoneTestCase.Case testCase) throws HistoneException {
        Parser parser = new Parser();
        Context context = new Context();
        Evaluator evaluator = new Evaluator();
        try {
            AstNode root = parser.process(input, "");
            if (testCase.getExpectedAST() != null) {
                Assert.assertEquals(ParserUtils.astToString(root), testCase.getExpectedAST());
            } else {
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

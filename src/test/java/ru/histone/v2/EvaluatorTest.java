package ru.histone.v2;

import org.junit.Assert;
import org.junit.Test;
import ru.histone.HistoneException;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.Evaluator;
import ru.histone.v2.parser.Parser;
import ru.histone.v2.parser.node.AstNode;

/**
 * Created by inv3r on 13/01/16.
 */
public class EvaluatorTest {
    @Test
    public void testIf() throws HistoneException {
        String ifStatement = "sdjhfsdjkbfdsksd {{if 1 = 1}}1231231{{else}}aaa{{/if}}sdklbjfsdlkfdsbfsdksfd";
        String expectedResult = "sdjhfsdjkbfdsksd 1231231sdklbjfsdlkfdsbfsdksfd";
        String baseUri = "";

        Parser parser = new Parser();
        AstNode ifNode = parser.process(ifStatement, "");
        Context context = new Context();
        Evaluator evaluator = new Evaluator();
        String result = evaluator.process(baseUri, ifNode, context);
        Assert.assertEquals(expectedResult, result);
    }
}

package ru.histone.v2;

import org.junit.Assert;
import org.junit.Test;
import ru.histone.v2.parser.Parser;
import ru.histone.v2.parser.ParserException;
import ru.histone.v2.parser.node.AstNode;
import ru.histone.v2.utils.ParserUtils;

/**
 * Created by alexey.nevinsky on 25.12.2015.
 */
public class ParserTest {
    @Test
    public void ifTest() throws ParserException {
        String ifStatement = "sdjhfsdjkbfdsksd {{if 1 = 1}}1231231{{else}}aaa{{/if}}sdklbjfsdlkfdsbfsdksfd";
        String ifResult = "[31,[-2147483648,'sdjhfsdjkbfdsksd'],[-2147483648,' '],[26,[31,[-2147483648,'1231231']],[19,[-2147483648,'1'],[-2147483648,'1']],[31,[-2147483648,'aaa']]],[-2147483648,'sdklbjfsdlkfdsbfsdksfd']]";
        Parser parser = new Parser();
        AstNode node = parser.process(ifStatement, "");
        Assert.assertEquals(ifResult, ParserUtils.astToString(node));
    }

    @Test
    public void ifTest2() throws ParserException {
        String ifStatement = "sdjhfsdjkbfdsksd {{if 1 = 1 && '2' != 'fdsds4'}}1231231{{else}}aaa{{/if}}sdklbjfsdlkfdsbfsdksfd";
        String ifResult = "[31,[-2147483648,'sdjhfsdjkbfdsksd'],[-2147483648,' '],[26,[31,[-2147483648,'1231231']],[6,[19,[-2147483648,'1'],[-2147483648,'1']],[20,[-2147483648,'2'],[-2147483648,'fdsds4']]],[31,[-2147483648,'aaa']]],[-2147483648,'sdklbjfsdlkfdsbfsdksfd']]";
        Parser parser = new Parser();
        AstNode node = parser.process(ifStatement, "");
        Assert.assertEquals(ifResult, ParserUtils.astToString(node));
    }
}

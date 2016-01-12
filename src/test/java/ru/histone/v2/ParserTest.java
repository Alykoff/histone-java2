package ru.histone.v2;

import org.junit.Test;
import ru.histone.v2.parser.Parser;
import ru.histone.v2.parser.ParserException;

/**
 * Created by alexey.nevinsky on 25.12.2015.
 */
public class ParserTest {
    @Test
    public void ifTest() throws ParserException {
        String ifStatement = "sdjhfsdjkbfdsksd {{if 1 = 1}}1231231{{else}}aaa{{/if}}sdklbjfsdlkfdsbfsdksfd";
        Parser parser = new Parser();
        parser.process(ifStatement, "");
    }
}

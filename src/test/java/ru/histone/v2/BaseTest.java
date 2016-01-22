package ru.histone.v2;

import org.apache.commons.lang.ObjectUtils;
import org.junit.Assert;
import ru.histone.HistoneException;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.Evaluator;
import ru.histone.v2.parser.Parser;
import ru.histone.v2.parser.ParserException;
import ru.histone.v2.parser.node.ExpAstNode;
import ru.histone.v2.test.dto.HistoneTestCase;
import ru.histone.v2.utils.ParserUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by inv3r on 21/01/16.
 */
public class BaseTest {

    protected void doTest(String input, HistoneTestCase.Case testCase) throws HistoneException {
        Parser parser = new Parser();

        Evaluator evaluator = new Evaluator();
        try {
            ExpAstNode root = parser.process(input, "");
            if (testCase.getExpectedAST() != null) {
                Assert.assertEquals(testCase.getExpectedAST(), ParserUtils.astToString(root));
            }
            if (testCase.getExpectedResult() != null) {
                Context context = new Context("");
                if (testCase.getContext() != null) {
                    context.getVars().putAll(convertContext(testCase));
                }
                String result = evaluator.process("", root, context);
                Assert.assertEquals(testCase.getExpectedResult(), result);
            }
        } catch (ParserException ex) {
            if (testCase.getExpectedException() != null) {
                HistoneTestCase.ExpectedException e = testCase.getExpectedException();
                Assert.assertEquals(e.getLine(), ex.getLine());
                Assert.assertEquals("unexpected '" + e.getFound() + "', expected '" + e.getExpected() + "'", ex.getMessage());
            } else {
                throw new RuntimeException(ex);
            }
        }
    }

    protected Map<String, Object> convertContext(HistoneTestCase.Case testCase) {
        Map<String, Object> res = new HashMap<>();
        for (Map.Entry<String, Object> entry : testCase.getContext().entrySet()) {
            if (entry.getValue() == null) {
                res.putIfAbsent(entry.getKey(), ObjectUtils.NULL);
            } else if (entry.getValue() instanceof List) {
                List list = (List) entry.getValue();
                Map<String, Object> map = new LinkedHashMap<>(list.size());
                for (int i = 0; i < list.size(); i++) {
                    map.put(i + "", getObjectValue(list.get(i)));
                }
                res.putIfAbsent(entry.getKey(), map);
            } else if (entry.getValue() instanceof Map) {
                Map<String, Object> m = (Map<String, Object>) entry.getValue();
                Map<String, Object> map = new LinkedHashMap<>(m.size());
                for (Map.Entry<String, Object> e : m.entrySet()) {
                    map.put(e.getKey(), getObjectValue(e.getValue()));
                }
                res.putIfAbsent(entry.getKey(), map);
            } else {
                res.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }
        return res;
    }

    protected boolean isDouble(Object value) {
        return value instanceof Double;
    }

    protected Object getObjectValue(Object value) {
        if (isDouble(value)) {
            Double v = (Double) value;
            if (v % 1 == 0 && v <= Integer.MAX_VALUE) {
                return v.longValue();
            } else {
                return v.floatValue();
            }
        } else {
            return value;
        }
    }
}

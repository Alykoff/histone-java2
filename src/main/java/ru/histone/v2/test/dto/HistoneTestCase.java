package ru.histone.v2.test.dto;

import java.util.List;

/**
 * Created by inv3r on 15/01/16.
 */
public class HistoneTestCase {
    private String name;
    private List<Case> cases;

    public String getName() {
        return name;
    }

    public List<Case> getCases() {
        return cases;
    }

    public static class Case {
        private String input;
        private String expectedResult;
        private ExpectedException expectedException;
        private String expectedAST;

        public String getInput() {
            return input;
        }

        public String getExpectedResult() {
            return expectedResult;
        }

        public ExpectedException getExpectedException() {
            return expectedException;
        }

        public String getExpectedAST() {
            return expectedAST;
        }
    }

    public static class ExpectedException {
        private int line;
        private String expected;
        private String found;

        public int getLine() {
            return line;
        }

        public String getExpected() {
            return expected;
        }

        public String getFound() {
            return found;
        }
    }


}

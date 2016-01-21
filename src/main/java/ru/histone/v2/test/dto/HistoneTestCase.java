package ru.histone.v2.test.dto;

import java.util.List;
import java.util.Map;

/**
 * Created by inv3r on 15/01/16.
 */
public class HistoneTestCase {
    private String name;
    private List<Case> cases;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Case> getCases() {
        return cases;
    }

    public void setCases(List<Case> cases) {
        this.cases = cases;
    }

    public static class Case {
        private String input;
        private String expectedResult;
        private ExpectedException expectedException;
        private String expectedAST;
        private Map<String, Object> context;

        public String getInput() {
            return input;
        }

        public void setInput(String input) {
            this.input = input;
        }

        public String getExpectedResult() {
            return expectedResult;
        }

        public void setExpectedResult(String expectedResult) {
            this.expectedResult = expectedResult;
        }

        public ExpectedException getExpectedException() {
            return expectedException;
        }

        public void setExpectedException(ExpectedException expectedException) {
            this.expectedException = expectedException;
        }

        public String getExpectedAST() {
            return expectedAST;
        }

        public void setExpectedAST(String expectedAST) {
            this.expectedAST = expectedAST;
        }

        public Map<String, Object> getContext() {
            return context;
        }

        public void setContext(Map<String, Object> context) {
            this.context = context;
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

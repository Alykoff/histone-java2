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
    }
}

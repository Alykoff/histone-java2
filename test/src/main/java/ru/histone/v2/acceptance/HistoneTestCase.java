/*
 * Copyright (c) 2016 MegaFon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.histone.v2.acceptance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * @author Alexey Nevinsky
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
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
        private String inputClass;
        private String inputAST;
        private String input;
        private String inputFile;
        private String baseURI;
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

        public String getInputFile() {
            return inputFile;
        }

        public void setInputFile(String inputFile) {
            this.inputFile = inputFile;
        }

        public String getBaseURI() {
            return baseURI;
        }

        public void setBaseURI(String baseURI) {
            this.baseURI = baseURI;
        }

        public String getInputAST() {
            return inputAST;
        }

        public void setInputAST(String inputAST) {
            this.inputAST = inputAST;
        }

        public String getInputClass() {
            return inputClass;
        }

        public void setInputClass(String inputClass) {
            this.inputClass = inputClass;
        }
    }


}

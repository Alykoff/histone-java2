/**
 *    Copyright 2013 MegaFon
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package ru.histone.evaluator;

import ru.histone.v2.exceptions.HistoneException;

/**
 * Evaluator exception, provides detailed information about evaluation error
 */
public class EvaluatorException extends HistoneException {
    private int lineNumber;

    private String expected;
    private String found;

    public EvaluatorException(Throwable cause) {
        super(cause);
    }

    public EvaluatorException(String message, Throwable cause) {
        super(message, cause);
    }

    public EvaluatorException(int lineNumber, String expected, String found) {
        super(generateMessage(lineNumber, expected, found));

        this.lineNumber = lineNumber;
        this.expected = expected;
        this.found = found;
    }

    public EvaluatorException(String string) {
        super(string);
    }

    private static String generateMessage(int lineNumber, String expected, String found) {
        StringBuilder sb = new StringBuilder();
        sb.append("Syntax error at [");
        sb.append(lineNumber);
//		sb.append(":");
//		sb.append(columnNumber);
        sb.append("]: expected='");
        sb.append(expected);
        sb.append("', found='");
        sb.append(found);
        sb.append("'");
        return sb.toString();
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getExpected() {
        return expected;
    }

    public String getFound() {
        return found;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EvaluatorException that = (EvaluatorException) o;

        if (lineNumber != that.lineNumber) return false;
        if (expected != null ? !expected.equals(that.expected) : that.expected != null) return false;
        if (found != null ? !found.equals(that.found) : that.found != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = lineNumber;
        result = 31 * result + (expected != null ? expected.hashCode() : 0);
        result = 31 * result + (found != null ? found.hashCode() : 0);
        return result;
    }
}

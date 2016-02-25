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
package ru.histone.parser;

import ru.histone.v2.exceptions.HistoneException;

/**
 * Parse exception, provides detailed information about parse error
 */
public class ParserException extends HistoneException {

	private int lineNumber;

	private String expected;
	private String found;

	public ParserException(int lineNumber, String expected, String found) {
		super(generateMessage(lineNumber, expected, found));

		this.lineNumber = lineNumber;
		this.expected = expected;
		this.found = found;
	}

	public ParserException(String msg) {
		super(msg);
	}

	private static String generateMessage(int lineNumber, String expected, String found) {
		StringBuilder sb = new StringBuilder();
		sb.append("Syntax error at line ");
		sb.append(lineNumber);
		sb.append(": expected='");
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

}

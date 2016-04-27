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

package ru.histone.v2.exceptions;

/**
 *
 * @author Alexey Nevinsky
 */
public class ParserException extends HistoneException {
    protected final String baseURI;
    protected final int line;

    public ParserException(String message, String baseURI, int line) {
        super(message);
        this.baseURI = baseURI;
        this.line = line;
    }

    public String getBaseURI() {
        return baseURI;
    }

    public int getLine() {
        return line;
    }
}

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

import org.junit.jupiter.api.Assertions;

/**
 * @author Alexey Nevinsky
 */
public class TestUtils {

    private static String normalizeLineEndings(String value) {
        return value.replaceAll("\\r\\n", "\n");
    }

    public static void assertEquals(String expected, String actual) {
        Assertions.assertEquals(normalizeLineEndings(expected), normalizeLineEndings(actual));
    }

    public static void checkException(Exception e, ExpectedException expectedException) {
        if (expectedException.getMessage() != null) {
            Assertions.assertEquals(expectedException.getMessage(), e.getMessage());
        } else {
            Assertions.assertEquals("unexpected '" + expectedException.getFound() + "', expected '" + expectedException.getExpected() + "'", e.getMessage());
        }
    }
}

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

package ru.histone.v2.utils;

import ru.histone.v2.parser.node.AstNode;
import ru.histone.v2.parser.node.AstType;
import ru.histone.v2.parser.node.ExpAstNode;
import ru.histone.v2.parser.node.StringAstNode;

import java.util.Optional;

/**
 * @author Alexey Nevinsky on 12.01.2016.
 */
public class ParserUtils {
    public static ExpAstNode createNopNode(String name) {
        final StringAstNode nameNode = new StringAstNode(name);
        return new ExpAstNode(AstType.AST_NOP).add(nameNode);
    }

    public static ExpAstNode createNopNode(String name, AstNode node) {
        final StringAstNode nameNode = new StringAstNode(name);
        return new ExpAstNode(AstType.AST_NOP).add(nameNode).add(node);
    }

    public static String getValueFromStringNode(AstNode node) {
        return ((StringAstNode) node).getValue();
    }

    public static boolean isStrongString(Object obj) {
        return obj instanceof String;
    }

    public static boolean isStrongNumber(Object obj) {
        return obj instanceof Integer || obj instanceof Long;
    }

    public static boolean isNumber(Object value) {
        if (value instanceof Number) {
            return true;
        } else if (!isStrongString(value)) {
            return false;
        }

        String v = (String) value;
        return tryInt(v).isPresent() || tryDouble(v).isPresent();
    }

    public static boolean isInt(String value) {
        return tryInt(value).isPresent();
    }

    public static Optional<Integer> tryInt(String value) {
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (Exception ignore) {
            return Optional.empty();
        }
    }

    public static Optional<Double> tryDouble(Object value) {
        if (value instanceof Integer) {
            return Optional.of(new Double((Integer) value));
        }
        if (value instanceof Long) {
            return Optional.of(new Double((Long) value));
        }
        if (value instanceof Double) {
            return Optional.of((Double) value);
        }
        if (value instanceof String) {
            try {
                return Optional.of(Double.parseDouble((String) value));
            } catch (Exception ignore) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public static Optional<Integer> tryIntNumber(Object value) {
        return tryDouble(value).flatMap(doubleValue -> {
            if ((doubleValue == Math.floor(doubleValue)) && !Double.isInfinite(doubleValue) && doubleValue <= Integer.MAX_VALUE && doubleValue >= Integer.MIN_VALUE) {
                return Optional.of(doubleValue.intValue());
            }
            return Optional.empty();
        });
    }

    public static Optional<Long> tryLongNumber(Object value) {
        return tryDouble(value).flatMap(longValue -> {
            if ((longValue == Math.floor(longValue)) && !Double.isInfinite(longValue) && longValue <= Long.MAX_VALUE && longValue >= Long.MIN_VALUE) {
                return Optional.of(longValue.longValue());
            }
            return Optional.empty();
        });
    }
}

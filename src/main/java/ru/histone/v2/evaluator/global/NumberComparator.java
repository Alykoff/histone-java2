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

package ru.histone.v2.evaluator.global;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Comparator;

/**
 *
 * Created by gali.alykoff on 22/01/16.
 */
public class NumberComparator implements Comparator<Number>, Serializable {
    private static boolean isSpecial(final Number x) {
        boolean specialDouble = x instanceof Double
                && (Double.isNaN((Double) x) || Double.isInfinite((Double) x));
        boolean specialFloat = x instanceof Float
                && (Float.isNaN((Float) x) || Float.isInfinite((Float) x));
        return specialDouble || specialFloat;
    }

    private static BigDecimal toBigDecimal(final Number number) {
        if(number instanceof Byte || number instanceof Short
                || number instanceof Integer || number instanceof Long) {
            return new BigDecimal(number.longValue());
        }
        if(number instanceof Float || number instanceof Double) {
            return new BigDecimal(number.doubleValue());
        }

        try {
            return new BigDecimal(number.toString());
        } catch(final NumberFormatException e) {
            throw new RuntimeException("The given number (\"" + number
                    + "\" of class " + number.getClass().getName()
                    + ") does not have a parsable string representation", e);
        }
    }

    public int compare(final Number x, final Number y) {
        if (isSpecial(x) || isSpecial(y)) {
            return Double.compare(x.longValue(), y.longValue());
        } else {
            return toBigDecimal(x).compareTo(toBigDecimal(y));
        }
    }
}

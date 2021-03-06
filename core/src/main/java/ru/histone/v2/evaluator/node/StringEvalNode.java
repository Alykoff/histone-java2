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

package ru.histone.v2.evaluator.node;

import ru.histone.v2.evaluator.Converter;
import ru.histone.v2.exceptions.HistoneException;
import ru.histone.v2.rtti.HistoneType;

import static ru.histone.v2.utils.ParserUtils.isInteger;

/**
 * @author Alexey Nevinsky
 */
public class StringEvalNode extends EvalNode<String> implements HasProperties {
    public StringEvalNode(String value) {
        super(value);
        if (value == null) {
            throw new NullPointerException();
        }
    }

    @Override
    public HistoneType getType() {
        return HistoneType.T_STRING;
    }

    @Override
    public EvalNode getProperty(Converter converter, Object propertyName) throws HistoneException {
        final int size = value.length();
        if (!(propertyName instanceof Double) && !(propertyName instanceof Long)) {
            return null;
        }
        final int indexRaw;
        if (propertyName instanceof Double) {
            if (isInteger((Double) propertyName)) {
                indexRaw = ((Double) propertyName).intValue();
            } else {
                return converter.createEvalNode(null);
            }
        } else { // Long
            indexRaw = ((Long) propertyName).intValue();
        }

        if (indexRaw >= size || size + indexRaw <= 0) {
            return converter.createEvalNode(null);
        }

        final int index;
        if (indexRaw < 0) {
            index = size + indexRaw;
        } else {
            index = indexRaw;
        }
        return converter.createEvalNode(value.charAt(index) + "");
    }
}

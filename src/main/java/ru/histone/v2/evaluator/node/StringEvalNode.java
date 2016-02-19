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

import ru.histone.HistoneException;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.rtti.HistoneType;

import java.util.concurrent.CompletableFuture;

/**
 * @author alexey.nevinsky
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
    public EvalNode getProperty(Object propertyName) throws HistoneException {
        final int size = value.length();
        if (!(propertyName instanceof Double) && !(propertyName instanceof Long)) {
            return null;
        }
        int index;
        if (propertyName instanceof Double) {
            index = ((Double) propertyName).intValue();
        } else { // Long
            index = ((Long) propertyName).intValue();
        }

        if (index >= size) {
            return EmptyEvalNode.INSTANCE;
        }

        if (size + index <= 0) {
            return EmptyEvalNode.INSTANCE;
        }

        if (index < 0) {
            index = size + index;
        }
        return EvalUtils.createEvalNode(value.charAt(index) + "");
    }
}

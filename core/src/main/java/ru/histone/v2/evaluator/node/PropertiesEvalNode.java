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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Alexey Nevinsky
 */
public abstract class PropertiesEvalNode<T> extends EvalNode<T> implements HasProperties {
    protected Map<String, EvalNode> extArgs = new LinkedHashMap<>();

    public PropertiesEvalNode(T value) {
        super(value);
    }

    public Map<String, EvalNode> getExtArgs() {
        return this.extArgs;
    }


    @Override
    public EvalNode getProperty(Converter converter, Object propertyName) throws HistoneException {
        return this.extArgs.get(propertyName + "");
    }
}

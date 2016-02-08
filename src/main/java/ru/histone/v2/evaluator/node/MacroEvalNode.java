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
import ru.histone.v2.evaluator.data.HistoneMacro;
import ru.histone.v2.rtti.HistoneType;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 *
 * Created by gali.alykoff on 27/01/16.
 */
public class MacroEvalNode extends EvalNode<HistoneMacro> implements HasProperties, Serializable {
    private Map<String, EvalNode> extArgs = new LinkedHashMap<>();
    public MacroEvalNode(HistoneMacro value) {
        super(value);
        if (value == null) {
            throw new NullPointerException();
        }
    }

    public MacroEvalNode(HistoneMacro value, Map<String, EvalNode> extArgs) {
        super(value);
        if (value == null) {
            throw new NullPointerException();
        }
        this.extArgs.putAll(extArgs);
    }

    @Override
    public HistoneType getType() {
        return HistoneType.T_MACRO;
    }

    public MacroEvalNode putAllExtArgs(Map<String, EvalNode> extArgs) {
        this.extArgs.putAll(extArgs);
        return this;
    }

    @Override
    public EvalNode getProperty(Object propertyName) throws HistoneException {
        return this.extArgs.get(propertyName);
    }

}

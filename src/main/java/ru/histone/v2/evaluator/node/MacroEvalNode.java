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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author gali.alykoff
 */
public class MacroEvalNode extends EvalNode<Map<String, HistoneMacro>> implements HasProperties, Serializable {
    private Map<String, EvalNode> extArgs = new LinkedHashMap<>();
    private boolean required = false;

    public MacroEvalNode(HistoneMacro value) {
        this(value, Collections.emptyMap());
    }

    public MacroEvalNode(HistoneMacro value, Map<String, EvalNode> extArgs) {
        super(Collections.singletonMap(null, value));
        if (value == null) {
            throw new NullPointerException();
        }
        this.extArgs.putAll(extArgs);
    }

    public MacroEvalNode(Map<String, HistoneMacro> macroses, Map<String, EvalNode> extArgs) {
        super(macroses);
        if (value == null) {
            throw new NullPointerException();
        }
        this.extArgs.putAll(extArgs);
        required = true;
    }

    public HistoneMacro getMacro() {
        return getValue().get(null);
    }

    public boolean isRequired() {
        return required;
    }

    @Override
    public HistoneType getType() {
        return HistoneType.T_MACRO;
    }

    public MacroEvalNode putAllExtArgs(Map<String, EvalNode> extArgs) {
        for (Map.Entry<String, EvalNode> arg : extArgs.entrySet()) {
            this.extArgs.putIfAbsent(arg.getKey(), arg.getValue());
        }
        return this;
    }

    public Map<String, EvalNode> getExtArgs() {
        return this.extArgs;
    }

    @Override
    public EvalNode getProperty(Object propertyName) throws HistoneException {
        return this.extArgs.get(propertyName);
    }

}

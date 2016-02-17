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

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.data.HistoneMacro;
import ru.histone.v2.rtti.HistoneType;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author alexey.nevinsky
 */
public class RequireEvalNode extends PropertiesEvalNode<Map<String, HistoneMacro>> implements HasProperties, Serializable {
    private EvalNode mainValue = null;

    public RequireEvalNode(Context ctx) {
        super(new HashMap<>());

        for (Map.Entry<String, CompletableFuture<EvalNode>> entry : ctx.getVars().entrySet()) {
            EvalNode node = entry.getValue().join();
            if (node.getType() == HistoneType.T_MACRO) {
                HistoneMacro macro = ((MacroEvalNode) node).getValue();
                value.put(entry.getKey(), macro);
            } else {
                extArgs.put(entry.getKey(), node);
            }
        }
    }

    public RequireEvalNode(EvalNode mainValue) {
        super(Collections.emptyMap());
        this.mainValue = mainValue;
    }

    @Override
    public HistoneType getType() {
        return HistoneType.T_REQUIRE;
    }

    public EvalNode getMainValue() {
        return mainValue;
    }
}

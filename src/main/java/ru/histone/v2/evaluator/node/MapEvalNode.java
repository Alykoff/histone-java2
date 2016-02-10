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
import ru.histone.v2.rtti.HistoneType;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author alexey.nevinsky
 */
public class MapEvalNode extends EvalNode<Map<String, EvalNode>> implements HasProperties, Serializable {

    public MapEvalNode(List<EvalNode> values) {
        super(new LinkedHashMap<>());
        for (int i = 0; i < values.size(); i++) {
            this.value.put(i + "", values.get(i));
        }
    }

    public MapEvalNode(Map<String, EvalNode> value) {
        super(value);
    }

    @Override
    public HistoneType getType() {
        return HistoneType.T_ARRAY;
    }

    public void append(MapEvalNode node) {
        int maxIndex = getMaxIndex();
        for (Map.Entry<String, EvalNode> entry : node.getValue().entrySet()) {
            if (convertToIndex(entry.getKey()) != null) {
                value.put(++maxIndex + "", entry.getValue());
            } else {
                value.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private int getMaxIndex() {
        int maxIndex = -1;
        for (String key : value.keySet()) {
            Integer index = convertToIndex(key);
            if (index != null) {
                maxIndex = index;
            }
        }
        return maxIndex;
    }

    private Integer convertToIndex(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public EvalNode getProperty(Object propertyName) throws HistoneException {
        if (propertyName instanceof String) {
            final String[] propArray = ((String) propertyName).split("\\.");
            EvalNode curr = this;
            for (String str : propArray) {
                if (curr instanceof MapEvalNode) {
                    curr = ((MapEvalNode) curr).value.get(str);
                } else {
                    throw new HistoneException("Unable to find property '" + str + "'");
                }
            }

            return curr;
        } else {
            return value.get(propertyName + "");
        }
    }

}

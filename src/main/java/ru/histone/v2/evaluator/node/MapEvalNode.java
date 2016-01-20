package ru.histone.v2.evaluator.node;

import org.apache.commons.lang.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by inv3r on 19/01/16.
 */
public class MapEvalNode extends EvalNode<Map<String, Object>> {
    public MapEvalNode(Map<String, Object> value) {
        super(value);
    }

    @Override
    public String asString() {
        Set<Object> set = new HashSet<>();
        for (Object obj : value.values()) {
            if (obj != null) {
                set.add(obj);
            }
        }
        return StringUtils.join(set, " ");
    }

    public void append(MapEvalNode node) {
        int maxIndex = getMaxIndex();
        for (Map.Entry<String, Object> entry : node.getValue().entrySet()) {
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
}

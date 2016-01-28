package ru.histone.v2.evaluator.node;

import ru.histone.HistoneException;

import java.util.Map;

/**
 * Created by inv3r on 19/01/16.
 */
public class MapEvalNode extends EvalNode<Map<String, Object>> {
    public MapEvalNode(Map<String, Object> value) {
        super(value);
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

    public Object getProperty(String propertyName) throws HistoneException {
        String[] propArray = propertyName.split("\\.");

        Object v = value;
        Object curr = null;
        for (String str : propArray) {
            if (v instanceof Map) {
                curr = ((Map<String, Object>) v).get(str);
            } else {
                throw new HistoneException("Unable to find property '" + str + "'");
            }
        }

        return curr;
    }

}

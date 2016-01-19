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
}

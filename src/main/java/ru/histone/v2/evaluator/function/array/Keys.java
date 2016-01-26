package ru.histone.v2.evaluator.function.array;

import ru.histone.v2.evaluator.Function;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.utils.ParserUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by inv3r on 25/01/16.
 */
public class Keys implements Function {

    private boolean isKeys;

    public Keys(boolean isKeys) {
        this.isKeys = isKeys;
    }

    @Override
    public String getName() {
        return isKeys ? "keys" : "values";
    }

    @Override
    public EvalNode execute(List<EvalNode> args) throws FunctionExecutionException {
        Map<String, Object> map = (Map<String, Object>) args.get(0).getValue();
        Collection set = isKeys ? map.keySet() : map.values();

        Map<String, Object> res = new LinkedHashMap<>(set.size());
        int i = 0;
        for (Object key : set) {
            if (ParserUtils.isInt(key.toString())) {
                res.put(i + "", Integer.valueOf(key.toString()));
            } else {
                res.put(i + "", key);
            }
            i++;
        }

        return new MapEvalNode(res);
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isClear() {
        return true;
    }
}

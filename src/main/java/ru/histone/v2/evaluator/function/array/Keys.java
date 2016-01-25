package ru.histone.v2.evaluator.function.array;

import ru.histone.v2.evaluator.Function;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.exceptions.GlobalFunctionExecutionException;
import ru.histone.v2.utils.ParserUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by inv3r on 25/01/16.
 */
public class Keys implements Function {
    @Override
    public String getName() {
        return "keys";
    }

    @Override
    public EvalNode execute(List<EvalNode> args) throws GlobalFunctionExecutionException {
        Map<String, Object> map = (Map<String, Object>) args.get(0).getValue();
        Set<String> keys = map.keySet();

        Map<String, Object> res = new LinkedHashMap<>(keys.size());
        int i = 0;
        for (String key : keys) {
            if (ParserUtils.isInt(key)) {
                res.put(i + "", Integer.valueOf(key));
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

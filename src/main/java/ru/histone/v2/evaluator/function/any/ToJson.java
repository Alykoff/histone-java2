package ru.histone.v2.evaluator.function.any;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import ru.histone.v2.evaluator.Function;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.StringEvalNode;
import ru.histone.v2.exceptions.GlobalFunctionExecutionException;
import ru.histone.v2.utils.ParserUtils;

import java.io.IOException;
import java.util.*;

/**
 * Created by inv3r on 22/01/16.
 */
public class ToJson implements Function {
    @Override
    public String getName() {
        return "toJSON";
    }

    @Override
    public EvalNode execute(List<EvalNode> args) throws GlobalFunctionExecutionException {
        ObjectMapper mapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addSerializer(LinkedHashMap.class, new JsonSerializer<LinkedHashMap>() {

            @Override
            public void serialize(LinkedHashMap value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                Set<?> keys = value.keySet();
                boolean isArray = true;
                int i = 0;
                for (Object key : keys) {
                    if (!ParserUtils.isInt((String) key) || Integer.parseInt((String) key) != i) {
                        isArray = false;
                        break;
                    }
                    i++;
                }

                if (isArray) {
                    JsonSerializer<Object> serializer = provider.findValueSerializer(Collection.class, null);
                    serializer.serialize(value.values(), jgen, provider);
                } else {
                    JsonSerializer<Object> serializer = provider.findValueSerializer(Map.class, null);
                    serializer.serialize(value, jgen, provider);
                }
            }
        });
        mapper.registerModule(module);

        try {
            String res = mapper.writeValueAsString(args.get(0).getValue());
            return new StringEvalNode(res);
        } catch (JsonProcessingException e) {
            throw new GlobalFunctionExecutionException("Failed to write object to json", e);
        }
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
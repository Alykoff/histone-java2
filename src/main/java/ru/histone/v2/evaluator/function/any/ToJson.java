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

package ru.histone.v2.evaluator.function.any;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.ParserUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Created by inv3r on 22/01/16.
 */
public class ToJson extends AbstractFunction {
    @Override
    public String getName() {
        return "toJSON";
    }

    @Override
    public CompletableFuture<EvalNode> execute(String baseUri, Locale locale, List<EvalNode> args) throws FunctionExecutionException {
        EvalNode node = args.get(0);
        if (node.getType() == HistoneType.T_NULL) {
            return EvalUtils.getValue("null");
        }

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
                } else if (value.isEmpty()) {
                    JsonSerializer<Object> serializer = provider.findValueSerializer(Collection.class, null);
                    serializer.serialize(Collections.emptyList(), jgen, provider);
                } else {
                    JsonSerializer<Object> serializer = provider.findValueSerializer(Map.class, null);
                    serializer.serialize(value, jgen, provider);
                }
            }
        });
        mapper.registerModule(module);

        try {
            String res = mapper.writeValueAsString(args.get(0).getValue());
            return EvalUtils.getValue(res);
        } catch (JsonProcessingException e) {
            throw new FunctionExecutionException("Failed to write object to json", e);
        }
    }
}
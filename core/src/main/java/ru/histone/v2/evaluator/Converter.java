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

package ru.histone.v2.evaluator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import ru.histone.v2.evaluator.data.HistoneMacro;
import ru.histone.v2.evaluator.data.HistoneRegex;
import ru.histone.v2.evaluator.node.*;
import ru.histone.v2.exceptions.HistoneException;
import ru.histone.v2.exceptions.StopExecutionException;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.ParserUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static ru.histone.v2.utils.ParserUtils.canBeLong;
import static ru.histone.v2.utils.ParserUtils.isInteger;

/**
 * @author Alexey Nevinsky
 */
public class Converter {
    public boolean nodeAsBoolean(EvalNode node) {
        if (node instanceof NullEvalNode) {
            return false;
        } else if (node instanceof EmptyEvalNode) {
            return false;
        } else if (node instanceof BooleanEvalNode) {
            return (Boolean) node.getValue();
        } else if (node instanceof LongEvalNode) {
            return ((Long) node.getValue()) != 0;
        } else if (node instanceof DoubleEvalNode) {
            return ((Double) node.getValue()) != 0;
        } else if (node instanceof StringEvalNode) {
            return !node.getValue().equals("");
        }
        return true;
    }

    public Optional<Integer> tryPureIntegerValue(EvalNode node) {
        if (!(isNumberNode(node) || node instanceof StringEvalNode)) {
            return Optional.empty();
        }
        if (node instanceof DoubleEvalNode) {
            final Double value = ((DoubleEvalNode) node).getValue();
            if (isInteger(value)) {
                return Optional.of(value).map(Double::intValue);
            } else {
                return Optional.empty();
            }
        } else if (node instanceof LongEvalNode) {
            final Long value = ((LongEvalNode) node).getValue();
            return Optional.ofNullable(value).map(Long::intValue);
        } else if (node instanceof StringEvalNode) {
            try {
                final double value = Double.valueOf(((StringEvalNode) node).getValue());
                if (isInteger(value)) {
                    return Optional.of(value).map(Double::intValue);
                } else {
                    return Optional.empty();
                }
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    public Number getNumberValue(EvalNode node) {
        if (!(isNumberNode(node) || node instanceof StringEvalNode)) {
            throw new RuntimeException();
        }
        if (node instanceof DoubleEvalNode) {
            return ((DoubleEvalNode) node).getValue();
        } else if (node instanceof LongEvalNode) {
            return ((LongEvalNode) node).getValue();
        } else if (node instanceof StringEvalNode) {
            return Double.valueOf(((StringEvalNode) node).getValue());
        } else {
            throw new NotImplementedException("");
        }
    }

    public boolean isStringNode(EvalNode node) {
        return node.getType() == HistoneType.T_STRING;
    }

    public boolean isNumberNode(EvalNode node) {
        return node instanceof LongEvalNode || node instanceof DoubleEvalNode;
    }

    public boolean isNumeric(StringEvalNode evalNode) {
        return isNumeric(evalNode.getValue());
    }

    public boolean isNumeric(String v) {
        try {
            final Double value = Double.valueOf(v);
            return !Double.isNaN(value) && Double.isFinite(value);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public EvalNode<?> createEvalNode(Object object) {
        if (object == null) {
            return new EmptyEvalNode();
        }
        if (object.equals(ObjectUtils.NULL)) {
            return new NullEvalNode();
        }
        if (object instanceof Boolean) {
            return new BooleanEvalNode((Boolean) object);
        }
        if (object instanceof Double) {
            return new DoubleEvalNode((Double) object);
        }
        if (object instanceof Long) {
            return new LongEvalNode((Long) object);
        }
        if (object instanceof String) {
            return new StringEvalNode((String) object);
        }
        if (object instanceof Map) {
            return new MapEvalNode((Map<String, EvalNode>) object);
        }
        if (object instanceof HistoneRegex) {
            return new RegexEvalNode((HistoneRegex) object);
        }
        if (object instanceof HistoneMacro) {
            return new MacroEvalNode((HistoneMacro) object);
        }
        if (object instanceof EvalNode) {
            return (EvalNode) object;
        }
        if (object instanceof Float) {
            return new DoubleEvalNode(((Float) object).doubleValue());
        }
        if (object instanceof Integer) {
            return new LongEvalNode(((Integer) object).longValue());
        }

        if (object instanceof Serializable) {
            ObjectMapper mapper = new ObjectMapper();
            final byte[] serialization;
            try {
                serialization = mapper.writeValueAsBytes(object);
                final Map result = mapper.readValue(serialization, Map.class);

                return constructFromMap(result);
            } catch (IOException ignore) {
                //we are ignore this exception
            }
        }

        throw new HistoneException("Didn't resolve object class: " + object.getClass());
    }

    public EvalNode<?> createEvalNode(Object object, boolean isDate) {
        if (object instanceof Map && isDate) {
            return new DateEvalNode((Map<String, EvalNode>) object);
        }
        return createEvalNode(object);
    }

    public MapEvalNode constructFromMap(Map<String, Object> map) {
        Map<String, EvalNode> res = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            res.put(entry.getKey(), constructFromObject(entry.getValue()));
        }
        return new MapEvalNode(res);
    }

    public MapEvalNode constructFromList(List<Object> list) {
        List<EvalNode> res = new ArrayList<>(list.size());
        res.addAll(list.stream()
                       .map(this::constructFromObject)
                       .collect(Collectors.toList())
        );
        return new MapEvalNode(res);
    }

    public EvalNode constructFromObject(Object object) {
        if (object instanceof Map) {
            return constructFromMap((Map) object);
        } else if (object instanceof List) {
            return constructFromList((List) object);
        } else if (object instanceof String && object.equals("undefined")) {
            return createEvalNode(null);
        } else if (object == null) {
            return createEvalNode(ObjectUtils.NULL);
        }
        return createEvalNode(object);
    }

    public CompletableFuture<EvalNode> getValue(Object v) {
        return CompletableFuture.completedFuture(createEvalNode(v));
    }

    public EvalNode getNumberNode(Double v) {
        if (canBeLong(v)) {
            return createEvalNode(v.longValue());
        }
        return createEvalNode(v);
    }

    public CompletableFuture<EvalNode> getNumberFuture(Double v) {
        return CompletableFuture.completedFuture(getNumberNode(v));
    }

    public boolean isArray(Set<String> indexKeys) {
        int i = 0;
        for (String key : indexKeys) {
            if (!isNumeric(key) || ParserUtils.tryInt(key).get() != i) {
                return false;
            }
            i++;
        }
        return true;
    }

    public java.util.function.Function<Throwable, EvalNode> checkThrowable(Logger log) {
        return e -> {
            if (e.getCause() instanceof StopExecutionException) {
                throw (StopExecutionException) e.getCause();
            }
            log.error(e.getMessage(), e);
            return createEvalNode(null);
        };
    }
}

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

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import ru.histone.v2.evaluator.data.HistoneMacro;
import ru.histone.v2.evaluator.data.HistoneRegex;
import ru.histone.v2.evaluator.node.*;
import ru.histone.v2.exceptions.HistoneException;
import ru.histone.v2.exceptions.StopExecutionException;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.ParserUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Alexey Nevinsky
 */
public class EvalUtils {
    public static boolean nodeAsBoolean(EvalNode node) {
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

    public static Double parseDouble(String value) throws NumberFormatException {
        return Double.parseDouble(value);
    }

    public static Optional<Integer> tryPureIntegerValue(EvalNode node) {
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
                final double value = Double.parseDouble(((StringEvalNode) node).getValue());
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

    public static boolean isInteger(Double value) {
        return value != null
                && value % 1 == 0
                && value >= Integer.MIN_VALUE
                && value <= Integer.MAX_VALUE;
    }

    public static Number getNumberValue(EvalNode node) {
        if (!(isNumberNode(node) || node instanceof StringEvalNode)) {
            throw new RuntimeException();
        }
        if (node instanceof DoubleEvalNode) {
            return ((DoubleEvalNode) node).getValue();
        } else if (node instanceof LongEvalNode) {
            return ((LongEvalNode) node).getValue();
        } else if (node instanceof StringEvalNode) {
            return Double.parseDouble(((StringEvalNode) node).getValue());
        } else {
            throw new NotImplementedException("");
        }
    }

    public static boolean isStringNode(EvalNode node) {
        return node.getType() == HistoneType.T_STRING;
    }

    public static boolean isNumberNode(EvalNode node) {
        return node instanceof LongEvalNode || node instanceof DoubleEvalNode;
    }

    public static boolean isNumeric(StringEvalNode evalNode) {
        return isNumeric(evalNode.getValue());
    }

    public static boolean isNumeric(String v) {
        try {
            final Double value = parseDouble(v);
            return !Double.isNaN(value) && Double.isFinite(value);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static EvalNode<?> createEvalNode(Object object) {
        if (object == null) {
            return new EmptyEvalNode();
        }
        if (object.equals(ObjectUtils.NULL)) {
            return new NullEvalNode();
        }
        if (object instanceof Boolean) {
            return new BooleanEvalNode((Boolean) object);
        }
        if (object instanceof Integer) {
            return new LongEvalNode(((Integer) object).longValue());
        }
        if (object instanceof Float) {
            return new DoubleEvalNode(((Float) object).doubleValue());
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
        throw new HistoneException("Didn't resolve object class: " + object.getClass());
    }

    public static EvalNode<?> createEvalNode(Object object, boolean isDate) {
        if (object instanceof Map && isDate) {
            return new DateEvalNode((Map<String, EvalNode>) object);
        }
        return createEvalNode(object);
    }

    public static MapEvalNode constructFromMap(Map<String, Object> map) {
        Map<String, EvalNode> res = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            res.put(entry.getKey(), constructFromObject(entry.getValue()));
        }
        return new MapEvalNode(res);
    }

    public static MapEvalNode constructFromList(List<Object> list) {
        List<EvalNode> res = new ArrayList<>(list.size());
        res.addAll(list.stream()
                .map(EvalUtils::constructFromObject)
                .collect(Collectors.toList())
        );
        return new MapEvalNode(res);
    }

    public static EvalNode constructFromObject(Object object) {
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

    public static CompletableFuture<EvalNode> getValue(Object v) {
        return CompletableFuture.completedFuture(createEvalNode(v));
    }

    public static boolean canBeLong(Double v) {
        return v % 1 == 0 && v <= Long.MAX_VALUE && v >= Long.MIN_VALUE;
    }

    public static EvalNode getNumberNode(Double v) {
        if (canBeLong(v)) {
            return EvalUtils.createEvalNode(v.longValue());
        }
        return EvalUtils.createEvalNode(v);
    }

    public static CompletableFuture<EvalNode> getNumberFuture(Double v) {
        return CompletableFuture.completedFuture(getNumberNode(v));
    }

    public static String escape(String str) {
        return StringEscapeUtils.escapeHtml4(str);
    }

    public static boolean isAst(String template) {
        Pattern pattern = Pattern.compile("^\\s*\\[\\s*[0-9]+.*\\]\\s*$");
        return pattern.matcher(template).matches();
    }

    public static boolean isArray(Set<String> indexKeys) {
        int i = 0;
        for (String key : indexKeys) {
            if (!isNumeric(key) || ParserUtils.tryInt(key).get() != i) {
                return false;
            }
            i++;
        }
        return true;
    }

    public static java.util.function.Function<Throwable, EvalNode> checkThrowable(Logger log) {
        return e -> {
            if (e.getCause() instanceof StopExecutionException) {
                throw (StopExecutionException) e.getCause();
            }
            log.error(e.getMessage(), e);
            return EvalUtils.createEvalNode(null);
        };
    }
}

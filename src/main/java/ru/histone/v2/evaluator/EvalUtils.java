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

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.ObjectUtils;
import ru.histone.v2.evaluator.data.HistoneRegex;
import ru.histone.v2.evaluator.node.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by inv3r on 14/01/16.
 */
public class EvalUtils {
    public static boolean equalityNode(EvalNode node1, EvalNode node2) {
        //todo normal equality logic
        return ObjectUtils.equals(node1.getValue(), node2.getValue());
    }

    public static boolean nodeAsBoolean(EvalNode node) {
        if (node instanceof NullEvalNode) {
            return false;
        } else if (node instanceof EmptyEvalNode) {
            return false;
        } else if (node instanceof BooleanEvalNode) {
            return (Boolean) node.getValue();
        } else if (node instanceof LongEvalNode) {
            return ((Long) node.getValue()) != 0;
        } else if (node instanceof StringEvalNode) {
            return !node.getValue().equals("");
        }
        return true;
    }

    public static Float parseFloat(String value) throws NumberFormatException {
        return Float.parseFloat(value);
    }

    public static Number getNumberValue(EvalNode node) {
        if (!(isNumberNode(node) || node instanceof StringEvalNode)) {
            throw new RuntimeException();
        }
        if (node instanceof FloatEvalNode) {
            return ((FloatEvalNode) node).getValue();
        } else if (node instanceof LongEvalNode) {
            return ((LongEvalNode) node).getValue();
        } else if (node instanceof StringEvalNode) {
            return Float.parseFloat(((StringEvalNode) node).getValue());
        } else {
            throw new NotImplementedException();
        }
    }

    public static boolean isNumberNode(EvalNode node) {
        return node instanceof LongEvalNode || node instanceof FloatEvalNode;
    }

    public static boolean isNumeric(StringEvalNode evalNode) {
        return isNumeric(evalNode.getValue());
    }

    public static boolean isNumeric(String v) {
        try {
            final Float value = parseFloat(v);
            return !Float.isNaN(value) && Float.isFinite(value);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static EvalNode<?> createEvalNode(Object object) {
        if (object == null) {
            return EmptyEvalNode.INSTANCE;
        }
        if (object.equals(ObjectUtils.NULL)) {
            return NullEvalNode.INSTANCE;
        }
        if (object instanceof Boolean) {
            return new BooleanEvalNode((Boolean) object);
        }
        if (object instanceof Float) {
            return new FloatEvalNode((Float) object);
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
        if (object instanceof EvalNode) {
            return (EvalNode) object;
        }
        return new ObjectEvalNode(object);
    }


    public static CompletableFuture<EvalNode> getValue(Object v) {
        return CompletableFuture.completedFuture(createEvalNode(v));
    }

}

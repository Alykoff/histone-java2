package ru.histone.v2.evaluator;

import org.apache.commons.lang.ObjectUtils;
import ru.histone.v2.evaluator.node.*;

import java.util.Map;

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
        } else if (node instanceof IntEvalNode) {
            return ((int) node.getValue()) != 0;
        } else if (node instanceof StringEvalNode) {
            return !node.getValue().equals("");
        }
        return true;
    }

//    public static boolean greaterThan(EvalNode left, EvalNode right) {
//        if (isNumberNode(left) && isNumberNode(right)) {
//            Float a = 1f;
//            Integer b = 9;
//
//            return right.getValue()
//        }
//    }

    public static boolean isNumberNode(EvalNode node) {
        return node instanceof IntEvalNode || node instanceof FloatEvalNode;
    }

    public static EvalNode<?> createEvalNode(Object object) {
        if (object == null) {
            throw new NullPointerException();
        }
        if (object.equals(ObjectUtils.NULL)) {
            return new NullEvalNode();
        }
        if (object instanceof Boolean) {
            return new BooleanEvalNode((Boolean) object);
        }
        if (object instanceof Float) {
            return new FloatEvalNode((Float) object);
        }
        if (object instanceof Integer) {
            return new IntEvalNode((Integer) object);
        }
        if (object instanceof String) {
            return new StringEvalNode((String) object);
        }
        if (object instanceof Map) {
            return new MapEvalNode((Map<String, Object>) object);
        }
        return new ObjectEvalNode(object);
    }
}

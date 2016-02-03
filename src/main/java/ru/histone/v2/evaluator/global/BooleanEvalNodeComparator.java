package ru.histone.v2.evaluator.global;

import ru.histone.v2.evaluator.node.BooleanEvalNode;

import java.io.Serializable;
import java.util.Comparator;

/**
 *
 * Created by gali.alykoff on 03/02/16.
 */
public class BooleanEvalNodeComparator implements Comparator<BooleanEvalNode>, Serializable {
    @Override
    public int compare(BooleanEvalNode left, BooleanEvalNode right) {
        return left.getValue().compareTo(right.getValue());
    }
}

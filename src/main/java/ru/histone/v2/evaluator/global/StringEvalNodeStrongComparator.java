package ru.histone.v2.evaluator.global;

import ru.histone.v2.evaluator.node.StringEvalNode;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author gali.alykoff on 11/02/16.
 */
public class StringEvalNodeStrongComparator implements Comparator<StringEvalNode>, Serializable {
    @Override
    public int compare(StringEvalNode left, StringEvalNode right) {
        return left.getValue().compareTo(right.getValue());
    }
}

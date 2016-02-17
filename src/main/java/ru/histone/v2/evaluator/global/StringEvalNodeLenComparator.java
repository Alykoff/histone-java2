package ru.histone.v2.evaluator.global;

import ru.histone.v2.evaluator.node.StringEvalNode;

import java.io.Serializable;
import java.util.Comparator;

/**
 *
 * Created by gali.alykoff on 03/02/16.
 */
public class StringEvalNodeLenComparator implements Comparator<StringEvalNode>, Serializable {
    @Override
    public int compare(StringEvalNode left, StringEvalNode right) {
        final long leftLength = left.getValue().length();
        final long rightLength = right.getValue().length();
        return Long.valueOf(leftLength).compareTo(rightLength);
    }
}

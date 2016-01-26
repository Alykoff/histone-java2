package ru.histone.v2.evaluator.data;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 *
 * Created by gali.alykoff on 25/01/16.
 */
public class HistoneRegex implements Serializable {
    private final boolean isGlobal;
    private final Pattern pattern;

    public HistoneRegex(boolean isGlobal, Pattern pattern) {
        this.isGlobal = isGlobal;
        this.pattern = pattern;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoneRegex that = (HistoneRegex) o;
        return isGlobal == that.isGlobal &&
                Objects.equals(pattern, that.pattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isGlobal, pattern);
    }

    @Override
    public String toString() {
        return "HistoneRegex{" +
                "isGlobal=" + isGlobal +
                ", pattern=" + pattern +
                '}';
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public Pattern getPattern() {
        return pattern;
    }
}

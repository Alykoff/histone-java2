package ru.histone.tokenizer;

import java.util.Collections;
import java.util.List;

/**
 * Created by alexey.nevinsky on 21.12.2015.
 */
public class Token {

    private final String value;
    private final List<Integer> types;
    private final int index;
    private final boolean isIgnored;

    public Token(String value, List<Integer> types, int index) {
        this.value = value;
        this.types = types;
        this.index = index;
        isIgnored = false;
    }

    public Token(String value, Integer type, int index) {
        this.value = value;
        this.types = Collections.singletonList(type);
        this.index = index;
        isIgnored = false;
    }

    public Token(Integer type, int index) {
        this(Collections.singletonList(type), index);
    }

    public Token(List<Integer> types, int index) {
        this.types = types;
        this.index = index;
        value = null;
        isIgnored = false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Token{");
        sb.append("value='").append(value).append('\'');
        sb.append(", types=").append(types);
        sb.append(", index=").append(index);
        sb.append(", isIgnored=").append(isIgnored);
        sb.append('}');
        return sb.toString();
    }

    public String getValue() {
        return value;
    }

    public List<Integer> getTypes() {
        return types;
    }

    public long getIndex() {
        return index;
    }

    public boolean isIgnored() {
        return isIgnored;
    }
}

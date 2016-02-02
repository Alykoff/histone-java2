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
    private boolean isIgnored;

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

    public void setIsIgnored(boolean isIgnored) {
        this.isIgnored = isIgnored;
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

    public int getIndex() {
        return index;
    }

    public boolean isIgnored() {
        return isIgnored;
    }
}

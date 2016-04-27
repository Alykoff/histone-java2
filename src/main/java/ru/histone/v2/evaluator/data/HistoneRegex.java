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

package ru.histone.v2.evaluator.data;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * @author Gali Alykoff
 */
public class HistoneRegex implements Serializable {
    private final boolean isGlobal;
    private final boolean isIgnoreCase;
    private final boolean isMultiline;
    private final Pattern pattern;

    public HistoneRegex(boolean isGlobal, boolean isIgnoreCase, boolean isMultiline, Pattern pattern) {
        this.isGlobal = isGlobal;
        this.isIgnoreCase = isIgnoreCase;
        this.isMultiline = isMultiline;
        this.pattern = pattern;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HistoneRegex regex = (HistoneRegex) o;

        if (isGlobal != regex.isGlobal) return false;
        if (isIgnoreCase != regex.isIgnoreCase) return false;
        if (isMultiline != regex.isMultiline) return false;
        return pattern != null ? pattern.equals(regex.pattern) : regex.pattern == null;

    }

    @Override
    public int hashCode() {
        int result = (isGlobal ? 1 : 0);
        result = 31 * result + (isIgnoreCase ? 1 : 0);
        result = 31 * result + (isMultiline ? 1 : 0);
        result = 31 * result + (pattern != null ? pattern.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        String res = "/" + pattern.pattern() + "/";
        if (isGlobal()) {
            res += "g";
        }
        if (isIgnoreCase()) {
            res += "i";
        }
        if (isMultiline()) {
            res += "m";
        }
        return res;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public boolean isIgnoreCase() {
        return isIgnoreCase;
    }

    public boolean isMultiline() {
        return isMultiline;
    }

    public Pattern getPattern() {
        return pattern;
    }
}

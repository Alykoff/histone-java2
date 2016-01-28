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

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

package ru.histone.v2.rtti;

/**
 * Created by gali.alykoff on 22/01/16.
 */
public enum HistoneType {
    T_BASE(0),
    T_UNDEFINED(1),
    T_NULL(2),
    T_BOOLEAN(3),
    T_NUMBER(4),
    T_STRING(5),
    T_REGEXP(6),
    T_MACRO(7),
    T_ARRAY(8),
    T_GLOBAL(9);

    private int id;

    HistoneType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}

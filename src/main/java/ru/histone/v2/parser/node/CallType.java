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

package ru.histone.v2.parser.node;

/**
 * @author Alexey Nevinsky
 */
public enum CallType {
    //we have a variable: {{var a = [b:'c']}}
    SIMPLE(-1),    //example: {{toJSON()}} or {{toJSON}} or {{global->toJSON}} or {{varFromOuterContext->toJSON()}}
    RTTI_M_GET(0), //example: {{a.b}} or {{a['b']}}
    RTTI_M_CALL(1);//example: {{a()}} or {{a(1,2,3,4,5)}}


    private final int id;

    CallType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static CallType fromId(int id) {
        for (CallType type : values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("wrong CallType id '" + id + "'");
    }
}

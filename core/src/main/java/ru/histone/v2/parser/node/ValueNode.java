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

import java.io.Serializable;

/**
 * @author Gali Alykoff
 */
public abstract class ValueNode<T> extends AstNode implements Serializable {
    protected T value;

    public ValueNode() {
        super(AstType.AST_VALUE_NODE);
    }

    public ValueNode(T value) {
        this();
        this.value = value;
    }

    public boolean hasValue() {
        return true;
    }

    public T getValue() {
        return value;
    }

    public int size() {
        return 0;
    }

    @Override
    public String toString() {
        return "{\"ValueNode\": {\"value\": \"" + value + "\"}}";
    }
}

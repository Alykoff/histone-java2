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

package ru.histone.v2.evaluator.node;

import ru.histone.v2.rtti.HistoneType;

/**
 * Created by inv3r on 19/01/16.
 */
public abstract class EvalNode<T> {
    protected T value;
    protected boolean isReturn = false;

    public EvalNode(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setIsReturn() {
        isReturn = true;
    }

    public boolean isReturn() {
        return isReturn;
    }

    public abstract HistoneType getType();

    @Override
    public String toString() {
        return "{\"EvalNode\": {\"value\": \"" + value + "\"}}";
    }
}

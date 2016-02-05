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

import org.apache.commons.lang.ObjectUtils;
import ru.histone.v2.rtti.HistoneType;

/**
 * @author alexey.nevinsky
 */
public class NullEvalNode extends EvalNode<ObjectUtils.Null> {
    public static final String HISTONE_VIEW = "null";
    public static final NullEvalNode INSTANCE = new NullEvalNode();

    private NullEvalNode() {
        super(ObjectUtils.NULL);
    }

    @Override
    public ObjectUtils.Null getValue() {
        return ObjectUtils.NULL;
    }

    @Override
    public HistoneType getType() {
        return HistoneType.T_NULL;
    }
}

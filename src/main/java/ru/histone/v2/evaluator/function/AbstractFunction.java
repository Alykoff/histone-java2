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

package ru.histone.v2.evaluator.function;

import ru.histone.v2.evaluator.Function;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.rtti.HistoneType;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * Created by inv3r on 27/01/16.
 */
public abstract class AbstractFunction implements Function {
    protected final Executor executor;

    protected AbstractFunction() {
        executor = null;
    }

    protected AbstractFunction(Executor executor) {
        this.executor = executor;
    }

    /**
     * Method validates argument {@link EvalNode} on {@link int} position with specified {@link HistoneType} and {@link Class}.
     * If validation will failed, method returns error message. Otherwise method returns null.
     *
     * @param node         eval node, which wrapping argument value
     * @param index        index in arguments list
     * @param expectedType expected type of node
     * @param clazz        expected type of node wrapped value
     * @return null, if validation was successful, otherwise returns error message
     */
    protected static String checkType(EvalNode node, int index, HistoneType expectedType, Class<?> clazz) {
        if (node.getType() != expectedType) {
            return String.format("Argument with index '%s' must be of type '%s', but specified is '%s'",
                    index, expectedType, node.getType());
        }

        Object value = node.getValue();
        if (!(clazz.isAssignableFrom(value.getClass()))) {
            return String.format("Argument with index '%s' must be of type '%s', but specified is '%s'",
                    index, expectedType, node.getType());
        }
        return null;
    }

    protected static String checkMinArgsLength(List<EvalNode> args, int expectedCount) {
        if (args.size() < expectedCount) {
            return String.format("Expected length of arguments is '%s', but current is '%s'", expectedCount, args.size());
        }
        return null;
    }

    protected static String checkMaxArgsLength(List<EvalNode> args, int expectedCount) {
        if (args.size() > expectedCount) {
            return String.format("Expected length of arguments is '%s', but current is '%s'. Extra arguments will be not used",
                    expectedCount, args.size());
        }
        return null;
    }

    protected <T> T getValue(List<EvalNode> args, int index) {
        return index > args.size() - 1 ? null : (T) args.get(index).getValue();
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isClear() {
        return false;
    }
}

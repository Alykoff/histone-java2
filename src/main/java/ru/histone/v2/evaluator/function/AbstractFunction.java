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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.histone.v2.evaluator.Function;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.resource.HistoneResourceLoader;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * @author alexey.nevinsky
 */
public abstract class AbstractFunction implements Function {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final Executor executor;
    protected final HistoneResourceLoader resourceLoader;

    protected AbstractFunction() {
        executor = null;
        resourceLoader = null;
    }

    protected AbstractFunction(Executor executor, HistoneResourceLoader resourceLoader) {
        this.executor = executor;
        this.resourceLoader = resourceLoader;
    }

    /**
     * Method validates argument {@link EvalNode} on {@link int} position with specified {@link HistoneType} and {@link Class}.
     * If validation will failed, method returns error message. Otherwise method returns null.
     *
     * @param node          eval node, which wrapping argument value
     * @param index         index in arguments list
     * @param expectedTypes expected types of node
     * @param classes       expected types of node wrapped value
     */
    protected static void checkTypes(EvalNode node, int index, List<HistoneType> expectedTypes, List<Class<?>> classes) {
        if (!expectedTypes.contains(node.getType())) {
            throw new FunctionExecutionException("Argument with index '%s' must be of type '%s', but specified is '%s'",
                    index, expectedTypes, node.getType());
        }

        Object value = node.getValue();
        if (!classes.contains(value.getClass())) {
            throw new FunctionExecutionException("Argument with index '%s' must be of type '%s', but specified is '%s'",
                    index, classes, value.getClass());
        }
    }

    protected static void checkTypes(EvalNode node, int index, HistoneType type, Class<?> clazz) {
        checkTypes(node, index, Collections.singletonList(type), Collections.singletonList(clazz));
    }

    protected static void checkMinArgsLength(List<EvalNode> args, int expectedCount) {
        if (args.size() < expectedCount) {
            throw new FunctionExecutionException("Expected length of arguments is '%s', but current is '%s'",
                    expectedCount, args.size());
        }
    }

    protected void checkMaxArgsLength(List<EvalNode> args, int expectedCount) {
        if (args.size() > expectedCount) {
            logger.warn("Expected length of arguments is '%s', but current is '%s'. Extra arguments will be not used",
                    expectedCount, args.size());
        }
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

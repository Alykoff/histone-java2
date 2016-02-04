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
package ru.histone.v2.evaluator.function.global;

import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author inv3r
 */
public class ResolveURI extends AbstractFunction {
    @Override
    public String getName() {
        return "resolveURI";
    }

    @Override
    public CompletableFuture<EvalNode> execute(String baseUri, List<EvalNode> args) throws FunctionExecutionException {
        String relativeUri = getValue(args, 0) + "";
        if (getValue(args, 1) != null) {
            baseUri = getValue(args, 1);
        }
        if (!baseUri.endsWith("/")) {
            baseUri += "/";
        }
        if (relativeUri.startsWith("/")) {
            relativeUri = relativeUri.substring(1);
        }
        return EvalUtils.getValue(baseUri + relativeUri);
    }
}

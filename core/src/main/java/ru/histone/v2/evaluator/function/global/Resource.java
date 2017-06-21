/*
 * Copyright (c) 2017 MegaFon
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

import org.apache.commons.collections.CollectionUtils;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.Converter;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.utils.AsyncUtils;
import ru.histone.v2.utils.PathUtils;
import ru.histone.v2.utils.RttiUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * It accepts 0..Infinity arguments, which are references to resources. When called from ANY template, the backend
 * should do the following:
 * 1. There is an array (or map) of RESOURCES.
 * 2. When you call the "resource" method, the resultStr variable are intializing, go through the list of passed
 * arguments, for each found string, resize the argument to the baseURI of the template from where the method was
 * called and put it into the ResourceURI variable
 * 3. Add in resultStr + = "<noscript id='res_${resourceURI}'></ noscript>"
 * 4. Add resourceURI to the RESOURCES array, simultaneously excluding duplicates (that is, there can not be duplicates)
 * 5. Output resultStr - the result of the execution of the method.
 *
 * @author Alexey Nevinsky
 * @since 16-06-2017
 */
public class Resource extends AbstractFunction {

    protected String urlFormat = "<noscript id=\"res_%s\"></noscript>";

    public Resource(Converter converter) {
        super(converter);
    }

    @Override
    public String getName() {
        return "resource";
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        List<EvalNode> arguments = clearGlobal(args);
        if (CollectionUtils.isEmpty(args)) {
            return converter.getValue(null);
        }

        List<CompletableFuture<String>> urlFutures = new ArrayList<>(arguments.size());
        for (EvalNode node : arguments) {
            urlFutures.add(RttiUtils.callToStringResult(context, node));
        }

        return AsyncUtils.sequence(urlFutures).thenCompose(urlStrings -> {
            if (CollectionUtils.isEmpty(urlStrings)) {
                return converter.getValue(null);
            }

            context.getTemplateVars().compute("resources", (k, old) -> {
                List<CompletableFuture<EvalNode>> res = (List<CompletableFuture<EvalNode>>) old;
                if (old == null) {
                    res = new ArrayList<>();
                }

                for (String url : urlStrings) {
                    res.add(converter.getValue(url));
                }
                return res;
            });

            return converter.getValue(urlStrings.stream()
                                                .map(url -> String.format(urlFormat, url))
                                                .collect(Collectors.joining())
            );
        });
    }
}

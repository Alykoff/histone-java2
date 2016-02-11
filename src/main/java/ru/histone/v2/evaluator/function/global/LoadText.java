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

import ru.histone.utils.IOUtils;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.MapEvalNode;
import ru.histone.v2.evaluator.resource.HistoneResourceLoader;
import ru.histone.v2.evaluator.resource.HistoneStreamResource;
import ru.histone.v2.evaluator.resource.HistoneStringResource;
import ru.histone.v2.evaluator.resource.Resource;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.exceptions.ResourceLoadException;
import ru.histone.v2.rtti.HistoneType;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * @author alexey.nevinsky
 */
public class LoadText extends AbstractFunction {
    public LoadText(Executor executor, HistoneResourceLoader loader) {
        super(executor, loader);
    }

    @Override
    public String getName() {
        return "loadText";
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        checkMinArgsLength(args, 1);
        checkMaxArgsLength(args, 2);
        checkTypes(args.get(0), 0, HistoneType.T_STRING, String.class);

        String path = getValue(args, 0);
        MapEvalNode requestMap = null;
        if (args.size() > 1) {
            checkTypes(args.get(1), 1, HistoneType.T_ARRAY, Map.class);
            requestMap = (MapEvalNode) args.get(1);
        }

        CompletableFuture<Resource> resourceFuture = resourceLoader.load(path, context.getBaseUri(), requestMap);
        return resourceFuture
                .exceptionally(ex -> {
                    logger.error("Error", ex);
                    return null;
                })
                .thenApply(resource -> {
                    if (resource == null) {
                        return EvalUtils.createEvalNode(null);
                    }

                    String content = readStringFromResource(resource, path, context.getBaseUri());
                    return EvalUtils.createEvalNode(content);
                });
    }

    private String readStringFromResource(Resource resource, String path, String currentBaseURI) throws ResourceLoadException {
        try {
            if (resource == null) {
                throw new ResourceLoadException(String.format("Can't load resource by path: %s.", path));
            }

            String content;
            if (resource instanceof HistoneStringResource) {
                content = ((HistoneStringResource) resource).getContent();
            } else if (resource instanceof HistoneStreamResource) {
                content = IOUtils.toString(((HistoneStreamResource) resource).getContent());
            } else {
                throw new ResourceLoadException(MessageFormat.format("Unsupported resource class: {0}", resource.getClass()));
            }

            if (content == null) {
                throw new ResourceLoadException(MessageFormat.format("Can't load resource by path: {0}. Resource is unreadable.", path));
            }
            return content;
        } catch (IOException e) {
            throw new ResourceLoadException("Resource import failed! Resource reading error.", e);
        }
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}

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

package ru.histone.v2.java_compiler.java_evaluator.function;

import org.apache.commons.lang3.StringUtils;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.Evaluator;
import ru.histone.v2.evaluator.function.global.Require;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.resource.HistoneResourceLoader;
import ru.histone.v2.evaluator.resource.loader.FileLoader;
import ru.histone.v2.exceptions.ResourceLoadException;
import ru.histone.v2.exceptions.StopExecutionException;
import ru.histone.v2.java_compiler.bcompiler.StdLibrary;
import ru.histone.v2.java_compiler.bcompiler.data.Template;
import ru.histone.v2.parser.Parser;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.PathUtils;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * @author Alexey Nevinsky
 */
public class JavaRequire extends Require {

    public JavaRequire(Executor executor, HistoneResourceLoader resourceLoader, Evaluator evaluator, Parser parser) {
        super(executor, resourceLoader, evaluator, parser);
    }

    @Override
    protected CompletableFuture<EvalNode> doExecute(Context context, List<EvalNode> args) {
        checkMinArgsLength(args, 1);
        checkMaxArgsLength(args, 2);
        checkTypes(args.get(0), 0, Collections.singletonList(HistoneType.T_STRING), Collections.singletonList(String.class));

        String url = getValue(args, 0);
        final Object params = getValue(args, 1, null);

        URI uri = URI.create(url);
        if (StringUtils.isBlank(uri.getScheme())) {
            String fullUri = PathUtils.resolveUrl(url, context.getBaseUri()).replace(FileLoader.FILE_SCHEME + ":", "fullPathClass:");
            uri = URI.create(fullUri);
        }

        return resourceLoader.load(uri.toString(), context.getBaseUri(), Collections.emptyMap())
                .thenCompose(res -> {
                    try {
                        Template tpl = (Template) res.getContent();

                        Context ctx = createCtx(context, res.getBaseHref(), params);
                        return tpl.render(ctx);
                    } catch (Exception e) {
                        throw new ResourceLoadException("Resource import failed! Resource reading error.", e);
                    }
                })
                .exceptionally(e -> {
                    if (e.getCause() instanceof StopExecutionException) {
                        throw (StopExecutionException) e.getCause();
                    }
                    logger.error(e.getMessage(), e);
                    return EvalUtils.createEvalNode(null);
                });
    }
}

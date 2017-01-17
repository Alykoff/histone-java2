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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.Converter;
import ru.histone.v2.evaluator.Evaluator;
import ru.histone.v2.evaluator.function.global.LoadText;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.resource.HistoneResourceLoader;
import ru.histone.v2.evaluator.resource.Resource;
import ru.histone.v2.java_compiler.bcompiler.data.Template;
import ru.histone.v2.java_compiler.java_evaluator.HistoneTemplateResource;
import ru.histone.v2.parser.Parser;
import ru.histone.v2.utils.IOUtils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

/**
 * @author Alexey Nevinsky
 */
public class JavaLoadText extends LoadText {

    private static final Logger LOG = LoggerFactory.getLogger(JavaLoadText.class);

    public JavaLoadText(Executor executor, HistoneResourceLoader loader, Evaluator evaluator, Parser parser,
                        Converter converter, ConcurrentMap<String, CompletableFuture<EvalNode>> cache) {
        super(executor, loader, evaluator, parser, converter, cache);
    }

    @Override
    protected CompletableFuture<EvalNode> loadResource(Context context, String path, Map<String, Object> params) {
        return resourceLoader.load(path, context.getBaseUri(), params)
                .exceptionally(ex -> {
                    logger.error("Error", ex);
                    return null;
                })
                .thenApply(resource -> {
                    if (resource == null) {
                        return converter.createEvalNode(null);
                    }

                    if (resource.getContentType().equals(HistoneTemplateResource.CONTENT_TYPE)) {
                        return loadAST(resource);
                    }

                    String content = IOUtils.readStringFromResource(resource, path);
                    return converter.createEvalNode(content);
                });
    }

    protected EvalNode loadAST(Resource resource) {
        try {
            Template t = (Template) resource.getContent();
            return converter.createEvalNode(t.getStringAst());
        } catch (IOException e) {
            LOG.error("Failed to getting ast-tree", e);
            return converter.createEvalNode(null);
        }
    }
}

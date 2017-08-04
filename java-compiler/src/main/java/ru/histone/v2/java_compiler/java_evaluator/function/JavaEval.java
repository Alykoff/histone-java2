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

import org.apache.commons.codec.digest.DigestUtils;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.Converter;
import ru.histone.v2.evaluator.Evaluator;
import ru.histone.v2.evaluator.function.global.Eval;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.resource.HistoneResourceLoader;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.exceptions.ResourceLoadException;
import ru.histone.v2.exceptions.StopExecutionException;
import ru.histone.v2.java_compiler.bcompiler.data.Template;
import ru.histone.v2.java_compiler.java_evaluator.loader.JavaHistoneRawTemplateLoader;
import ru.histone.v2.parser.Parser;
import ru.histone.v2.rtti.HistoneType;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * @author Alexey Nevinsky
 */
public class JavaEval extends Eval {
    public JavaEval(Executor executor, HistoneResourceLoader resourceLoader, Evaluator evaluator, Parser parser, Converter converter) {
        super(executor, resourceLoader, evaluator, parser, converter);
    }

    protected CompletableFuture<EvalNode> doExecute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        EvalNode templateNode = args.get(0);
        if (templateNode.getType() != HistoneType.T_STRING) {
            return converter.getValue(null);
        }

        String template = (String) templateNode.getValue();

        final EvalNode params;
        if (args.size() >= 2) {
            params = args.get(1);
        } else {
            params = converter.createEvalNode(null);
        }

        final String baseUri;
        if (args.size() > 2 && args.get(2).getType() == HistoneType.T_STRING) {
            baseUri = (String) args.get(2).getValue();
        } else {
            baseUri = context.getBaseUri();
        }

        String className = "ru.histone.generated.Tpl$" + DigestUtils.sha512Hex(template);

        URI uri = URI.create("rawTpl:" + className);

        Map<String, Object> loadParams = Collections.singletonMap(JavaHistoneRawTemplateLoader.PARAM_KEY, template);

        return resourceLoader.load(context, uri.toString(), baseUri, loadParams)
                             .thenCompose(res -> {
                                 try {
                                     Template tpl = (Template) res.getContent();

                                     Context ctx = createCtx(context, baseUri, params);
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
                                 return converter.createEvalNode(null);
                             });
    }
}

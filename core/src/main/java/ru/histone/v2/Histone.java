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

package ru.histone.v2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.Evaluator;
import ru.histone.v2.evaluator.resource.Resource;
import ru.histone.v2.evaluator.resource.SchemaResourceLoader;
import ru.histone.v2.evaluator.resource.loader.Loader;
import ru.histone.v2.exceptions.HistoneException;
import ru.histone.v2.exceptions.SyntaxErrorException;
import ru.histone.v2.exceptions.UnexpectedTokenException;
import ru.histone.v2.parser.Parser;
import ru.histone.v2.parser.SsaOptimizer;
import ru.histone.v2.parser.node.ExpAstNode;
import ru.histone.v2.property.DefaultPropertyHolder;
import ru.histone.v2.property.PropertyHolder;
import ru.histone.v2.rtti.RunTimeTypeInfo;
import ru.histone.v2.utils.AstJsonProcessor;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * @author Alexey Nevinsky
 */
public class Histone implements HistoneEngine {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected Evaluator evaluator;
    protected RunTimeTypeInfo runTimeTypeInfo;
    protected Parser parser;
    protected SchemaResourceLoader resourceLoader;
    protected Executor executor;
    protected Locale locale;
    protected PropertyHolder propertyHolder;

    public Histone() {
        this(new ForkJoinPool());
    }

    public Histone(Locale locale) {
        this();
        this.locale = locale;
    }

    public Histone(Locale locale, Executor executor) {
        this(executor);
        this.locale = locale;
    }

    public Histone(Executor executor) {
        initializeHistone(executor);
    }

    protected void initializeHistone(Executor executor) {
        logger.info("================================================================");
        logger.info("Initializing Histone2 engine, implementation: " + getClass() + ". With executor: " + executor.getClass());
        this.evaluator = new Evaluator();
        this.parser = new Parser();
        this.executor = executor;
        this.propertyHolder = new DefaultPropertyHolder();
        this.resourceLoader = new SchemaResourceLoader();
        this.locale = Locale.getDefault();
        this.runTimeTypeInfo = new RunTimeTypeInfo(executor, resourceLoader, evaluator, parser);
        logger.info("Initialization finished");
        logger.info("================================================================");
    }

    public String process(String template, String baseUri, Map<String, Object> params) {
        ExpAstNode tree = parser.process(template, baseUri);
        return evaluator.process(tree, createContext(baseUri, params));
    }

    public String process(String template) {
        return process(template, "", Collections.emptyMap());
    }

    public ExpAstNode parseTemplateToAST(String templateData, String baseURI) {
        try {
            if (EvalUtils.isAst(templateData)) {
                ExpAstNode root = AstJsonProcessor.read(templateData);
                SsaOptimizer optimizer = new SsaOptimizer();
                optimizer.process(root);
                return root;
            }
            //it's ok to skip cache here, since href are small enough
            return parser.process(templateData, baseURI);
        } catch (UnexpectedTokenException | SyntaxErrorException e) {
            throw new RuntimeException("Error evaluating histone template '" + baseURI + "' in line " + e.getLine() + ": " + e.getMessage());
        } catch (HistoneException e) {
            throw new RuntimeException("Error evaluating histone template '" + baseURI + "': " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("Error parsing histone AST '" + baseURI + "': " + e.getMessage(), e);
        }
    }

    public CompletableFuture<String> evaluateAST(String baseUri, ExpAstNode ast, Map<String, Object> params) throws HistoneException {
        Context ctx = createContext(baseUri, params);

        return evaluator.processFuture(ast, ctx);
    }

    @Override
    public String processTemplate(String templateLocation, String baseUri, Map<String, Object> params, String encoding) throws IOException {
        try (Resource resource = resourceLoader.load(templateLocation, baseUri, params).join()) {
            ExpAstNode expAstNode = parseTemplateToAST((String) resource.getContent(), baseUri);
            return evaluateAST(templateLocation, expAstNode, params).join();
        }
    }

    @Override
    public HistoneEngine addResourceLoader(String name, Loader loader) {
        this.resourceLoader.addLoader(name, loader);
        return this;
    }


    private Context createContext(String baseUri, Map<String, Object> params) {
        Context ctx = Context.createRoot(baseUri, locale, runTimeTypeInfo, propertyHolder);
        ctx.put("this", CompletableFuture.completedFuture(EvalUtils.constructFromObject(params)));
        return ctx;
    }
}

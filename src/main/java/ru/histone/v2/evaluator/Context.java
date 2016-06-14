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

package ru.histone.v2.evaluator;

import ru.histone.v2.Constants;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.exceptions.HistoneException;
import ru.histone.v2.property.PropertyHolder;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.rtti.RunTimeTypeInfo;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

/**
 * Evaluation context of histone
 * <p>
 *
 * @author Alexey Nevinsky
 */
public class Context implements Cloneable {
    private String baseUri;
    private Locale locale;
    private RunTimeTypeInfo rttiInfo;
    private ConcurrentMap<String, CompletableFuture<EvalNode>> vars = new ConcurrentHashMap<>();
    private PropertyHolder<String> propertyHolder;
    private Context parent;

    private Context(String baseUri, Locale locale, RunTimeTypeInfo rttiInfo,
                    PropertyHolder<String> propertyHolder) {
        this.baseUri = baseUri;
        this.rttiInfo = rttiInfo;
        this.locale = locale;
        this.propertyHolder = propertyHolder;
    }

    /**
     * This method used for create a root node
     *
     * @param baseUri  of context
     * @param locale   environment locale
     * @param rttiInfo is global run time type info
     * @param propertyHolder holder with global properties
     * @return created root context
     */
    public static Context createRoot(String baseUri, Locale locale, RunTimeTypeInfo rttiInfo,
                                     PropertyHolder<String> propertyHolder) {
        return new Context(baseUri, locale, rttiInfo, propertyHolder);
    }

    /**
     * This method used for create a root node using default locale
     *
     * @param baseUri        of context
     * @param rttiInfo       is global run time type info
     * @param propertyHolder holder with global properties
     * @return created root context
     */
    public static Context createRoot(String baseUri, RunTimeTypeInfo rttiInfo,
                                     PropertyHolder<String> propertyHolder) {
        return new Context(baseUri, Locale.getDefault(), rttiInfo, propertyHolder);
    }

    @Override
    public Context clone() {
        try {
            Context that = (Context) super.clone();
            that.vars = new ConcurrentHashMap<>(this.vars);
            return that;
        } catch (CloneNotSupportedException e) {
            throw new HistoneException(e);
        }
    }

    public Context cloneEmpty() {
        return new Context(baseUri, locale, rttiInfo, propertyHolder);
    }

    /**
     * This method used for create a child context
     *
     * @return child context
     */
    public Context createNew() {
        Context ctx = new Context(baseUri, locale, rttiInfo, propertyHolder);
        ctx.parent = this;
        ctx.put(Constants.THIS_CONTEXT_VALUE, getValue(Constants.THIS_CONTEXT_VALUE));
        return ctx;
    }

    public void put(String key, CompletableFuture<EvalNode> value) {
        vars.put(key, value);
    }

    public CompletableFuture<EvalNode> getValue(String key) {
        return vars.getOrDefault(key, EvalUtils.getValue(null));
    }

    public Context getParent() {
        return parent;
    }

    public Map<String, String> getGlobalProperties() {
        return propertyHolder.getPropertyMap();
    }

    public ConcurrentMap<String, CompletableFuture<EvalNode>> getVars() {
        return vars;
    }

    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public CompletableFuture<EvalNode> call(String name, List<EvalNode> args) {
        return rttiInfo.callFunction(this, HistoneType.T_GLOBAL, name, args);
    }

    public CompletableFuture<EvalNode> call(EvalNode node, String name, List<EvalNode> args) {
        return rttiInfo.callFunction(this, node, name, args);
    }

    public CompletableFuture<EvalNode> macroCall(List<EvalNode> args) {
        return rttiInfo.callFunction(this, HistoneType.T_MACRO, "call", args);
    }

    public boolean findFunction(EvalNode node, String name) {
        try {
            return rttiInfo.getFunc(node.getType(), name).isPresent();
        } catch (FunctionExecutionException ignore) {
            // yeah, we couldn't find function with this name
        }
        return false;
    }

    public boolean findFunction(String name) {
        try {
            return rttiInfo.getFunc(HistoneType.T_GLOBAL, name).isPresent();
        } catch (FunctionExecutionException ignore) {
            // yeah, we couldn't find function with this name
        }
        return false;
    }

    public Locale getLocale() {
        return locale;
    }

    public Executor getExecutor() {
        return rttiInfo.getExecutor();
    }
}

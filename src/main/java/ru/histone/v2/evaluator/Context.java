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
import ru.histone.v2.evaluator.node.EmptyEvalNode;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.rtti.RunTimeTypeInfo;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Evaluation context of histone
 * <p>
 * Created by inv3r on 13/01/16.
 */
public class Context implements Serializable {
    private String baseUri;
    private Locale locale;
    private RunTimeTypeInfo rttiInfo;
    private ConcurrentMap<String, CompletableFuture<EvalNode>> vars = new ConcurrentHashMap<>();
    private ConcurrentMap<String, CompletableFuture<EvalNode>> thisVars = null;

    private Context parent;

    private Context(String baseUri, Locale locale, RunTimeTypeInfo rttiInfo) {
        this.baseUri = baseUri;
        this.rttiInfo = rttiInfo;
        this.locale = locale;
    }

    /**
     * This method used for create a root node
     *
     * @param baseUri  of context
     * @param locale   environment locale
     * @param rttiInfo is global run time type info
     * @return created root context
     */
    public static Context createRoot(String baseUri, Locale locale, RunTimeTypeInfo rttiInfo) {
        Context ctx = new Context(baseUri, locale, rttiInfo);
        ctx.thisVars = new ConcurrentHashMap<>();
        return ctx;
    }

    /**
     * This method used for create a root node
     *
     * @param baseUri  of context
     * @param rttiInfo is global run time type info
     * @return created root context
     */
    public static Context createRoot(String baseUri, RunTimeTypeInfo rttiInfo) {
        Context ctx = new Context(baseUri, Locale.getDefault(), rttiInfo);
        ctx.thisVars = new ConcurrentHashMap<>();
        return ctx;
    }

    public Context clone() {
        final Context that = new Context(baseUri, locale, rttiInfo);
        that.parent = this.parent;
        that.thisVars = this.thisVars;
        that.vars.putAll(this.vars);
        return that;
    }

    /**
     * This method used for create a child context
     *
     * @return child context
     */
    public Context createNew() {
        Context ctx = new Context(baseUri, locale, rttiInfo);
        ctx.parent = this;
        ctx.thisVars = thisVars;
        return ctx;
    }

    public void release() {
        parent = null;
    }

    public void put(String key, CompletableFuture<EvalNode> value) {
        vars.put(key, value);
    }

    public boolean contains(String key) {
        return vars.containsKey(key);
    }

    public CompletableFuture<EvalNode> getValue(String key) {
        if (key.equals(Constants.THIS_CONTEXT_VALUE)) {
            if (thisVars.containsKey(key)) {
                return thisVars.get(key);
            }
            return EmptyEvalNode.FUTURE_INSTANCE;
        }
        return vars.get(key);
    }

    public Context getParent() {
        return parent;
    }

    public ConcurrentMap<String, CompletableFuture<EvalNode>> getVars() {
        return vars;
    }

    public ConcurrentMap<String, CompletableFuture<EvalNode>> getThisVars() {
        return thisVars;
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

    public boolean findFunction(EvalNode node, String name) {
        try {
            return rttiInfo.getFunc(node.getType(), name) != null;
        } catch (FunctionExecutionException ignore) {
            // yeah, we couldn't find function with this name
        }
        return false;
    }

    public Locale getLocale() {
        return locale;
    }
}

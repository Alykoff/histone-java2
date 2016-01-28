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
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.rtti.RunTimeTypeInfo;

import java.io.Serializable;
import java.util.List;
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
    private RunTimeTypeInfo rttiInfo;
    private ConcurrentMap<String, CompletableFuture<EvalNode>> vars = new ConcurrentHashMap<>();
    private ConcurrentMap<String, CompletableFuture<EvalNode>> thisVars = new ConcurrentHashMap<>();

    private Context parent;

    private Context(String baseUri, RunTimeTypeInfo rttiInfo) {
        this.baseUri = baseUri;
        this.rttiInfo = rttiInfo;
    }

    /**
     * This method used for create a root node
     *
     * @param baseUri  of context
     * @param rttiInfo is global run time type info
     * @return created root context
     */
    public static Context createRoot(String baseUri, RunTimeTypeInfo rttiInfo) {
        return new Context(baseUri, rttiInfo);
    }

    /**
     * This method used for create a child context
     *
     * @return child context
     */
    public Context createNew() {
        Context ctx = new Context(baseUri, rttiInfo);
        ctx.parent = this;
        ctx.thisVars = thisVars;
        return ctx;
    }

    public void release() {
        parent = null;
    }

    public void put(String key, CompletableFuture<EvalNode> value) {
        vars.putIfAbsent(key, value);
    }

    public boolean contains(String key) {
        return vars.containsKey(key);
    }

    public CompletableFuture<EvalNode> getValue(String key) {
        if (key.equals(Constants.THIS_CONTEXT_VALUE)) {
            if (thisVars.containsKey(key)) {
                return thisVars.get(key);
            }
            return CompletableFuture.completedFuture(EmptyEvalNode.INSTANCE);
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
        return rttiInfo.callFunction(baseUri, HistoneType.T_GLOBAL, name, args);
    }

    public CompletableFuture<EvalNode> call(EvalNode node, String name, List<EvalNode> args) {
        return rttiInfo.callFunction(baseUri, node, name, args);
    }
}

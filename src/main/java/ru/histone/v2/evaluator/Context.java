package ru.histone.v2.evaluator;

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

    private Context parent;

    private Context(String baseUri, RunTimeTypeInfo rttiInfo) {
        this.baseUri = baseUri;
        this.rttiInfo = rttiInfo;
    }

    /**
     * This method used for create a root node
     *
     * @param baseUri  of context
     * @param rttiInfo is global run tipe type info
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
        return vars.get(key);
    }

    public Context getParent() {
        return parent;
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
        return rttiInfo.callFunction(baseUri, HistoneType.T_GLOBAL, name, args);
    }

    public CompletableFuture<EvalNode> call(EvalNode node, String name, List<EvalNode> args) {
        return rttiInfo.callFunction(baseUri, node, name, args);
    }
}

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

    private Context parent;
    private ConcurrentMap<String, CompletableFuture<EvalNode>> vars = new ConcurrentHashMap<>();

    /**
     * This constructor user for creating a child context
     */
    private Context() {
    }

    /**
     * This method used for create a root node
     *
     * @param baseUri  of context
     * @param rttiInfo is global run tipe type info
     * @return created root context
     */
    public static Context createRoot(String baseUri, RunTimeTypeInfo rttiInfo) {
        Context context = new Context();
        context.baseUri = baseUri;
        context.rttiInfo = rttiInfo;
        return context;
    }

    /**
     * This method used for create a child context
     *
     * @return child context
     */
    public Context createNew() {
        Context ctx = new Context();
        ctx.setParent(this);
        ctx.setRttiInfo(rttiInfo); //for fast access to rtti functions
        return ctx;
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

    public void setParent(Context parent) {
        this.parent = parent;
    }

    public ConcurrentMap<String, CompletableFuture<EvalNode>> getVars() {
        return vars;
    }

    public void setVars(ConcurrentMap<String, CompletableFuture<EvalNode>> vars) {
        this.vars = vars;
    }

    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public RunTimeTypeInfo getRttiInfo() {
        return rttiInfo;
    }

    public void setRttiInfo(RunTimeTypeInfo rttiInfo) {
        this.rttiInfo = rttiInfo;
    }

    public void registerUserFunction(HistoneType type, Function function) {
        this.rttiInfo.register(type, function.getName(), function);
    }

    public CompletableFuture<EvalNode> call(String name, List<EvalNode> args) {
        return rttiInfo.callFunction(HistoneType.T_GLOBAL, name, args);
    }

    public CompletableFuture<EvalNode> call(EvalNode node, String name, List<EvalNode> args) {
        return rttiInfo.callFunction(node, name, args);
    }
}

package ru.histone.v2.evaluator;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by inv3r on 13/01/16.
 */
public class Context {
    private String baseUri;
    private Map<String, GlobalFunction> globalFunctions = new HashMap<>();

    private Context parent;
    private ConcurrentMap<String, Object> vars = new ConcurrentHashMap<>();

    public Context createNew() {
        Context ctx = new Context();
        ctx.setParent(this);
        ctx.setGlobalFunctions(globalFunctions);
        return ctx;
    }

    public Context getParent() {
        return parent;
    }

    public void setParent(Context parent) {
        this.parent = parent;
    }

    public ConcurrentMap<String, Object> getVars() {
        return vars;
    }

    public void setVars(ConcurrentMap<String, Object> vars) {
        this.vars = vars;
    }

    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public Map<String, GlobalFunction> getGlobalFunctions() {
        return globalFunctions;
    }

    public void setGlobalFunctions(Map<String, GlobalFunction> globalFunctions) {
        this.globalFunctions = globalFunctions;
    }

    public void registerGlobal(String name, GlobalFunction range) {
        this.globalFunctions.put(name, range);
    }
}

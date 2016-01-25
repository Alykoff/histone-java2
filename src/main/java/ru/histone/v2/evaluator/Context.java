package ru.histone.v2.evaluator;

import ru.histone.HistoneException;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.rtti.RunTimeTypeInfo;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Evaluation context of histone
 * <p>
 * Created by inv3r on 13/01/16.
 */
public class Context {
    private String baseUri;

    private RunTimeTypeInfo rttiInfo;

    private Context parent;
    private ConcurrentMap<String, Object> vars = new ConcurrentHashMap<>();

    /**
     * This constructor user for creating a child context
     */
    private Context() {
    }

    /**
     * This constructor used for create a root node
     *
     * @param baseUri of context
     */
    public Context(String baseUri) {
        this.baseUri = baseUri;
        this.rttiInfo = new RunTimeTypeInfo();
    }

    public Context createNew() {
        Context ctx = new Context();
        ctx.setParent(this);
        ctx.setRttiInfo(rttiInfo); //for fast access to rtti functions
        return ctx;
    }

    public void put(String key, Object value) {
        vars.putIfAbsent(key, value);
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

    public RunTimeTypeInfo getRttiInfo() {
        return rttiInfo;
    }

    public void setRttiInfo(RunTimeTypeInfo rttiInfo) {
        this.rttiInfo = rttiInfo;
    }

    public void registerUserFunction(HistoneType type, Function function) {
        this.rttiInfo.register(type, function.getName(), function);
    }

    public Function getFunction(HistoneType type, String name) throws HistoneException {
        Function function = rttiInfo.getFunc(type, name);
        if (function == null) {
            throw new HistoneException(String.format("Wrong function '%s' for type '%s'", name, type));
        }
        return function;
    }

    public Function getFunction(EvalNode node, String name) throws HistoneException {
        HistoneType type = rttiInfo.getType(node);
        return getFunction(type, name);
    }

    public Function getFunction(String name) throws HistoneException {
        return getFunction(HistoneType.T_GLOBAL, name);
    }
}

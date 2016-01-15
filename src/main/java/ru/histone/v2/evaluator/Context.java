package ru.histone.v2.evaluator;

import ru.histone.v2.parser.node.AstNode;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by inv3r on 13/01/16.
 */
public class Context {

    private final ConcurrentMap<UUID, ConcurrentMap<String, Object>> variables = new ConcurrentHashMap<>();
    private String baseUri;

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public void createContext(AstNode node) {
        variables.put(node.getId(), new ConcurrentHashMap<>());
    }

    public void deleteContext(AstNode node) {
        variables.remove(node.getId());
    }

    public void put(AstNode node, String varName, Object value) {
        ConcurrentMap<String, Object> vars = getNodeContext(node);

        if (vars.containsKey(varName)) {
            throw new IllegalStateException("context with id '" + node.getId() + "' already has immetable value '" + varName + "'");
        }

        vars.put(varName, value);
    }

    private ConcurrentMap<String, Object> getNodeContext(AstNode node) {
        ConcurrentMap<String, Object> vars = variables.get(node.getId());
        if (vars == null) {
            throw new IllegalStateException("context with id '" + node.getId() + "' doesn't exist");
        }
        return vars;
    }

    public Object get(AstNode node, String name) {
        return getNodeContext(node).get(name);
    }

    public static class NodeContext {
        private NodeContext parent;
        private ConcurrentMap<String, Object> vars = new ConcurrentHashMap<>();

        public NodeContext getParent() {
            return parent;
        }

        public void setParent(NodeContext parent) {
            this.parent = parent;
        }

        public ConcurrentMap<String, Object> getVars() {
            return vars;
        }

        public void setVars(ConcurrentMap<String, Object> vars) {
            this.vars = vars;
        }
    }
}

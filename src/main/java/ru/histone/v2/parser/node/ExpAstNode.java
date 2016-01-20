package ru.histone.v2.parser.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 *
 * Created by alexey.nevinsky on 24.12.2015.
 */
public class ExpAstNode implements Serializable {
    public static final int LEAF_NODE_TYPE_ID = Integer.MIN_VALUE;
    protected final UUID id = UUID.randomUUID();
    protected final int type;
    protected List<ExpAstNode> nodes = new ArrayList<>();
    private Object value = null;

    public ExpAstNode(int type) {
        this.type = type;
    }

    public ExpAstNode(AstType type) {
        this(type.getId());
    }

    public ExpAstNode(AstType type, ExpAstNode... res) {
        this(type.getId());
        nodes.addAll(Arrays.asList(res));
    }

    public static ExpAstNode forValue(Object object) {
        ExpAstNode node = new ExpAstNode(LEAF_NODE_TYPE_ID);
        node.setValue(object);
        return node;
    }

    public ExpAstNode add(ExpAstNode node) {
        nodes.add(node);
        return this;
    }

    public ExpAstNode add(ExpAstNode... nodes) {
        this.nodes.addAll(Arrays.asList(nodes));
        return this;
    }

    public Object getValue() {
        return value;
    }

    public ExpAstNode setValue(Object value) {
        this.value = value;
        return this;
    }

    public int getType() {
        return type;
    }

    public List<ExpAstNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<ExpAstNode> nodes) {
        this.nodes.clear();
        this.nodes.addAll(nodes);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ExpAstNode{");
        sb.append("type=");
        AstType t = AstType.fromId(type);
        if (t == null) {
            sb.append(type);
        } else {
            sb.append(t.name()).append("(").append(type).append(")");
        }
        sb.append(", nodes=").append(nodes);
        sb.append(", value=").append(value);
        sb.append('}');
        return sb.toString();
    }

    public ExpAstNode getNode(int index) {
        return nodes.size() >= index + 1 ? nodes.get(index) : null;
    }

    public ExpAstNode escaped() {
        //todo
        return this;
    }

    public UUID getId() {
        return id;
    }
}

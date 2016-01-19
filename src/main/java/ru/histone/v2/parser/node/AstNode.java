package ru.histone.v2.parser.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by alexey.nevinsky on 24.12.2015.
 */
public class AstNode implements Serializable {
    public static final int LEAF_NODE_TYPE_ID = Integer.MIN_VALUE;
    protected final UUID id = UUID.randomUUID();
    protected final int type;
    protected List<AstNode> nodes = new ArrayList<>();
    private Object value = null;

    public AstNode(int type) {
        this.type = type;
    }

    public AstNode(AstType type) {
        this(type.getId());
    }

    public AstNode(AstType type, AstNode res) {
        this(type.getId());
        add(res);
    }

    public static AstNode forValue(Object object) {
        AstNode node = new AstNode(LEAF_NODE_TYPE_ID);
        node.setValue(object);
        return node;
    }

    public AstNode add(AstNode node) {
        nodes.add(node);
        return this;
    }

    public Object getValue() {
        return value;
    }

    public AstNode setValue(Object value) {
        this.value = value;
        return this;
    }

    public AstNode add(AstNode... nodes) {
        this.nodes.addAll(Arrays.asList(nodes));
        return this;
    }

    public int getType() {
        return type;
    }

    public List<AstNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<AstNode> nodes) {
        this.nodes = nodes;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AstNode{");
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

    public AstNode getNode(int index) {
        return nodes.size() >= index + 1 ? nodes.get(index) : null;
    }

    public AstNode escaped() {
        //todo
        return this;
    }

    public UUID getId() {
        return id;
    }
}

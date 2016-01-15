package ru.histone.v2.parser.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by alexey.nevinsky on 24.12.2015.
 */
public class AstNode<T> implements Serializable {
    protected final UUID id;
    protected final int type;
    protected List<AstNode> nodes = new ArrayList<>();
    protected List<T> values = new ArrayList<>();

    public AstNode(int type) {
        this.type = type;
        id = UUID.randomUUID();
    }

    public AstNode(AstType type) {
        this(type.getId());
    }

    public AstNode(AstType type, AstNode res) {
        this(type.getId());
        add(res);
    }

    public static AstNode forValue(Object object) {
        AstNode node = new AstNode(Integer.MIN_VALUE);
        node.addValue(object);
        return node;
    }

    public boolean isSimpleNode() {
        return true;
    }

    public Class<T> getValueClass() {
        throw new IllegalStateException("This is simple node!");
    }

    public T getValue() {
        throw new IllegalStateException("Simple node hasn't have a value!");
    }

    public AstNode add(AstNode node) {
        nodes.add(node);
        return this;
    }

    public AstNode addValue(T node) {
        values.add(node);
        return this;
    }

    public List<T> getValues() {
        return values;
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
        sb.append(", values=").append(values);
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

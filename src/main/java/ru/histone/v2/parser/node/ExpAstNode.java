package ru.histone.v2.parser.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Expression AST Node
 * Created by alexey.nevinsky on 24.12.2015.
 */
public class ExpAstNode extends AstNode implements Serializable {
    public static final int LEAF_NODE_TYPE_ID = Integer.MIN_VALUE;
    protected List<AstNode> nodes = new ArrayList<>();

//    public ExpAstNode(int type) {
//        this.type = type;
//    }

    public ExpAstNode(AstType type) {
        super(type);
    }

    public ExpAstNode(AstType type, AstNode... res) {
        this(type);
        nodes.addAll(Arrays.asList(res));
    }

    public ExpAstNode add(AstNode node) {
        nodes.add(node);
        return this;
    }

    public ExpAstNode add(AstNode... nodes) {
        this.nodes.addAll(Arrays.asList(nodes));
        return this;
    }

    public ExpAstNode addAll(List<AstNode> nodes) {
        this.nodes.addAll(nodes);
        return this;
    }

    public List<AstNode> getNodes() {
        return nodes;
    }

    public void rewriteNodes(List<AstNode> nodes) {
        this.nodes.clear();
        this.nodes.addAll(nodes);
    }

    public ExpAstNode setNode(int index, AstNode node) {
        this.nodes.set(index, node);
        return this;
    }


    @Override
    public String toString() {
        return "{\"ExpAstNode\":{" +
                "\"typeName\": \"" + type.name() + "\", " +
                "\"typeId\": " + type.getId() + "," +
                "\"nodes\": " + nodes + "}";
    }

    public <X extends AstNode> X getNode(int index) {
        return nodes.size() >= index + 1
                ? (X) nodes.get(index)
                : null;
    }

    public AstNode escaped() {
        //todo
        return this;
    }


    public boolean hasValue() {
        return false;
    }

    public int size() {
        if (this.nodes == null) return 0;
        return this.nodes.size();
    }
}

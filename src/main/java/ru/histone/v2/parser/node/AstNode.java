package ru.histone.v2.parser.node;

import java.io.Serializable;
import java.util.UUID;

/**
 *
 * Created by gali.alykoff on 20/01/16.
 */
public abstract class AstNode implements Serializable {
    protected final UUID id = UUID.randomUUID();
    protected final AstType type;

    public AstNode(AstType type) {
        if (type == null) {
            throw new NullPointerException();
        }
        this.type = type;
    }

    public AstType getType() {
        return type;
    }

    public int getTypeId() {
        return type.getId();
    }

    public UUID getId() {
        return id;
    }

    public AstNode escaped() {
        // TODO
        return this;
    }

    public abstract boolean hasValue();

    public abstract int size();
}

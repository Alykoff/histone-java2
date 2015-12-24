package ru.histone.tokenizer;

/**
 * Created by alexey.nevinsky on 21.12.2015.
 */
public enum BaseTokens {
    T_EOF(-1),
    T_ERROR(-2),
    T_TOKEN(-3);

    private int id;

    BaseTokens(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}

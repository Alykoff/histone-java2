package ru.histone.v2.parser.node;

/**
 * Created by alexey.nevinsky on 24.12.2015.
 */
public enum AstRegexType {
    RE_GLOBAL(0x01),
    RE_MULTILINE(0x02),
    RE_IGNORECASE(0x04);

    private final int id;

    AstRegexType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}

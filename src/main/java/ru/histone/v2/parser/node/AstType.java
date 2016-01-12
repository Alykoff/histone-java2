package ru.histone.v2.parser.node;

/**
 * Created by alexey.nevinsky on 24.12.2015.
 */
public enum AstType {
    AST_NOP(0),
    AST_ARRAY(1),
    AST_REGEXP(2),
    AST_THIS(3),
    AST_GLOBAL(4),
    AST_NOT(5),
    AST_AND(6),
    AST_OR(7),
    AST_TERNARY(8),
    AST_ADD(9),
    AST_SUB(10),
    AST_MUL(11),
    AST_DIV(12),
    AST_MOD(13),
    AST_USUB(14),
    AST_LT(15),
    AST_GT(16),
    AST_LE(17),
    AST_GE(18),
    AST_EQ(19),
    AST_NEQ(20),
    AST_REF(21),
    AST_METHOD(22),
    AST_PROP(23),
    AST_CALL(24),
    AST_VAR(25),
    AST_IF(26),
    AST_FOR(27),
    AST_MACRO(28),
    AST_RETURN(29),
    AST_NODES(30),
    AST_NODELIST(31),

    AST_BOR(32),
    AST_BXOR(33),
    AST_BAND(34),

    AST_SUPRESS(35),
    AST_LISTEN(36),
    AST_TRIGGER(37);

    private final int id;

    AstType(int id) {
        this.id = id;
    }

    public static AstType fromId(int id) {
        for (AstType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }
}

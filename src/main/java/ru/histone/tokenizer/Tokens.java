package ru.histone.tokenizer;

/**
 * Created by alexey.nevinsky on 21.12.2015.
 */
public enum Tokens {
    T_PROP(1),
    T_STATEMENT(2),
    T_HEX(3),
    T_FLOAT(4),
    T_INT(5),
    T_REF(6),
    T_SPACES(7),
    T_ID(8),
    T_EOL(9),
    T_NULL(10),
    T_TRUE(11),
    T_FALSE(12),
    T_STAR(13),
    T_PLUS(14),
    T_DOT(15),
    T_AND(16),
    T_MINUS(17),
    T_OR(18),
    T_COLON(19),
    T_COMMA(20),
    T_GT(21),
    T_LT(22),
    T_LE(23),
    T_GE(24),
    T_LPAREN(25),
    T_RPAREN(26),
    T_LBRACKET(27),
    T_RBRACKET(28),
    T_NEQ(29),
    T_EQ(30),
    T_MOD(31),
    T_NOT(32),
    T_QUERY(33),
    T_SLASH(34),
    T_DQUOTE(35),
    T_SQUOTE(36),
    T_BACKSLASH(37),
    T_METHOD(38),
    T_BLOCK_START(39),
    T_BLOCK_END(40),
    T_CMT_START(41),
    T_CMT_END(42),
    T_LITERAL_START(43),
    T_LITERAL_END(44),
    T_THIS(45),
    T_GLOBAL(46),
    T_ELSEIF(47),
    T_RETURN(48),
    T_MACRO(49),
    T_ELSE(50),
    T_IF(51),
    T_IN(52),
    T_FOR(53),
    T_VAR(54),
    T_BOR(55),
    T_BXOR(56),
    T_BAND(57),
    T_BIN(58),
    T_SUPRESS(59),
    T_LISTEN(60),
    T_TRIGGER(61),
    T_ARROW(62),

    T_EOF(-1),
    T_ERROR(-2),
    T_TOKEN(-3);

    private int id;

    Tokens(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}

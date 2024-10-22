package me.white.wlua;

public enum MetaMethodType {
    ADD("__add", 1, 1, false),
    SUBTRACT("__sub", 1, 1, false),
    MULTIPLY("__mul", 1, 1, false),
    DIVIDE("__div", 1, 1, false),
    MODULO("__mod", 1, 1, false),
    POWER("__pow", 1, 1, false),
    UNARY_MINUS("__unm", 1, 0, true),
    INTEGER_DIVIDE("__idiv", 1, 1, false),
    BINARY_AND("__band", 1, 1, false),
    BINARY_OR("__bor", 1, 1, false),
    BINARY_XOR("__bxor", 1, 1, false),
    BINARY_NOT("__bnot", 1, 0, true),
    SHIFT_LEFT("__shl", 1, 1, false),
    SHIFT_RIGHT("__shr", 1, 1, false),
    CONCATENATE("__concat", 1, 1, false),
    LENGTH("__len", 1, 0, true),
    EQUALS("__eq", 1, 1, false),
    LESS_THAN("__lt", 1, 1 , false),
    LESS_EQUAL("__le", 1, 1, false),
    CALL("__call", -1, -1, false),
    CLOSE("__close", 0, 1, false),
    TO_STRING("__tostring", 1, 0, false),
    INDEX(null, 1, 1, false),
    NEW_INDEX(null, 0, 2, false);
    // '__gc' and '__name' are used by c-java interface
    // '__index' and '__newindex' are wrapped

    final String metaMethod;
    final int returns;
    final int parameters;
    // unary methods get the same value twice
    final boolean doubleReference;

    MetaMethodType(String metaMethod, int returns, int parameters, boolean doubleReference) {
        this.metaMethod = metaMethod;
        this.returns = returns;
        this.parameters = parameters;
        this.doubleReference = doubleReference;
    }
}

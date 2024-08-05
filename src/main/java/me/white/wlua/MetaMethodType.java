package me.white.wlua;

import java.lang.reflect.Method;

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
    INDEX(null, 1, 1, false),
    NEW_INDEX(null, 0, 2, false);
    // '__gc' and '__name' are used by c-java interface
    // '__mode' and '__close' are unused
    // '__index' and '__newindex' are wrapped

    final String metaMethod;
    final int returns;
    final int parameters;
    // methods like '__unm' get the same value twice
    final boolean doubleReference;

    MetaMethodType(String metaMethod, int returns, int parameters, boolean doubleReference) {
        this.metaMethod = metaMethod;
        this.returns = returns;
        this.parameters = parameters;
        this.doubleReference = doubleReference;
    }

    void validateSignature(Method method) {
        String what = "Meta method '" + method.getName() + "' of type '" + name() + "'";
        if (method.getParameterCount() < 1 || !method.getParameterTypes()[0].isAssignableFrom(LuaState.class)) {
            throw new IllegalStateException(what + " should take at least 1 parameter, with first parameter of type LuaState.");
        }
        if (returns == 0 && method.getReturnType() != void.class) {
            throw new IllegalStateException(what + " should return void.");
        }
        if (returns == 1 && !LuaValue.class.isAssignableFrom(method.getReturnType())) {
            throw new IllegalStateException(what + " should return value of type LuaValue.");
        }
        if (returns == -1 && !VarArg.class.isAssignableFrom(method.getReturnType())) {
            throw new IllegalStateException(what + " should return value of type VarArg.");
        }
        if (parameters == 0 && method.getParameterCount() != 1) {
            throw new IllegalStateException(what + " should take 0 value parameters.");
        }
        if (parameters == -1) {
            if (method.getParameterCount() != 2 || !method.getParameterTypes()[1].isAssignableFrom(VarArg.class)) {
                throw new IllegalStateException(what + " should take 1 value parameter of type VarArg.");
            }
        } else {
            if (parameters != method.getParameterCount() - 1) {
                throw new IllegalStateException(what + " should take " + parameters + " value parameters of type LuaValue.");
            }
            for (int i = 1; i < method.getParameterCount(); ++i) {
                if (!method.getParameterTypes()[i].isAssignableFrom(LuaValue.class)) {
                    throw new IllegalStateException(what + " should take " + parameters + " value parameters of type LuaValue.");
                }
            }
        }
    }
}

package me.white.wlua;

public interface FunctionValue {
    VarArg run(LuaState state, VarArg args);

    @FunctionalInterface
    interface Function {
        VarArg run(LuaState state, VarArg args);
    }
}

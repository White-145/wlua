package me.white.wlua;

public sealed interface FunctionValue permits FunctionLiteralValue, FunctionRefValue {
    VarArg run(LuaState state, VarArg args);

    @FunctionalInterface
    interface Function {
        VarArg run(LuaState state, VarArg args);
    }
}

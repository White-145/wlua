package me.white.wlua;

// interface meant to unite literal and reference function values
public sealed interface FunctionValue permits FunctionLiteralValue, FunctionRefValue {
    VarArg run(LuaState state, VarArg args);

    @FunctionalInterface
    interface Function {
        VarArg run(LuaState state, VarArg args) throws LuaException;
    }
}

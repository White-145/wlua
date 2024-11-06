package me.white.wlua;

@FunctionalInterface
public interface JavaFunction {
    VarArg run(LuaState state, VarArg args);
}

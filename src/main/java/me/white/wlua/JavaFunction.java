package me.white.wlua;

@FunctionalInterface
public interface JavaFunction {
    VarArg run(LuaThread thread, VarArg args) throws LuaException;
}

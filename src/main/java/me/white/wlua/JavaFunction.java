package me.white.wlua;

@FunctionalInterface
public interface JavaFunction {
    int run(LuaState state, int params);
}

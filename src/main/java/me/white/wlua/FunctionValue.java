package me.white.wlua;

import java.util.Objects;

public class FunctionValue extends LuaValue {
    private final Function function;

    public FunctionValue(Function function) {
        Objects.requireNonNull(function);
        this.function = function;
    }

    public VarArg run(LuaState state, VarArg args) {
        state.checkIsAlive();
        return function.run(state, args);
    }

    @Override
    void push(LuaState state) {
        state.checkIsAlive();
        LuaNatives.pushFunction(state.ptr, function);
    }

    @FunctionalInterface
    public interface Function {
        VarArg run(LuaState state, VarArg args);
    }
}

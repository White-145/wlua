package me.white.wlua;

import java.util.Objects;

public final class FunctionLiteralValue extends LuaValue implements FunctionValue {
    private final Function function;

    public FunctionLiteralValue(Function function) {
        Objects.requireNonNull(function);
        this.function = function;
    }

    @Override
    public ValueType getType() {
        return ValueType.FUNCTION;
    }

    @Override
    void push(LuaState state) {
        state.checkIsAlive();
        LuaNatives.pushFunction(state.ptr, function);
    }

    @Override
    public VarArg run(LuaState state, VarArg args) {
        state.checkIsAlive();
        return function.run(state, args);
    }

}

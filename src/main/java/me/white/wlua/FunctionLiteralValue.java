package me.white.wlua;

import java.util.Objects;

public final class FunctionLiteralValue extends LuaValue implements FunctionValue {
    private final Function function;

    public FunctionLiteralValue(Function function) {
        Objects.requireNonNull(function);
        this.function = function;
    }

    public FunctionRefValue toReference(LuaState state) {
        state.checkIsAlive();
        state.pushValue(this);
        FunctionRefValue ref = state.getReference(-1, FunctionRefValue::new);
        state.pop(1);
        return ref;
    }

    @Override
    public ValueType getType() {
        return ValueType.FUNCTION;
    }

    @Override
    void push(LuaState state) {
        LuaNatives.pushFunction(state.ptr, function);
    }

    @Override
    public VarArg run(LuaState state, VarArg args) {
        state.checkIsAlive();
        return function.run(state, args);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof FunctionLiteralValue)) {
            return false;
        }
        return function == ((FunctionLiteralValue)obj).function;
    }

    @Override
    public int hashCode() {
        return function.hashCode();
    }
}

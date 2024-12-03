package me.white.wlua;

public final class FunctionValue extends RefValue {
    FunctionValue(LuaState state, int reference) {
        super(state, reference);
    }

    @Override
    public ValueType getType() {
        return ValueType.FUNCTION;
    }

    public VarArg call(VarArg args) {
        return call(state, args);
    }
}

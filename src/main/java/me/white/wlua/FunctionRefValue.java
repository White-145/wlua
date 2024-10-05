package me.white.wlua;

public final class FunctionRefValue extends LuaValue.Ref implements FunctionValue {
    FunctionRefValue(LuaState state, int index) {
        super(state, index);
    }

    @Override
    public ValueType getType() {
        return ValueType.FUNCTION;
    }

    @Override
    public VarArg run(LuaState state, VarArg args) {
        checkIsAlive();
        if (!state.isSubThread(this.state)) {
            throw new IllegalStateException("Could not move reference between states.");
        }
        return state.run(this, args);
    }
}

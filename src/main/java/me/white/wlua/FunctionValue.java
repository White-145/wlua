package me.white.wlua;

public final class FunctionValue extends RefValue {
    FunctionValue(LuaState state, int reference) {
        super(state, reference);
    }

    @Override
    public ValueType getType() {
        return ValueType.FUNCTION;
    }

    public VarArg run(VarArg args) {
        checkIsAlive();
        if (!state.isSubThread(this.state)) {
            throw new IllegalStateException("Could not move reference between states.");
        }
        return state.run(this, args);
    }
}

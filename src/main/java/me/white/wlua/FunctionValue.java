package me.white.wlua;

import java.lang.foreign.MemorySegment;

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
        state.pushValue(this);
        args.push(state);
        int code = LuaBindings.pcallk(state.address, args.size(), LuaBindings.MULTRET, 0, 0, MemorySegment.NULL);
        LuaException.checkError(code, state);
        return VarArg.collect(state, LuaBindings.gettop(state.address));
    }
}

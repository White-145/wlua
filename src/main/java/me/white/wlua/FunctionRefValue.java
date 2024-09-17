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
        int top = LuaNatives.getTop(state.ptr);
        push(state);
        args.push(state);
        int code = LuaNatives.protectedCall(state.ptr, args.size(), LuaConsts.MULT_RET);
        LuaException.checkError(code, state);
        int amount = LuaNatives.getTop(state.ptr) - top;
        return VarArg.collect(state, amount);
    }
}

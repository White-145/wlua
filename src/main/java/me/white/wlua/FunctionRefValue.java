package me.white.wlua;

public class FunctionRefValue extends LuaValue.Ref {
    protected FunctionRefValue(LuaState state, int index) {
        super(state, index);
    }

    public VarArg run(LuaState state, VarArg args) {
        checkIsAlive();
        int top = LuaNatives.getTop(state.ptr);
        push(state);
        args.push(state);
        // TODO: pcall
        int code = LuaNatives.protectedCall(state.ptr, args.size(), LuaConsts.MULT_RET);
        LuaException.checkError(code, state);
        int amount = LuaNatives.getTop(state.ptr) - top;
        return VarArg.collect(state, amount);
    }
}

package me.white.wlua;

public class FunctionRefValue extends LuaValue.Ref {
    protected FunctionRefValue(LuaState state, int index) {
        super(state, index);
    }

    public VarArg run(LuaState state, VarArg args) {
        checkIsAlive();
        int top = LuaNatives.lua_gettop(state.ptr);
        push(state);
        args.push(state);
        int code = LuaNatives.lua_pcall(state.ptr, args.size(), LuaConsts.MULT_RET, 0);
        LuaException.checkError(code, state);
        return VarArg.collect(state, LuaNatives.lua_gettop(state.ptr) - top);
    }
}

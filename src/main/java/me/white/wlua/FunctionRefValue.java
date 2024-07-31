package me.white.wlua;

public class FunctionRefValue extends LuaValue.Ref {
    protected FunctionRefValue(LuaState state, int index) {
        super(state, index);
    }

    public VarArg run(LuaState state, VarArg args) {
        checkIsAlive();
        int top = LuaNatives.lua_gettop(state.ptr);
        push(state);
        for (LuaValue value : args.getValues()) {
            value.push(state);
        }
        int result = LuaNatives.lua_pcall(state.ptr, args.size(), LuaConsts.MULT_RET, 0);
        if (result != LuaConsts.OK) {
            // TODO: make it into centralized lua error
            String name = "Unknown";
            if (result == LuaConsts.ERR_RUN) {
                name = "Syntax";
            } else if (result == LuaConsts.ERR_MEM) {
                name = "Memory";
            } else if (result == LuaConsts.ERR_ERR) {
                name = "Error";
            }
            String msg = ((StringValue) LuaValue.from(state, -1)).getString();
            throw new IllegalStateException(name + ": " + msg);
        }
        int valueCount = LuaNatives.lua_gettop(state.ptr) - top;
        LuaValue[] values = new LuaValue[valueCount];
        for (int i = 0; i < valueCount; ++i) {
            values[i] = LuaValue.from(state, i - valueCount);
        }
        state.pop(valueCount);
        return new VarArg(values);
    }
}

package me.white.wlua;

import java.util.Objects;

public class FunctionValue extends LuaValue {
    private Function value;

    public FunctionValue(Function function) {
        Objects.requireNonNull(function);
        this.value = function;
    }

    public VarArg run(LuaState state, VarArg args) {
        state.checkIsAlive();
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
            String msg = ((StringValue)LuaValue.from(state, -1)).getString();
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

    @Override
    protected void push(LuaState state) {
        state.checkIsAlive();
        LuaNatives.pushFunction(state.ptr, this);
    }

    @FunctionalInterface
    public interface Function {
        VarArg run(LuaState state, VarArg params);
    }
}

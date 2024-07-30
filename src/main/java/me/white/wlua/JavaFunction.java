package me.white.wlua;

public class JavaFunction extends LuaValue {
    Function function;

    protected JavaFunction(Function function) {
        this.function = function;
    }

    protected int run(LuaState state, int params) {
        LuaValue[] values = new LuaValue[params];
        for (int i = 0; i < params; ++i) {
            values[i] = LuaValue.from(state, i - params);
        }
        LuaNatives.lua_pop(state.ptr, params);
        VarArg returnValues = function.run(state, new VarArg(values));
        for (LuaValue value : returnValues.getValues()) {
            value.push(state);
        }
        return returnValues.size();
    }

    @Override
    protected void push(LuaState state) {
        LuaNatives.pushFunction(state.ptr, this);
    }

    @FunctionalInterface
    public interface Function {
        VarArg run(LuaState state, VarArg params);
    }
}

package me.white.wlua;

public class NilValue extends LuaValue {
    @Override
    void push(LuaState state) {
        LuaNatives.lua_pushnil(state.ptr);
    }

    @Override
    public String toString() {
        return "nil";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        return obj instanceof NilValue;
    }

    @Override
    public int hashCode() {
        return -1;
    }
}

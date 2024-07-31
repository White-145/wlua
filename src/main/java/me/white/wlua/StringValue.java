package me.white.wlua;

import java.util.Objects;

public class StringValue extends LuaValue {
    private String value;

    public StringValue(String value) {
        Objects.requireNonNull(value);
        this.value = value;
    }

    public String getString() {
        return value;
    }

    @Override
    protected void push(LuaState state) {
        state.checkIsAlive();
        LuaNatives.lua_pushstring(state.ptr, value);
    }

    @Override
    public String toString() {
        return "\"" + value + "\"";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof StringValue)) {
            return false;
        }
        return value.equals(((StringValue)obj).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}

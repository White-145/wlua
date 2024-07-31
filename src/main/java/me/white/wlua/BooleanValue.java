package me.white.wlua;

public class BooleanValue extends LuaValue {
    private boolean value;

    public BooleanValue(boolean value) {
        this.value = value;
    }

    public boolean getBoolean() {
        return value;
    }

    @Override
    protected void push(LuaState state) {
        state.checkIsAlive();
        LuaNatives.lua_pushboolean(state.ptr, value ? 1 : 0);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BooleanValue)) {
            return false;
        }
        return value == ((BooleanValue) obj).value;
    }

    @Override
    public int hashCode() {
        return value ? 1 : 0;
    }
}

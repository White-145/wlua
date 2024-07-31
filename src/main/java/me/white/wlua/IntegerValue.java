package me.white.wlua;

public class IntegerValue extends LuaValue {
    private long value;

    public IntegerValue(long value) {
        this.value = value;
    }

    public long getInteger() {
        return value;
    }

    @Override
    protected void push(LuaState state) {
        state.checkIsAlive();
        LuaNatives.lua_pushinteger(state.ptr, value);
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
        if (!(obj instanceof IntegerValue)) {
            return false;
        }
        return value == ((IntegerValue)obj).value;
    }

    @Override
    public int hashCode() {
        return (int)value;
    }
}

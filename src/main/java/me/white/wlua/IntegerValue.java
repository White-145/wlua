package me.white.wlua;

public class IntegerValue extends NumberValue {
    private long value;

    public IntegerValue(double value) {
        super(value);
        this.value = (long)value;
    }

    public IntegerValue(long value) {
        super((double)value);
        this.value = value;
    }

    public long getInteger() {
        return value;
    }

    @Override
    void push(LuaState state) {
        state.checkIsAlive();
        LuaNatives.lua_pushinteger(state.ptr, value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public int hashCode() {
        return (int)value;
    }
}

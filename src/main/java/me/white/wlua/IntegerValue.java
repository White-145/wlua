package me.white.wlua;

public final class IntegerValue extends NumberValue {
    private final long value;

    public IntegerValue(long value) {
        super((double)value);
        this.value = value;
    }

    public IntegerValue(double value) {
        this((long)value);
    }

    @Override
    void push(LuaState state) {
        LuaNatives.pushInteger(state.ptr, value);
    }

    @Override
    public long toInteger() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}

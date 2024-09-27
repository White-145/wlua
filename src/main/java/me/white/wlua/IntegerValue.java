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
    public long getInteger() {
        return value;
    }

    @Override
    void push(LuaState state) {
        state.checkIsAlive();
        LuaNatives.pushInteger(state.ptr, value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}

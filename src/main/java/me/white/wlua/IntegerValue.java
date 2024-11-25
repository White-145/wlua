package me.white.wlua;

public final class IntegerValue extends NumberValue {
    private final int value;

    public IntegerValue(int value) {
        super(value);
        this.value = value;
    }

    public IntegerValue(double value) {
        this((int)value);
    }

    @Override
    void push(LuaThread thread) {
        LuaBindings.pushinteger(thread.address, value);
    }

    @Override
    public int toInteger() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}

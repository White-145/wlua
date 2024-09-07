package me.white.wlua;

public sealed class NumberValue extends LuaValue permits IntegerValue {
    private final double value;

    public NumberValue(double value) {
        this.value = value;
    }

    public double getNumber() {
        return value;
    }

    @Override
    void push(LuaState state) {
        state.checkIsAlive();
        LuaNatives.pushNumber(state.ptr, value);
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
        if (!(obj instanceof NumberValue)) {
            return false;
        }
        return value == ((NumberValue)obj).value;
    }

    @Override
    public int hashCode() {
        return (int)value;
    }
}

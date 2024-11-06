package me.white.wlua;

public sealed class NumberValue extends LuaValue permits IntegerValue {
    private final double value;

    public NumberValue(double value) {
        this.value = value;
    }

    @Override
    void push(LuaState state) {
        LuaNatives.pushNumber(state.ptr, value);
    }

    @Override
    public ValueType getType() {
        return ValueType.NUMBER;
    }

    @Override
    public boolean isNumber() {
        return true;
    }

    @Override
    public double toNumber() {
        return value;
    }

    @Override
    public long toInteger() {
        return (long)value;
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
        return Double.hashCode(value);
    }
}

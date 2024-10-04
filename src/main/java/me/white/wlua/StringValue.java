package me.white.wlua;

import java.util.Objects;

public final class StringValue extends LuaValue {
    private final String value;

    public StringValue(String value) {
        Objects.requireNonNull(value);
        this.value = value;
    }

    @Override
    public double getNumber() {
        return Double.parseDouble(value);
    }

    @Override
    public long getInteger() {
        return (long)Double.parseDouble(value);
    }

    @Override
    public String getString() {
        return value;
    }

    @Override
    public ValueType getType() {
        return ValueType.STRING;
    }

    @Override
    void push(LuaState state) {
        LuaNatives.pushString(state.ptr, value);
    }

    @Override
    public String toString() {
        return value;
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

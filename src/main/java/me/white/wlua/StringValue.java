package me.white.wlua;

import java.lang.foreign.Arena;
import java.util.Objects;

public final class StringValue extends LuaValue {
    private final String value;

    public StringValue(String value) {
        Objects.requireNonNull(value);
        this.value = value;
    }

    @Override
    void push(LuaThread thread) {
        try (Arena arena = Arena.ofConfined()) {
            LuaBindings.pushstring(thread.address, arena.allocateFrom(value));
        }
    }

    @Override
    public ValueType getType() {
        return ValueType.STRING;
    }

    @Override
    public boolean isNumber() {
        try {
            Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            return false;
        }
        return true;
    }

    @Override
    public double toNumber() {
        return Double.parseDouble(value);
    }

    @Override
    public int toInteger() {
        return (int)Double.parseDouble(value);
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

package me.white.wlua;

import java.lang.foreign.Arena;
import java.lang.foreign.ValueLayout;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public final class StringValue extends LuaValue {
    private final String value;
    private final byte[] bytes;

    public StringValue(String value) {
        Objects.requireNonNull(value);
        this.value = value;
        bytes = value.getBytes(StandardCharsets.UTF_8);
    }

    public StringValue(byte[] bytes) {
        Objects.requireNonNull(bytes);
        value = new String(bytes, StandardCharsets.UTF_8);
        this.bytes = bytes;
    }

    StringValue(String value, byte[] bytes) {
        Objects.requireNonNull(value);
        Objects.requireNonNull(bytes);
        this.value = value;
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        byte[] arr = new byte[bytes.length];
        System.arraycopy(bytes, 0, arr, 0, bytes.length);
        return arr;
    }

    @Override
    void push(LuaThread thread) {
        try (Arena arena = Arena.ofConfined()) {
            LuaBindings.pushlstring(thread.address, arena.allocateFrom(ValueLayout.JAVA_BYTE, bytes), bytes.length);
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
    public boolean isInteger() {
        double number;
        try {
            number = Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            return false;
        }
        return Double.isFinite(number) && (int)number == number;
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
        return Arrays.equals(bytes, ((StringValue)obj).bytes);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}

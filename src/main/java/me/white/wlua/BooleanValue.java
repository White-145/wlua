package me.white.wlua;

public final class BooleanValue extends LuaValue {
    public static final BooleanValue FALSE = new BooleanValue(false);
    public static final BooleanValue TRUE = new BooleanValue(true);
    private final boolean value;

    private BooleanValue(boolean value) {
        this.value = value;
    }

    @Override
    public boolean getBoolean() {
        return value;
    }

    @Override
    public ValueType getType() {
        return ValueType.BOOLEAN;
    }

    @Override
    void push(LuaState state) {
        LuaNatives.pushBoolean(state.ptr, value);
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
        if (!(obj instanceof BooleanValue)) {
            return false;
        }
        return value == ((BooleanValue)obj).value;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(value);
    }
}

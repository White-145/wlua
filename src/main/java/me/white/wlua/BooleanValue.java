package me.white.wlua;

public class BooleanValue extends LuaValue {
    private final boolean value;

    public BooleanValue(boolean value) {
        this.value = value;
    }

    public boolean getBoolean() {
        return value;
    }

    @Override
    void push(LuaState state) {
        state.checkIsAlive();
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
        return value ? 1 : 0;
    }
}
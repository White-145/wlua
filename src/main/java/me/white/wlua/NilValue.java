package me.white.wlua;

public final class NilValue extends LuaValue {
    public static final NilValue INSTANCE = new NilValue();

    private NilValue() { }

    @Override
    void push(LuaThread thread) {
        LuaBindings.pushnil(thread.address);
    }

    @Override
    public ValueType getType() {
        return ValueType.NIL;
    }

    @Override
    public boolean isNil() {
        return true;
    }

    @Override
    public boolean toBoolean() {
        return false;
    }

    @Override
    public String toString() {
        return "nil";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LuaValue)) {
            return false;
        }
        return ((LuaValue)obj).isNil();
    }

    @Override
    public int hashCode() {
        return -98518;
    }
}

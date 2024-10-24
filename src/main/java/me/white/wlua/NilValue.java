package me.white.wlua;

public final class NilValue extends LuaValue {
    public static final NilValue INSTANCE = new NilValue();

    private NilValue() { }

    @Override
    public boolean isNil() {
        return true;
    }

    @Override
    public boolean getBoolean() {
        return false;
    }

    @Override
    public ValueType getType() {
        return ValueType.NIL;
    }

    @Override
    void push(LuaState state) {
        LuaNatives.pushNil(state.ptr);
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
        return 0;
    }
}

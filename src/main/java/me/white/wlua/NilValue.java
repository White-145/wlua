package me.white.wlua;

public sealed class NilValue extends LuaValue permits FailValue {
    @Override
    void push(LuaState state) {
        LuaNatives.pushNil(state.ptr);
    }

    @Override
    public boolean isNil() {
        return true;
    }

    @Override
    public boolean isTrue() {
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
        return obj instanceof NilValue;
    }

    @Override
    public int hashCode() {
        return -1;
    }
}

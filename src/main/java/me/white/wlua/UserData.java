package me.white.wlua;

public non-sealed class UserData extends LuaValue {
    @Override
    public final boolean isNil() {
        return super.isNil();
    }

    @Override
    public final boolean isNumber() {
        return super.isNumber();
    }

    @Override
    public final boolean toBoolean() {
        return super.toBoolean();
    }

    @Override
    public final double toNumber() {
        return super.toNumber();
    }

    @Override
    public final int toInteger() {
        return super.toInteger();
    }

    @Override
    final void push(LuaThread thread) {
        ObjectRegistry.pushObject(thread, this);
    }

    @Override
    public final ValueType getType() {
        return ValueType.USER_DATA;
    }
}

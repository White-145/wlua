package me.white.wlua;

public non-sealed class UserData extends LuaValue {
    private static int nextId = 1;
    private final int id;

    {
        id = nextId;
        nextId += 1;
    }

    @Override
    public final boolean isNil() {
        return super.isNil();
    }

    @Override
    public final boolean isNumber() {
        return super.isNumber();
    }

    @Override
    public final boolean isInteger() {
        return super.isInteger();
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
        LuaBindings.rawgeti(thread.address, LuaBindings.REGISTRYINDEX, LuaState.RIDX_USERDATAS);
        if (LuaBindings.rawgeti(thread.address, -1, id) == LuaBindings.TNIL) {
            LuaBindings.settop(thread.address, -2);
            thread.pushObject(this);
            LuaBindings.pushvalue(thread.address, -1);
            LuaBindings.rawseti(thread.address, -3, id);
        }
        LuaBindings.copy(thread.address, -1, -2);
        LuaBindings.settop(thread.address, -2);
    }

    @Override
    public final ValueType getType() {
        return ValueType.USER_DATA;
    }
}

package me.white.wlua;

public final class UserDataValue extends LuaValue {
    private final Object value;

    public UserDataValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @SuppressWarnings("unchecked")
    public <T> T getT() {
        return (T)value;
    }

    @Override
    void push(LuaThread thread) {
        ObjectRegistry.pushObject(thread, value);
    }

    @Override
    public ValueType getType() {
        return ValueType.USER_DATA;
    }
}

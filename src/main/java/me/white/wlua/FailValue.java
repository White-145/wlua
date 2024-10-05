package me.white.wlua;

final class FailValue extends NilValue {
    static final FailValue INSTANCE = new FailValue();

    private FailValue() {
        super();
    }

    @Override
    void push(LuaState state) {
        LuaNatives.pushFail(state.ptr);
    }

    @Override
    public String toString() {
        return "fail";
    }
}

package me.white.wlua;

final class FailValue extends NilValue {
    @Override
    void push(LuaState state) {
        LuaNatives.pushFail(state.ptr);
    }
}

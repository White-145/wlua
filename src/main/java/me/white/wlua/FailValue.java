package me.white.wlua;

final class FailValue extends NilValue {
    @Override
    void push(LuaState state) {
        state.checkIsAlive();
        LuaNatives.pushFail(state.ptr);
    }
}

package me.white.wlua;

public class ThreadValue extends LuaValue {
    private LuaState thread;

    public ThreadValue(LuaState mainThread, LuaState thread) {
        this.thread = thread;
    }

    public LuaState getThread() {
        return thread;
    }

    @Override
    protected void push(LuaState state) {
        state.checkIsAlive();
        if (state.mainThread.isSubThread(thread)) {
            throw new IllegalStateException("Could not push thread to the separate lua state.");
        }
        LuaNatives.lua_pushthread(thread.ptr);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ThreadValue)) {
            return false;
        }
        return thread.equals(((ThreadValue)obj).thread);
    }

    @Override
    public int hashCode() {
        return thread.hashCode();
    }
}

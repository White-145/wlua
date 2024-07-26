package me.white.wlua;

import java.util.ArrayList;
import java.util.List;

public class LuaState implements AutoCloseable {
    protected long ptr;
    private int id;
    private LuaState mainThread;
    private List<LuaState> subThreads = new ArrayList<>();

    protected LuaState(long ptr, int state_id, LuaState mainThread) {
        if (ptr == 0) {
            throw new IllegalStateException("Could not create new lua state.");
        }
        this.ptr = ptr;
        this.id = state_id == -1 ? LuaInstances.add(this) : state_id;
        this.mainThread = mainThread == null ? this : mainThread;
        LuaNatives.init_state(ptr, id);
    }

    public LuaState() {
        this(LuaNatives.luaL_newstate(), -1, null);
    }

    protected void addSubThread(LuaState thread) {
        subThreads.add(thread);
    }

    public void push(String str) {
        LuaNatives.lua_pushstring(ptr, str);
    }

    public String getString(int i) {
        if (LuaNatives.lua_isstring(ptr, i) == 0) {
            return null;
        }
        return LuaNatives.lua_tostring(ptr, i);
    }

    @Override
    public void close() {
        LuaNatives.lua_close(ptr);
    }
}

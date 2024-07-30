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
        LuaNatives.initState(ptr, id);
    }

    public LuaState() {
        this(LuaNatives.newState(), -1, null);
    }

    protected void addSubThread(LuaState thread) {
        subThreads.add(thread);
    }

    public void loadChunk(String chunk) throws IllegalStateException {
        int code = LuaNatives.luaL_loadstring(ptr, chunk);
        if (code != LuaConsts.OK) {
            String name = "Unknown";
            if (code == LuaConsts.ERR_SYNTAX) {
                name = "Syntax";
            }
            if (code == LuaConsts.ERR_MEM) {
                name = "Memory";
            }
            String msg = LuaNatives.lua_tostring(ptr, -1);
            throw new IllegalStateException(name + " error: " + msg);
        }
    }

    public void run(int params, int values) {
        int code = LuaNatives.lua_pcall(ptr, params, values, 0);
        if (code != LuaConsts.OK) {
            String name = "Unknown";
            if (code == LuaConsts.ERR_RUN) {
                name = "Syntax";
            } else if (code == LuaConsts.ERR_MEM) {
                name = "Memory";
            } else if (code == LuaConsts.ERR_ERR) {
                name = "Error message";
            }
            String msg = LuaNatives.lua_tostring(ptr, -1);
            throw new IllegalStateException(name + " error: " + msg);
        }
    }

    @Override
    public void close() {
        if (mainThread == this) {
            for (LuaState thread : subThreads) {
                LuaInstances.remove(thread.id);
            }
            subThreads.clear();
            LuaInstances.remove(id);
            LuaNatives.lua_close(ptr);
        } else {
            mainThread.subThreads.remove(this);
            LuaInstances.remove(id);
            LuaNatives.removeState(ptr);
        }
    }
}

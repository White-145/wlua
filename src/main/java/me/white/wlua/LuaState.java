package me.white.wlua;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LuaState implements AutoCloseable {
    protected long ptr;
    protected List<LuaState> subThreads = new ArrayList<>();
    protected Set<Integer> aliveReferences = new HashSet<>();
    protected LuaState mainThread;
    private boolean isClosed = false;
    private int id;

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

    public void checkIsAlive() {
        if (isClosed()) {
            throw new IllegalStateException("Could not use closed lua state.");
        }
    }

    protected void addSubThread(LuaState thread) {
        checkIsAlive();
        subThreads.add(thread);
    }

    public boolean isSubThread(LuaState thread) {
        return thread == this || subThreads.contains(thread);
    }

    protected void pop(int n) {
        checkIsAlive();
        LuaNatives.lua_pop(ptr, n);
    }

    public void openLibs() {
        checkIsAlive();
        LuaNatives.luaL_openlibs(ptr);
    }

    public void setGlobal(LuaValue value, String name) {
        checkIsAlive();
        value.push(this);
        LuaNatives.lua_setglobal(ptr, name);
    }

    public LuaValue getGlobal(String name) {
        checkIsAlive();
        LuaNatives.lua_getglobal(ptr, name);
        LuaValue value = LuaValue.from(this, -1);
        pop(1);
        return value;
    }

    public void run(String chunk) {
        checkIsAlive();
        LuaValue.load(this, chunk).run(this, new VarArg());
    }

    public boolean isClosed() {
        return mainThread.isClosed;
    }

    @Override
    public void close() {
        if (isClosed()) {
            return;
        }
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
        isClosed = true;
    }
}

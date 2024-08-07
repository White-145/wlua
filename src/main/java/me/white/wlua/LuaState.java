package me.white.wlua;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LuaState extends LuaValue implements AutoCloseable {
    protected long ptr;
    protected List<LuaState> subThreads = new ArrayList<>();
    protected Set<Integer> aliveReferences = new HashSet<>();
    protected LuaState mainThread;
    private boolean isClosed = false;
    private int id;

    LuaState(long ptr, int state_id, LuaState mainThread) {
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

    void addSubThread(LuaState thread) {
        checkIsAlive();
        subThreads.add(thread);
    }

    void pop(int n) {
        checkIsAlive();
        LuaNatives.pop(ptr, n);
    }

    void pushValue(LuaValue value) {
        checkIsAlive();
        value.push(this);
    }

    public void checkIsAlive() {
        if (isClosed()) {
            throw new IllegalStateException("Could not use closed lua state.");
        }
    }

    public boolean isSubThread(LuaState thread) {
        return thread == this || subThreads.contains(thread);
    }

    public void openLibs() {
        checkIsAlive();
        LuaNatives.openLibs(ptr);
    }

    public void setGlobal(LuaValue value, String name) {
        checkIsAlive();
        value.push(this);
        LuaNatives.setGlobal(ptr, name);
    }

    public LuaValue getGlobal(String name) {
        checkIsAlive();
        LuaNatives.getGlobal(ptr, name);
        LuaValue value = LuaValue.from(this, -1);
        pop(1);
        return value;
    }

    public void run(String chunk) {
        checkIsAlive();
        LuaValue.chunk(this, chunk).run(this, new VarArg());
    }

    public boolean isClosed() {
        return mainThread.isClosed;
    }

    @Override
    void push(LuaState state) {
        state.checkIsAlive();
        if (state.mainThread.isSubThread(this)) {
            throw new IllegalStateException("Could not push thread to the separate lua state.");
        }
        LuaNatives.pushThread(ptr);
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
            LuaNatives.closeState(ptr);
        } else {
            mainThread.subThreads.remove(this);
            LuaInstances.remove(id);
            LuaNatives.removeState(ptr);
        }
        isClosed = true;
    }
}

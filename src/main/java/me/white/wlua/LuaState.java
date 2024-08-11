package me.white.wlua;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LuaState extends LuaValue implements AutoCloseable {
    private boolean isClosed = false;
    private int id;
    final Object LOCK = new Object();
    long ptr;
    List<LuaState> subThreads = new ArrayList<>();
    Set<Integer> aliveReferences = new HashSet<>();
    LuaState mainThread;

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
        synchronized (LOCK) {
            checkIsAlive();
            subThreads.add(thread);
        }
    }

    void pop(int n) {
        synchronized (LOCK) {
            checkIsAlive();
            LuaNatives.pop(ptr, n);
        }
    }

    void pushValue(LuaValue value) {
        synchronized (LOCK) {
            checkIsAlive();
            value.push(this);
        }
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
        synchronized (LOCK) {
            checkIsAlive();
            LuaNatives.openLibs(ptr);
        }
    }

    public void setGlobal(LuaValue value, String name) {
        synchronized (LOCK) {
            checkIsAlive();
            value.push(this);
            LuaNatives.setGlobal(ptr, name);
        }
    }

    public LuaValue getGlobal(String name) {
        synchronized (LOCK) {
            checkIsAlive();
            LuaNatives.getGlobal(ptr, name);
            LuaValue value = LuaValue.from(this, -1);
            LuaNatives.pop(ptr, 1);
            return value;
        }
    }

    public void run(String chunk) {
        synchronized (LOCK) {
            checkIsAlive();
            FunctionRefValue ref = LuaValue.chunk(this, chunk);
            ref.run(this, new VarArg());
        }
    }

    public boolean isClosed() {
        return mainThread.isClosed;
    }

    @Override
    void push(LuaState state) {
        synchronized (LOCK) {
            state.checkIsAlive();
            if (state.mainThread.isSubThread(this)) {
                throw new IllegalStateException("Could not push thread to the separate lua state.");
            }
            LuaNatives.pushThread(ptr);
        }
    }

    @Override
    public void close() {
        synchronized (LOCK) {
            if (isClosed()) {
                return;
            }
            LuaInstances.remove(id);
            if (mainThread == this) {
                for (LuaState thread : subThreads) {
                    LuaInstances.remove(thread.id);
                }
                subThreads.clear();
                LuaNatives.closeState(ptr);
            } else {
                mainThread.subThreads.remove(this);
                LuaNatives.removeState(ptr);
            }
            isClosed = true;
        }
    }
}

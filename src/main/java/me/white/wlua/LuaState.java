package me.white.wlua;

import java.util.*;

public non-sealed class LuaState extends LuaValue implements AutoCloseable {
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

    public void openLib(Library library, String name) {
        synchronized (LOCK) {
            checkIsAlive();
            if (name == null) {
                library.openGlobal(this);
            } else {
                library.open(this, name);
            }
        }
    }

    public void openLib(Library library) {
        openLib(library, null);
    }

    public void setGlobal(String name, LuaValue value) {
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

    public FunctionRefValue load(String chunk) {
        synchronized (LOCK) {
            checkIsAlive();
            return LuaValue.chunk(this, chunk);
        }
    }

    public VarArg run(String code) {
        synchronized (LOCK) {
            return LuaValue.chunk(this, code).run(this, new VarArg());
        }
    }

    public VarArg run(FunctionRefValue chunk, VarArg args) {
        synchronized (LOCK) {
            return chunk.run(this, args);
        }
    }

    private VarArg resume(FunctionRefValue chunk, VarArg args) {
        synchronized (LOCK) {
            checkIsAlive();
            int top = LuaNatives.getTop(ptr);
            if (chunk != null) {
                chunk.push(this);
            } else if (!isSuspended()) {
                throw new IllegalStateException("Cannot resume not suspended state.");
            }
            args.push(this);
            int code = LuaNatives.resume(ptr, args.size());
            LuaException.checkError(code, this);
            int amount = LuaNatives.getTop(ptr) - top;
            return VarArg.collect(this, amount);
        }
    }

    public VarArg start(FunctionRefValue chunk, VarArg args) {
        return resume(chunk, args);
    }

    public VarArg resume(VarArg args) {
        return resume(null, args);
    }

    public VarArg yield(VarArg result) {
        if (!isYieldable()) {
            throw new IllegalStateException("Cannot yield non-yieldable state");
        }
        synchronized (LOCK) {
            LuaNatives.yield(ptr);
            return result;
        }
    }

    public VarArg yield() {
        return this.yield(new VarArg());
    }

    public boolean isYieldable() {
        return LuaNatives.isYieldable(ptr);
    }

    public boolean isSuspended() {
        return LuaNatives.isSuspended(ptr);
    }

    public boolean isClosed() {
        return mainThread.isClosed;
    }

    @Override
    final void push(LuaState state) {
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

package me.white.wlua;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public sealed class LuaThread extends LuaValue implements AutoCloseable permits LuaState {
    static final String STATE_ID_FIELD = "state_id";
    protected final int id;
    final LuaState state;
    final MemorySegment address;
    boolean isClosed = false;

    LuaThread(LuaState state, MemorySegment address) {
        this.state = state;
        if (state != null) {
            state.threads.add(this);
        }
        this.address = address;
        id = ObjectRegistry.register(this);
        LuaBindings.pushthread(address);
        LuaBindings.pushinteger(address, id);
        LuaBindings.settable(address, LuaBindings.REGISTRYINDEX);
    }

    static LuaThread getThread(MemorySegment address) {
        LuaBindings.pushthread(address);
        if (LuaBindings.gettable(address, LuaBindings.REGISTRYINDEX) != LuaBindings.TNUMBER) {
            try (Arena arena = Arena.ofConfined()) {
                if (LuaBindings.getfield(address, LuaBindings.REGISTRYINDEX, arena.allocateFrom(STATE_ID_FIELD)) != LuaBindings.TNUMBER) {
                    throw new IllegalStateException("Could not adopt foreign thread.");
                }
            }
            int id = LuaBindings.tointegerx(address, LuaBindings.REGISTRYINDEX, MemorySegment.NULL);
            LuaState state = (LuaState)ObjectRegistry.get(id);
            LuaThread thread = new LuaThread(state, address);
            state.threads.add(state);
            return thread;
        }
        int id = LuaBindings.tointegerx(address, -1, MemorySegment.NULL);
        LuaBindings.settop(address, -2);
        return (LuaThread)ObjectRegistry.get(id);
    }

    void pushValue(LuaValue value) {
        if (value == null) {
            LuaBindings.pushnil(address);
        } else {
            value.push(this);
        }
    }

    public LuaState getState() {
        return state == null ? (LuaState)this : state;
    }

    public boolean isAlive() {
        return !isClosed && !getState().isClosed;
    }

    public void checkIsAlive() {
        if (!isAlive()) {
            throw new IllegalStateException("Could not use closed thread.");
        }
    }

    // INTERFACE IMPLEMENTATION START (marker to help with organization)

    public LuaValue getGlobal(String name) {
        checkIsAlive();
        try (Arena arena = Arena.ofConfined()) {
            LuaBindings.getglobal(address, arena.allocateFrom(name));
        }
        LuaValue value = LuaValue.from(this, -1);
        LuaBindings.settop(address, -2);
        return value;
    }

    public void setGlobal(String name, LuaValue value) {
        checkIsAlive();
        pushValue(value);
        try (Arena arena = Arena.ofConfined()) {
            LuaBindings.setglobal(address, arena.allocateFrom(name));
        }
    }

    public TableValue getGlobalTable() {
        checkIsAlive();
        LuaBindings.geti(address, LuaBindings.REGISTRYINDEX, LuaBindings.RIDX_GLOBALS);
        TableValue value = (TableValue)LuaValue.from(this, -1);
        LuaBindings.settop(address, -2);
        return value;
    }

    public VarArg run(String chunk) {
        checkIsAlive();
        return LuaValue.load(this, chunk).run(VarArg.empty());
    }

    public VarArg start(FunctionValue chunk, VarArg args) {
        checkIsAlive();
        pushValue(chunk);
        return resume(args);
    }

    public VarArg resume(VarArg args) {
        checkIsAlive();
        args.push(this);
        int results;
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment resultsSegment = arena.allocate(ValueLayout.JAVA_INT);
            int code = LuaBindings.resume(address, MemorySegment.NULL, args.size(), resultsSegment);
            LuaException.checkError(code, this);
            results = resultsSegment.get(ValueLayout.JAVA_INT, 0);
        }
        return VarArg.collect(this, results);
    }

    // this method returns VarArg purely for syntax purposes
    public VarArg yield(VarArg args) {
        checkIsAlive();
        args.push(this);
        LuaBindings.yieldk(address, args.size(), 0, MemorySegment.NULL);
        return VarArg.empty();
    }

    public boolean isYieldable() {
        return isAlive() && LuaBindings.isyieldable(address) == 1;
    }

    public boolean isSuspended() {
        return isAlive() && LuaBindings.status(address) == LuaBindings.YIELD;
    }

    // INTERFACE IMPLEMENTATION END

    @Override
    void push(LuaThread thread) {
        LuaBindings.pushthread(address);
        LuaBindings.xmove(address, thread.address, 1);
    }

    @Override
    public ValueType getType() {
        return ValueType.THREAD;
    }

    @Override
    public void close() {
        if (!isAlive()) {
            return;
        }
        int code = LuaBindings.closethread(address, getState().address);
        LuaException.checkError(code, this);
        state.threads.remove(this);
        isClosed = true;
    }
}

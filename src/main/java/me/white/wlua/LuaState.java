package me.white.wlua;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.HashSet;
import java.util.Set;

public final class LuaState extends LuaThread {
    private static final String REFERENCES_FIELD = "references";
    static final Arena GLOBAL = Arena.ofAuto();
    static final MemorySegment GC_FUNCTION = LuaBindings.stubCFunction(GLOBAL, address -> {
        LuaBindings.getiuservalue(address, -1, 1);
        int id = LuaBindings.tointegerx(address, -1, MemorySegment.NULL);
        ObjectRegistry.remove(id);
        return 0;
    });
    static final MemorySegment RUN_FUNCTION = LuaBindings.stubCFunction(GLOBAL, address -> {
        LuaBindings.getiuservalue(address, LuaBindings.REGISTRYINDEX - 1, 1);
        int id = LuaBindings.tointegerx(address, -1, MemorySegment.NULL);
        LuaBindings.settop(address, -2);
        Object object = ObjectRegistry.get(id);
        if (!(object instanceof JavaFunction)) {
            try (Arena arena = Arena.ofConfined()) {
                LuaBindings.pushstring(address, arena.allocateFrom("error getting function"));
            }
            LuaBindings.error(address);
            return 0;
        }
        LuaThread thread = LuaThread.getThread(address);
        VarArg args = VarArg.collect(thread, LuaBindings.gettop(address));
        VarArg results = ((JavaFunction)object).run(thread, args);
        results.push(thread);
        return results.size();
    });
    private final Set<Integer> references = new HashSet<>();
    private int nextReference = 1;
    final Set<LuaThread> threads = new HashSet<>();

    public LuaState() {
        super(null, LuaBindings.auxiliaryNewstate());
        LuaBindings.createtable(address, 0, 0);
        try (Arena arena = Arena.ofConfined()) {
            LuaBindings.setfield(address, LuaBindings.REGISTRYINDEX, arena.allocateFrom(REFERENCES_FIELD));
        }
        LuaBindings.pushinteger(address, id);
        try (Arena arena = Arena.ofConfined()) {
            LuaBindings.setfield(address, LuaBindings.REGISTRYINDEX, arena.allocateFrom(LuaThread.STATE_ID_FIELD));
        }
    }

    public LuaThread subThread() {
        MemorySegment threadAddress = LuaBindings.newthread(address);
        LuaBindings.settop(address, -2);
        return new LuaThread(this, threadAddress);
    }

    public boolean isSubThread(LuaThread thread) {
        return thread == this || threads.contains(thread);
    }

    int getReference(int index) {
        int absindex = LuaBindings.absindex(address, index);
        try (Arena arena = Arena.ofConfined()) {
            LuaBindings.getfield(address, LuaBindings.REGISTRYINDEX, arena.allocateFrom(REFERENCES_FIELD));
        }
        LuaBindings.pushvalue(address, absindex);
        LuaBindings.gettable(address, -2);
        if (LuaBindings.isinteger(address, -1) == 1) {
            int reference = LuaBindings.tointegerx(address, -1, MemorySegment.NULL);
            LuaBindings.settop(address, -3);
            return reference;
        }
        int reference = nextReference;
        nextReference += 1;
        references.add(reference);
        LuaBindings.pushvalue(address, absindex);
        LuaBindings.pushinteger(address, reference);
        LuaBindings.settable(address, -4);
        LuaBindings.pushinteger(address, reference);
        LuaBindings.pushvalue(address, absindex);
        LuaBindings.settable(address, -4);
        LuaBindings.settop(address, -3);
        return reference;
    }

    boolean hasReference(int reference) {
        return references.contains(reference);
    }

    void fromReference(int reference, LuaThread thread) {
        if (!isSubThread(thread)) {
            throw new IllegalStateException("Could not move references between states.");
        }
        try (Arena arena = Arena.ofConfined()) {
            LuaBindings.getfield(thread.address, LuaBindings.REGISTRYINDEX, arena.allocateFrom(REFERENCES_FIELD));
        }
        LuaBindings.pushinteger(thread.address, reference);
        LuaBindings.gettable(thread.address, -2);
        LuaBindings.copy(thread.address, -1, -2);
        LuaBindings.settop(thread.address, -2);
    }

    void cleanReference(int reference) {
        if (!hasReference(reference)) {
            return;
        }
        references.remove(reference);
        try (Arena arena = Arena.ofConfined()) {
            LuaBindings.getfield(address, LuaBindings.REGISTRYINDEX, arena.allocateFrom(REFERENCES_FIELD));
        }
        LuaBindings.pushinteger(address, reference);
        LuaBindings.gettable(address, -2);
        LuaBindings.pushnil(address);
        LuaBindings.settable(address, -3);
        LuaBindings.pushinteger(address, reference);
        LuaBindings.pushnil(address);
        LuaBindings.settable(address, -3);
        LuaBindings.settop(address, -2);
    }

    @Override
    public void close() {
        if (!isAlive()) {
            return;
        }
        Object[] arr = threads.toArray();
        for (Object thread : arr) {
            ((LuaThread)thread).close();
        }
        LuaBindings.close(address);
    }
}

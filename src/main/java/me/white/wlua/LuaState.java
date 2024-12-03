package me.white.wlua;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.*;

public final class LuaState extends LuaThread {
    private static final Arena GLOBAL = Arena.ofAuto();
    private static final MemorySegment GC_FUNCTION = LuaBindings.stubCFunction(GLOBAL, LuaState::gcFunction);
    static final MemorySegment RUN_FUNCTION = LuaBindings.stubCFunction(GLOBAL, LuaState::runFunction);
    static final MemorySegment DUMP_WRITER = LuaBindings.stubWriter(GLOBAL, LuaState::dumpWriter);
    static final int RIDX_REFERENCES = 11;
    static final int RIDX_GC_METATABLE = 12;
    static final int RIDX_USERDATAS = 13;
    static final int RIDX_ERROR_HANDLER = 14;
    final RefManager references = new RefManager(this);
    final Set<LuaThread> threads = new HashSet<>();

    public LuaState() {
        super(null, LuaBindings.auxiliaryNewstate());
        LuaBindings.createtable(address, 0, 0);
        LuaBindings.rawseti(address, LuaBindings.REGISTRYINDEX, RIDX_REFERENCES);
        LuaBindings.createtable(address, 0, 1);
        LuaBindings.pushcclosure(address, GC_FUNCTION, 0);
        try (Arena arena = Arena.ofConfined()) {
            LuaBindings.rawsetp(address, -2, arena.allocateFrom("__gc"));
        }
        LuaBindings.rawseti(address, LuaBindings.REGISTRYINDEX, RIDX_GC_METATABLE);
        LuaBindings.createtable(address, 0, 0);
        LuaBindings.createtable(address, 0, 1);
        try (Arena arena = Arena.ofConfined()) {
            LuaBindings.pushstring(address, arena.allocateFrom("v"));
            LuaBindings.rawsetp(address, -2, arena.allocateFrom("__mode"));
        }
        LuaBindings.setmetatable(address, -2);
        LuaBindings.rawseti(address, LuaBindings.REGISTRYINDEX, RIDX_USERDATAS);
        pushValue(LuaValue.fromFunction(this, LuaState::messageHandler));
        LuaBindings.rawseti(address, LuaBindings.REGISTRYINDEX, RIDX_ERROR_HANDLER);
    }

    private static int gcFunction(MemorySegment address) {
        LuaBindings.getiuservalue(address, 1, 1);
        int id = LuaBindings.tointegerx(address, -1, MemorySegment.NULL);
        LuaBindings.settop(address, -3);
        ObjectManager.remove(id);
        return 0;
    }

    private static int runFunction(MemorySegment address) {
        LuaBindings.getiuservalue(address, LuaBindings.REGISTRYINDEX - 1, 1);
        LuaBindings.getiuservalue(address, -1, 1);
        int id = LuaBindings.tointegerx(address, -1, MemorySegment.NULL);
        LuaBindings.settop(address, -3);
        Object object = ObjectManager.get(id);
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
    }

    @SuppressWarnings("unchecked")
    private static int dumpWriter(MemorySegment address, MemorySegment p, long sz, MemorySegment ud) {
        int id = ud.reinterpret(ValueLayout.JAVA_INT.byteSize()).get(ValueLayout.JAVA_INT, 0);
        List<byte[]> chunks = (List<byte[]>)ObjectManager.get(id);
        MemorySegment chunk = p.reinterpret(sz);
        chunks.add(chunk.toArray(ValueLayout.JAVA_BYTE));
        return 0;
    }

    private static VarArg messageHandler(LuaThread thread, VarArg args) {
        try (Arena arena = Arena.ofConfined()) {
            LuaBindings.auxiliaryTraceback(thread.address, thread.address, arena.allocateFrom(args.checkString(0)), 1);
        }
        LuaValue value = LuaValue.from(thread, -1);
        LuaBindings.settop(thread.address, -2);
        return VarArg.of(value);
    }

    public LuaThread subThread() {
        MemorySegment threadAddress = LuaBindings.newthread(address);
        LuaBindings.settop(address, -2);
        LuaThread thread = new LuaThread(this, threadAddress);
        threads.add(thread);
        return thread;
    }

    public boolean isSubThread(LuaThread thread) {
        return thread == this || threads.contains(thread);
    }

    @Override
    public void close() {
        if (!isAlive()) {
            return;
        }
        LuaBindings.close(address);
        references.clear();
        isClosed = true;
    }
}

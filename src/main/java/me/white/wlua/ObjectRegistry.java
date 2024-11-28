package me.white.wlua;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.HashMap;
import java.util.Map;

class ObjectRegistry {
    private static final String METATABLE_NAME = "object_gc";
    private static final Map<Integer, Object> REGISTRY = new HashMap<>();
    private static int nextId = 1;

    static int register(Object object) {
        int id = nextId;
        nextId += 1;
        REGISTRY.put(id, object);
        return id;
    }

    static Object get(int id) {
        return REGISTRY.get(id);
    }

    static void remove(int id) {
        REGISTRY.remove(id);
    }

    static void pushObject(LuaThread thread, Object object) {
        try (Arena arena = Arena.ofConfined()) {
            int id = register(object);
            LuaBindings.newuserdatauv(thread.address, 0, 1);
            LuaBindings.newuserdatauv(thread.address, 0, 1);
            LuaBindings.pushinteger(thread.address, id);
            LuaBindings.setiuservalue(thread.address, -2, 1);
            if (LuaBindings.auxiliaryNewmetatable(thread.address, arena.allocateFrom(METATABLE_NAME)) == 1) {
                LuaBindings.pushvalue(thread.address, -2);
                LuaBindings.pushcclosure(thread.address, LuaState.GC_FUNCTION, 1);
                LuaBindings.setfield(thread.address, -2, arena.allocateFrom("__gc"));
            }
            LuaBindings.setmetatable(thread.address, -2);
            LuaBindings.setiuservalue(thread.address, -2, 1);
        }
    }

    static int from(LuaThread thread, int index) {
        LuaBindings.getiuservalue(thread.address, -1, 1);
        LuaBindings.getiuservalue(thread.address, -1, 1);
        int id = LuaBindings.tointegerx(thread.address, -1, MemorySegment.NULL);
        LuaBindings.settop(thread.address, -3);
        return id;
    }
}

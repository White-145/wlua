package me.white.wlua;

import java.lang.foreign.MemorySegment;
import java.util.HashMap;
import java.util.Map;

class ObjectManager {
    private static final Map<Integer, Object> objects = new HashMap<>();
    private static int nextId = 1;

    static int create(Object object) {
        int id = nextId;
        nextId += 1;
        objects.put(id, object);
        return id;
    }

    static Object get(int id) {
        return objects.get(id);
    }

    static void remove(int id) {
        objects.remove(id);
    }

    static int from(LuaThread thread, int index) {
        LuaBindings.getiuservalue(thread.address, index, 1);
        LuaBindings.getiuservalue(thread.address, -1, 1);
        int id = LuaBindings.tointegerx(thread.address, -1, MemorySegment.NULL);
        LuaBindings.settop(thread.address, -3);
        return id;
    }
}

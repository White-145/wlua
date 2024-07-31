package me.white.wlua;

import java.util.HashMap;
import java.util.Set;

public class TestMain {
    public static void main(String[] args) {
        try (LuaState state = new LuaState()) {
            testTable(state);
            assert LuaNatives.lua_gettop(state.ptr) == 0;
        }
    }

    private static void testTable(LuaState state) {
        LuaValue.TableRef table = new LuaValue.Table().ref(state);
        assert table.size() == 0;
        table.put(LuaValue.of("kind value"), LuaValue.of(true));
        table.put(LuaValue.of("evil value"), LuaValue.of(false));
        assert table.size() == 2;
        assert table.get(LuaValue.of("kind value")).equals(state, LuaValue.of(true));
        assert table.get(LuaValue.of("evil value")).equals(state, LuaValue.of(false));
        // setting a key to nil is just a fancy way of removing it
        table.put(LuaValue.of("evil value"), LuaValue.nil());
        assert table.size() == 1;

        HashMap<LuaValue, LuaValue> map = new HashMap<>();
        map.put(LuaValue.of(false), LuaValue.of(true));
        map.put(LuaValue.of(5), LuaValue.of("10"));
        table.putAll(map);
        assert table.size() == 3;
        assert table.get(LuaValue.of(5)).equals(state, LuaValue.of("10"));

        assert table.containsKey(LuaValue.of("kind value"));
        assert !table.containsKey(LuaValue.of("evil value"));
        assert table.containsValue(LuaValue.of(true));
        assert !table.containsValue(LuaValue.of(9.5));

        Set<LuaValue> keys = Set.of(LuaValue.of(false), LuaValue.of("kind value"), LuaValue.of(5));
        assert table.keySet().equals(keys);
        Set<LuaValue> values = Set.of(LuaValue.of(true), LuaValue.of("10"));
        assert table.values().equals(values);

        map = new HashMap<>();
        map.put(LuaValue.of(false), LuaValue.of(true));
        map.put(LuaValue.of("kind value"), LuaValue.of(true));
        map.put(LuaValue.of(5), LuaValue.of("10"));
        assert map.equals(table);

        table.clear();
        // cant trust .clear(), gotta make sure table size really is 0
        assert table.isEmpty();
        table.unref();
    }
}

package me.white.wlua;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TableValue extends LuaValue implements Map<LuaValue, LuaValue> {
    private Map<LuaValue, LuaValue> map;

    public TableValue() {
        this.map = new HashMap<>();
    }

    public TableValue(Map<LuaValue, LuaValue> map) {
        this.map = new HashMap<>(map);
    }

    public TableRefValue ref(LuaState state) {
        push(state);
        TableRefValue ref = new TableRefValue(state, -1);
        state.pop(1);
        return ref;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public LuaValue get(Object key) {
        return map.get(key);
    }

    @Override
    public LuaValue put(LuaValue key, LuaValue value) {
        return map.put(key, value);
    }

    @Override
    public LuaValue remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends LuaValue, ? extends LuaValue> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<LuaValue> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<LuaValue> values() {
        return map.values();
    }

    @Override
    public Set<Entry<LuaValue, LuaValue>> entrySet() {
        return map.entrySet();
    }

    @Override
    protected void push(LuaState state) {
        LuaNatives.lua_newtable(state.ptr);
        for (Entry<LuaValue, LuaValue> entry : entrySet()) {
            entry.getKey().push(state);
            entry.getValue().push(state);
            LuaNatives.lua_settable(state.ptr, -3);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Map<?, ?>)) {
            return false;
        }
        return map.equals(obj);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }
}

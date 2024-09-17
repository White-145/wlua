package me.white.wlua;

import java.util.*;

public non-sealed class TableLiteralValue extends LuaValue implements TableValue {
    protected final Map<LuaValue, LuaValue> map;

    public TableLiteralValue(Map<LuaValue, LuaValue> map) {
        this.map = new HashMap<>(map);
    }

    public TableLiteralValue() {
        this(new HashMap<>());
    }

    public TableRefValue toReference(LuaState state) {
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
        if (value instanceof NilValue) {
            return map.remove(key);
        }
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
    public final ValueType getType() {
        return ValueType.TABLE;
    }

    @Override
    final void push(LuaState state) {
        LuaNatives.newTable(state.ptr, size());
        for (Entry<LuaValue, LuaValue> entry : entrySet()) {
            state.pushValue(entry.getKey());
            state.pushValue(entry.getValue());
            LuaNatives.tableSet(state.ptr);
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

package me.white.wlua;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class TableLiteralValue extends LuaValue implements TableValue {
    private final Map<LuaValue, LuaValue> map;
    private final ListValue list;

    public TableLiteralValue(Map<LuaValue, LuaValue> map) {
        this.map = map;
        list = new ListLiteralValue(this);
    }

    public TableLiteralValue() {
        this(new HashMap<>());
    }

    public TableRefValue toReference(LuaState state) {
        state.checkIsAlive();
        state.pushValue(this);
        TableRefValue ref = new TableRefValue(state, -1);
        state.pop(1);
        return ref;
    }

    @Override
    public ListValue getList() {
        return null;
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
        if (LuaValue.isNil(value)) {
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
    public ValueType getType() {
        return ValueType.TABLE;
    }

    @Override
    void push(LuaState state) {
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

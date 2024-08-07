package me.white.wlua;

import java.util.*;

public class TableRefValue extends LuaValue.Ref implements Map<LuaValue, LuaValue> {
    protected TableRefValue(LuaState state, int index) {
        super(state, index);
    }

    public TableValue copy() {
        checkIsAlive();
        return new TableValue(this);
    }

    @Override
    public int size() {
        checkIsAlive();
        state.pushValue(this);
        return LuaNatives.tableSize(state.ptr);
    }

    @Override
    public boolean isEmpty() {
        checkIsAlive();
        state.pushValue(this);
        return LuaNatives.isTableEmpty(state.ptr);
    }

    @Override
    public boolean containsKey(Object key) {
        checkIsAlive();
        if (!(key instanceof LuaValue)) {
            return false;
        }
        state.pushValue(this);
        state.pushValue((LuaValue)key);
        return LuaNatives.tableContainsKey(state.ptr);
    }

    @Override
    public boolean containsValue(Object value) {
        checkIsAlive();
        if (!(value instanceof LuaValue)) {
            return false;
        }
        state.pushValue(this);
        state.pushValue((LuaValue)value);
        return LuaNatives.tableContainsValue(state.ptr);
    }

    @Override
    public LuaValue get(Object key) {
        checkIsAlive();
        if (!(key instanceof LuaValue)) {
            return null;
        }
        state.pushValue(this);
        state.pushValue((LuaValue)key);
        LuaNatives.tableGet(state.ptr);
        LuaValue returnValue = LuaValue.from(state, -1);
        state.pop(2);
        return returnValue;
    }

    @Override
    public LuaValue put(LuaValue key, LuaValue value) {
        checkIsAlive();
        state.pushValue(this);
        state.pushValue(key);
        LuaNatives.tableGet(state.ptr);
        LuaValue returnValue = LuaValue.from(state, -1);
        state.pop(1);
        state.pushValue(key);
        state.pushValue(value);
        LuaNatives.tableSet(state.ptr);
        state.pop(1);
        return returnValue instanceof NilValue ? null : returnValue;
    }

    @Override
    public LuaValue remove(Object key) {
        checkIsAlive();
        if (!(key instanceof LuaValue)) {
            return null;
        }
        return put((LuaValue)key, LuaValue.nil());
    }

    @Override
    public void putAll(Map<? extends LuaValue, ? extends LuaValue> m) {
        checkIsAlive();
        state.pushValue(this);
        for (Entry<? extends LuaValue, ? extends LuaValue> entry : m.entrySet()) {
            state.pushValue(entry.getKey());
            state.pushValue(entry.getValue());
            LuaNatives.tableSet(state.ptr);
        }
        state.pop(1);
    }

    @Override
    public void clear() {
        checkIsAlive();
        state.pushValue(this);
        LuaNatives.tableClear(state.ptr);
    }

    @Override
    public Set<LuaValue> keySet() {
        checkIsAlive();
        Set<LuaValue> keys = new HashSet<>();
        state.pushValue(this);
        state.pushValue(LuaValue.nil());
        while (LuaNatives.tableNext(state.ptr)) {
            state.pop(1);
            keys.add(LuaValue.from(state, -1));
        }
        state.pop(1);
        return keys;
    }

    @Override
    public Collection<LuaValue> values() {
        checkIsAlive();
        Set<LuaValue> values = new HashSet<>();
        state.pushValue(this);
        state.pushValue(LuaValue.nil());
        while (LuaNatives.tableNext(state.ptr)) {
            values.add(LuaValue.from(state, -1));
            state.pop(1);
        }
        state.pop(1);
        return values;
    }

    @Override
    public Set<Entry<LuaValue, LuaValue>> entrySet() {
        checkIsAlive();
        return new TableSet();
    }

    private class TableSet extends AbstractSet<Entry<LuaValue, LuaValue>> {
        @Override
        public Iterator<Entry<LuaValue, LuaValue>> iterator() {
            checkIsAlive();
            return new TableIterator();
        }

        @Override
        public int size() {
            checkIsAlive();
            return TableRefValue.this.size();
        }
    }

    private class TableIterator implements Iterator<Entry<LuaValue, LuaValue>> {
        private LuaValue key = LuaValue.nil();

        @Override
        public boolean hasNext() {
            checkIsAlive();
            state.pushValue(TableRefValue.this);
            state.pushValue(key);
            boolean hasNext = LuaNatives.tableNext(state.ptr);
            state.pop(hasNext ? 3 : 1);
            return hasNext;
        }

        @Override
        public Entry<LuaValue, LuaValue> next() {
            checkIsAlive();
            state.pushValue(TableRefValue.this);
            state.pushValue(key);
            if (!LuaNatives.tableNext(state.ptr)) {
                state.pop(1);
                throw new NoSuchElementException();
            }
            key = LuaValue.from(state, -2);
            LuaValue value = LuaValue.from(state, -1);
            state.pop(3);
            return new AbstractMap.SimpleEntry<>(key, value);
        }

        @Override
        public void remove() {
            checkIsAlive();
            if (key instanceof NilValue) {
                throw new IllegalStateException();
            }
            state.pushValue(TableRefValue.this);
            state.pushValue(key);
            LuaNatives.tableRemove(state.ptr);
            state.pop(1);
        }
    }
}

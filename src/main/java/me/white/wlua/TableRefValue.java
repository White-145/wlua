package me.white.wlua;

import java.util.*;

public final class TableRefValue extends LuaValue.Ref implements TableValue {
    private final ListRefValue list = new ListRefValue(this);

    TableRefValue(LuaState state, int reference) {
        super(state, reference);
    }

    public TableLiteralValue toLiteral() {
        checkIsAlive();
        Map<LuaValue, LuaValue> map = new HashMap<>();
        state.pushValue(this);
        state.pushNil();
        while (LuaNatives.tableNext(state.ptr)) {
            map.put(LuaValue.from(state, -2), LuaValue.from(state, -1));
            state.pop(1);
        }
        state.pop(1);
        return new TableLiteralValue(map);
    }

    @Override
    public ListValue getList() {
        return list;
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
        return LuaValue.isNil(returnValue) ? null : returnValue;
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
        return LuaValue.isNil(returnValue) ? null : returnValue;
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
        state.pushNil();
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
        state.pushNil();
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
        return new TableSet(this);
    }

    @Override
    public ValueType getType() {
        return ValueType.TABLE;
    }

    private static class TableSet extends AbstractSet<Entry<LuaValue, LuaValue>> {
        private final TableRefValue table;

        TableSet(TableRefValue table) {
            this.table = table;
        }

        @Override
        public Iterator<Entry<LuaValue, LuaValue>> iterator() {
            table.checkIsAlive();
            return new TableIterator(table);
        }

        @Override
        public int size() {
            table.checkIsAlive();
            return table.size();
        }
    }

    private static class TableIterator implements Iterator<Entry<LuaValue, LuaValue>> {
        private final TableRefValue table;
        private LuaValue key = LuaValue.nil();
        private boolean hasRemoved = false;

        TableIterator(TableRefValue table) {
            this.table = table;
        }

        private Entry<LuaValue, LuaValue> getNext() {
            table.checkIsAlive();
            table.state.pushValue(table);
            table.state.pushValue(key);
            boolean hasNext = LuaNatives.tableNext(table.state.ptr);
            if (!hasNext) {
                table.state.pop(1);
                return null;
            }
            LuaValue key = LuaValue.from(table.state, -2);
            LuaValue value = LuaValue.from(table.state, -1);
            table.state.pop(3);
            return new AbstractMap.SimpleEntry<>(key, value);
        }

        @Override
        public boolean hasNext() {
            return getNext() != null;
        }

        @Override
        public Entry<LuaValue, LuaValue> next() {
            Entry<LuaValue, LuaValue> entry = getNext();
            if (entry == null) {
                throw new NoSuchElementException();
            }
            key = entry.getKey();
            hasRemoved = false;
            return entry;
        }

        @Override
        public void remove() {
            if (hasRemoved) {
                throw new IllegalStateException();
            }
            table.remove(key);
            hasRemoved = true;
        }
    }
}

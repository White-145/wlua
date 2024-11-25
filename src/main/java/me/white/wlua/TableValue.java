package me.white.wlua;

import java.util.*;

public final class TableValue extends RefValue implements Map<LuaValue, LuaValue> {
    private final ListValue list = new ListValue(this);

    TableValue(LuaState state, int reference) {
        super(state, reference);
    }

    public Map<LuaValue, LuaValue> toMap() {
        checkIsAlive();
        Map<LuaValue, LuaValue> map = new HashMap<>();
        state.pushValue(this);
        LuaBindings.pushnil(state.address);
        while (LuaBindings.next(state.address, -2) == 1) {
            map.put(LuaValue.from(state, -2), LuaValue.from(state, -1));
            LuaBindings.settop(state.address, -2);
        }
        LuaBindings.settop(state.address, -2);
        return map;
    }

    public ListValue getList() {
        return list;
    }

    @Override
    public int size() {
        checkIsAlive();
        int size = 0;
        state.pushValue(this);
        LuaBindings.pushnil(state.address);
        while (LuaBindings.next(state.address, -2) == 1) {
            size += 1;
            LuaBindings.settop(state.address, -2);
        }
        LuaBindings.settop(state.address, -2);
        return size;
    }

    @Override
    public boolean isEmpty() {
        checkIsAlive();
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        checkIsAlive();
        if (LuaValue.isNil(key)) {
            return false;
        }
        state.pushValue(this);
        state.pushValue((LuaValue)key);
        boolean contains = LuaBindings.gettable(state.address, -2) != LuaBindings.TNIL;
        LuaBindings.settop(state.address, -3);
        return contains;
    }

    @Override
    public boolean containsValue(Object value) {
        checkIsAlive();
        if (LuaValue.isNil(value)) {
            return false;
        }
        state.pushValue(this);
        state.pushValue((LuaValue)value);
        LuaBindings.pushnil(state.address);
        while (LuaBindings.next(state.address, -3) == 1) {
            if (LuaBindings.compare(state.address, -3, -1, LuaBindings.OPEQ) == 1) {
                LuaBindings.settop(state.address, -5);
                return true;
            }
            LuaBindings.settop(state.address, -2);
        }
        LuaBindings.settop(state.address, -3);
        return false;
    }

    @Override
    public LuaValue get(Object key) {
        checkIsAlive();
        if (LuaValue.isNil(key)) {
            return null;
        }
        state.pushValue(this);
        state.pushValue((LuaValue)key);
        LuaBindings.gettable(state.address, -2);
        LuaValue returnValue = LuaValue.from(state, -1);
        LuaBindings.settop(state.address, -3);
        return returnValue.isNil() ? null : returnValue;
    }

    @Override
    public LuaValue put(LuaValue key, LuaValue value) {
        checkIsAlive();
        if (LuaValue.isNil(key)) {
            return null;
        }
        state.pushValue(this);
        state.pushValue(key);
        LuaBindings.gettable(state.address, -2);
        LuaValue returnValue = LuaValue.from(state, -1);
        state.pushValue(key);
        state.pushValue(value);
        LuaBindings.settable(state.address, -4);
        LuaBindings.settop(state.address, -3);
        return returnValue.isNil() ? null : returnValue;
    }

    @Override
    public LuaValue remove(Object key) {
        checkIsAlive();
        return put((LuaValue)key, LuaValue.nil());
    }

    @Override
    public void putAll(Map<? extends LuaValue, ? extends LuaValue> m) {
        checkIsAlive();
        state.pushValue(this);
        for (Entry<? extends LuaValue, ? extends LuaValue> entry : m.entrySet()) {
            if (LuaValue.isNil(entry.getKey())) {
                continue;
            }
            state.pushValue(entry.getKey());
            state.pushValue(entry.getValue());
            LuaBindings.settable(state.address, -3);
        }
        LuaBindings.settop(state.address, -2);
    }

    @Override
    public void clear() {
        checkIsAlive();
        state.pushValue(this);
        LuaBindings.pushnil(state.address);
        while (LuaBindings.next(state.address, -2) == 1) {
            LuaBindings.settop(state.address, -2);
            LuaBindings.pushvalue(state.address, -1);
            LuaBindings.pushnil(state.address);
            LuaBindings.settable(state.address, -4);
        }
        LuaBindings.settop(state.address, -2);
    }

    @Override
    public Set<LuaValue> keySet() {
        checkIsAlive();
        Set<LuaValue> keys = new HashSet<>();
        state.pushValue(this);
        LuaBindings.pushnil(state.address);
        while (LuaBindings.next(state.address, -2) == 1) {
            LuaBindings.settop(state.address, -2);
            keys.add(LuaValue.from(state, -1));
        }
        LuaBindings.settop(state.address, -2);
        return keys;
    }

    @Override
    public Collection<LuaValue> values() {
        checkIsAlive();
        Set<LuaValue> values = new HashSet<>();
        state.pushValue(this);
        LuaBindings.pushnil(state.address);
        while (LuaBindings.next(state.address, -2) == 1) {
            values.add(LuaValue.from(state, -1));
            LuaBindings.settop(state.address, -2);
        }
        LuaBindings.settop(state.address, -2);
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
        private final TableValue table;

        TableSet(TableValue table) {
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
        private final TableValue table;
        private final LuaState state;
        private LuaValue key = LuaValue.nil();
        private boolean hasRemoved = false;

        TableIterator(TableValue table) {
            this.table = table;
            state = table.state;
        }

        private Entry<LuaValue, LuaValue> getNext() {
            table.checkIsAlive();
            state.pushValue(table);
            state.pushValue(key);
            if (LuaBindings.next(state.address, -2) == 0) {
                LuaBindings.settop(state.address, -2);
                return null;
            }
            LuaValue key = LuaValue.from(state, -2);
            LuaValue value = LuaValue.from(state, -1);
            LuaBindings.settop(state.address, -4);
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

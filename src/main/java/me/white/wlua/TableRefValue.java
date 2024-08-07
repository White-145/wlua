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
        int length = 0;
        push(state);
        LuaValue.nil().push(state);
        while (LuaNatives.lua_next(state.ptr, -2) == 1) {
            length += 1;
            state.pop(1);
        }
        state.pop(1);
        return length;
    }

    @Override
    public boolean isEmpty() {
        checkIsAlive();
        push(state);
        LuaValue.nil().push(state);
        if (LuaNatives.lua_next(state.ptr, -2) == 1) {
            state.pop(3);
            return false;
        }
        state.pop(1);
        return true;
    }

    @Override
    public boolean containsKey(Object key) {
        checkIsAlive();
        if (!(key instanceof LuaValue)) {
            return false;
        }
        push(state);
        ((LuaValue)key).push(state);
        LuaNatives.lua_gettable(state.ptr, -2);
        boolean contains = LuaNatives.lua_isnil(state.ptr, -1) == 0;
        state.pop(2);
        return contains;
    }

    @Override
    public boolean containsValue(Object value) {
        checkIsAlive();
        if (!(value instanceof LuaValue)) {
            return false;
        }
        ((LuaValue)value).push(state);
        push(state);
        LuaValue.nil().push(state);
        while (LuaNatives.lua_next(state.ptr, -2) == 1) {
            if (LuaNatives.lua_compare(state.ptr, -4, -1, LuaConsts.OP_EQ) == 1) {
                state.pop(4);
                return true;
            }
            state.pop(1);
        }
        state.pop(2);
        return false;
    }

    @Override
    public LuaValue get(Object key) {
        checkIsAlive();
        if (!(key instanceof LuaValue)) {
            return null;
        }
        push(state);
        ((LuaValue)key).push(state);
        LuaNatives.lua_gettable(state.ptr, -2);
        LuaValue returnValue = LuaValue.from(state, -1);
        state.pop(2);
        return returnValue;
    }

    @Override
    public LuaValue put(LuaValue key, LuaValue value) {
        checkIsAlive();
        push(state);
        key.push(state);
        LuaNatives.lua_gettable(state.ptr, -2);
        LuaValue returnValue = LuaValue.from(state, -1);
        state.pop(1);
        key.push(state);
        value.push(state);
        LuaNatives.lua_settable(state.ptr, -3);
        state.pop(1);
        return returnValue;
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
        push(state);
        for (Entry<? extends LuaValue, ? extends LuaValue> entry : m.entrySet()) {
            entry.getKey().push(state);
            entry.getValue().push(state);
            LuaNatives.lua_settable(state.ptr, -3);
        }
        state.pop(1);
    }

    @Override
    public void clear() {
        checkIsAlive();
        push(state);
        LuaValue key = LuaValue.nil();
        key.push(state);
        while (LuaNatives.lua_next(state.ptr, -2) == 1) {
            state.pop(1);
            key = LuaValue.from(state, -1);
            LuaValue.nil().push(state);
            LuaNatives.lua_settable(state.ptr, -3);
            key.push(state);
        }
        state.pop(1);
    }

    @Override
    public Set<LuaValue> keySet() {
        checkIsAlive();
        Set<LuaValue> keys = new HashSet<>();
        push(state);
        LuaValue.nil().push(state);
        while (LuaNatives.lua_next(state.ptr, -2) == 1) {
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
        push(state);
        LuaValue.nil().push(state);
        while (LuaNatives.lua_next(state.ptr, -2) == 1) {
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
        LuaValue key = LuaValue.nil();

        @Override
        public boolean hasNext() {
            checkIsAlive();
            push(state);
            key.push(state);
            boolean hasNext = LuaNatives.lua_next(state.ptr, -2) == 1;
            state.pop(hasNext ? 3 : 1);
            return hasNext;
        }

        @Override
        public Entry<LuaValue, LuaValue> next() {
            checkIsAlive();
            push(state);
            key.push(state);
            if (LuaNatives.lua_next(state.ptr, -2) == 0) {
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
            push(state);
            key.push(state);
            LuaNatives.lua_pushnil(state.ptr);
            LuaNatives.lua_settable(state.ptr, -3);
            state.pop(1);
        }
    }
}

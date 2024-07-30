package me.white.wlua;

import java.util.*;

public class TableValue extends LuaValue.RefValue implements Map<LuaValue, LuaValue> {
    protected TableValue(LuaState state, int index) {
        super(state, index);
    }

    @Override
    public int size() {
        push(state);
        long length = LuaNatives.lua_rawlen(state.ptr, -1);
        LuaNatives.lua_pop(state.ptr, 1);
        return (int)length;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        if (!(key instanceof LuaValue)) {
            return false;
        }
        push(state);
        ((LuaValue)key).push(state);
        LuaNatives.lua_gettable(state.ptr, -2);
        boolean contains = LuaNatives.lua_isnil(state.ptr, -1) == 1;
        LuaNatives.lua_pop(state.ptr, 2);
        return contains;
    }

    @Override
    public boolean containsValue(Object value) {
        if (!(value instanceof LuaValue)) {
            return false;
        }
        ((LuaValue)value).push(state);
        push(state);
        LuaValue.nil().push(state);
        while (LuaNatives.lua_next(state.ptr, -2) == 1) {
            if (LuaNatives.lua_compare(state.ptr, -4, -1, LuaConsts.OP_EQ) == 1) {
                LuaNatives.lua_pop(state.ptr, 4);
                return true;
            }
            LuaNatives.lua_pop(state.ptr, 1);
        }
        LuaNatives.lua_pop(state.ptr, 3);
        return false;
    }

    @Override
    public LuaValue get(Object key) {
        if (!(key instanceof LuaValue)) {
            return null;
        }
        push(state);
        ((LuaValue)key).push(state);
        LuaNatives.lua_gettable(state.ptr, -2);
        LuaValue returnValue = LuaValue.from(state, -1);
        LuaNatives.lua_pop(state.ptr, 2);
        return returnValue;
    }

    @Override
    public LuaValue put(LuaValue key, LuaValue value) {
        push(state);
        key.push(state);
        LuaNatives.lua_gettable(state.ptr, -2);
        LuaValue returnValue = LuaValue.from(state, -1);
        LuaNatives.lua_pop(state.ptr, 1);
        key.push(state);
        value.push(state);
        LuaNatives.lua_settable(state.ptr, -3);
        LuaNatives.lua_pop(state.ptr, 1);
        return returnValue;
    }

    @Override
    public LuaValue remove(Object key) {
        if (!(key instanceof LuaValue)) {
            return null;
        }
        return put((LuaValue)key, LuaValue.nil());
    }

    @Override
    public void putAll(Map<? extends LuaValue, ? extends LuaValue> m) {
        push(state);
        for (Map.Entry<? extends LuaValue, ? extends LuaValue> entry : m.entrySet()) {
            entry.getKey().push(state);
            entry.getValue().push(state);
            LuaNatives.lua_settable(state.ptr, -3);
        }
        LuaNatives.lua_pop(state.ptr, 1);
    }

    @Override
    public void clear() {
        push(state);
        LuaValue key = LuaValue.nil();
        key.push(state);
        while (LuaNatives.lua_next(state.ptr, -2) == 1) {
            LuaNatives.lua_pop(state.ptr, 1);
            key = LuaValue.from(state, -1);
            LuaValue.nil().push(state);
            LuaNatives.lua_settable(state.ptr, -3);
            key.push(state);
        }
        LuaNatives.lua_pop(state.ptr, 1);
    }

    @Override
    public Set<LuaValue> keySet() {
        push(state);
        int size = (int)LuaNatives.lua_rawlen(state.ptr, -1);
        Set<LuaValue> keys = new HashSet<>(size);
        (LuaValue.nil()).push(state);
        while (LuaNatives.lua_next(state.ptr, -2) == 1) {
            LuaNatives.lua_pop(state.ptr, 1);
            keys.add(LuaValue.from(state, -1));
        }
        LuaNatives.lua_pop(state.ptr, 1);
        return keys;
    }

    @Override
    public Collection<LuaValue> values() {
        push(state);
        int size = (int)LuaNatives.lua_rawlen(state.ptr, -1);
        Set<LuaValue> values = new HashSet<>(size);
        (LuaValue.nil()).push(state);
        while (LuaNatives.lua_next(state.ptr, -2) == 1) {
            values.add(LuaValue.from(state, -1));
            LuaNatives.lua_pop(state.ptr, 1);
        }
        LuaNatives.lua_pop(state.ptr, 1);
        return values;
    }

    @Override
    public Set<Entry<LuaValue, LuaValue>> entrySet() {
        push(state);
        int size = (int)LuaNatives.lua_rawlen(state.ptr, -1);
        Set<Entry<LuaValue, LuaValue>> entries = new HashSet<>(size);
        (LuaValue.nil()).push(state);
        while (LuaNatives.lua_next(state.ptr, -2) == 1) {
            entries.add(new AbstractMap.SimpleEntry<>(LuaValue.from(state, -2), LuaValue.from(state, -1)));
            LuaNatives.lua_pop(state.ptr, 1);
        }
        LuaNatives.lua_pop(state.ptr, 1);
        return entries;
    }

    protected class AbstractLuaTableSet extends AbstractSet<Entry<LuaValue, LuaValue>> {
        @Override
        public Iterator<Entry<LuaValue, LuaValue>> iterator() {
            return new Iterator<>() {
                LuaValue key = LuaValue.nil();

                @Override
                public boolean hasNext() {
                    push(state);
                    key.push(state);
                    boolean hasNext = LuaNatives.lua_next(state.ptr, -2) == 1;
                    LuaNatives.lua_pop(state.ptr, hasNext ? 3 : 1);
                    return hasNext;
                }

                @Override
                public Entry<LuaValue, LuaValue> next() {
                    push(state);
                    key.push(state);
                    if (LuaNatives.lua_next(state.ptr, -2) == 0) {
                        LuaNatives.lua_pop(state.ptr, 1);
                        throw new NoSuchElementException();
                    }
                    key = LuaValue.from(state, -2);
                    LuaValue value = LuaValue.from(state, -1);
                    LuaNatives.lua_pop(state.ptr, 3);
                    return new AbstractMap.SimpleEntry<>(key, value);
                }

                @Override
                public void remove() {
                    if (key instanceof LuaValue.NilValue) {
                        throw new IllegalStateException();
                    }
                    push(state);
                    key.push(state);
                    LuaNatives.lua_pushnil(state.ptr);
                    LuaNatives.lua_settable(state.ptr, -3);
                    LuaNatives.lua_pop(state.ptr, 1);
                }
            };
        }

        @Override
        public int size() {
            return TableValue.this.size();
        }
    }
}

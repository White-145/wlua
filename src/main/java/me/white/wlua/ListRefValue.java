package me.white.wlua;

import java.util.*;

public final class ListRefValue extends LuaValue implements ListValue {
    private final TableRefValue table;
    private final LuaState state;

    ListRefValue(TableRefValue table) {
        this.table = table;
        this.state = table.state;
    }

    public ListLiteralValue toLiteral() {
        return (ListLiteralValue)table.toLiteral().getList();
    }

    @Override
    public TableValue getTable() {
        return table;
    }

    @Override
    public boolean isEmpty() {
        table.checkIsAlive();
        return !table.containsKey(LuaValue.ofIndex(0));
    }

    @Override
    public Object[] toArray() {
        table.checkIsAlive();
        List<LuaValue> values = new ArrayList<>();
        for (int i = 0; table.containsKey(LuaValue.ofIndex(i)); ++i) {
            values.add(table.get(LuaValue.ofIndex(i)));
        }
        return values.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        table.checkIsAlive();
        List<LuaValue> values = new ArrayList<>();
        for (int i = 0; table.containsKey(LuaValue.ofIndex(i)); ++i) {
            values.add(table.get(LuaValue.ofIndex(i)));
        }
        return values.toArray(a);
    }

    @Override
    public int size() {
        table.checkIsAlive();
        state.pushValue(this);
        long length = LuaNatives.length(state.ptr, -1);
        state.pop(1);
        return (int)length;
    }

    @Override
    public boolean contains(Object o) {
        table.checkIsAlive();
        if (!(o instanceof LuaValue)) {
            return false;
        }
        state.pushValue(this);
        state.pushValue((LuaValue)o);
        return LuaNatives.listContains(state.ptr);
    }

    @Override
    public Iterator<LuaValue> iterator() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(LuaValue value) {
        table.checkIsAlive();
        state.pushValue(this);
        state.pushValue(value);
        return LuaNatives.listAdd(state.ptr);
    }

    @Override
    public boolean remove(Object o) {
        table.checkIsAlive();
        if (!(o instanceof LuaValue)) {
            return false;
        }
        state.pushValue(this);
        state.pushValue((LuaValue)o);
        return LuaNatives.listRemove(state.ptr);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        table.checkIsAlive();
        state.pushValue(this);
        int amount = 0;
        for (Object o : c) {
            if (o instanceof LuaValue) {
                state.pushValue((LuaValue)o);
                amount += 1;
            }
        }
        return LuaNatives.listContainsAll(state.ptr, amount);
    }

    @Override
    public boolean addAll(Collection<? extends LuaValue> c) {
        table.checkIsAlive();
        state.pushValue(this);
        for (LuaValue value : c) {
            state.pushValue(value);
        }
        return LuaNatives.listAddAll(state.ptr, c.size());
    }

    @Override
    public boolean addAll(int index, Collection<? extends LuaValue> c) {
        table.checkIsAlive();
        state.pushValue(this);
        for (LuaValue value : c) {
            state.pushValue(value);
        }
        return LuaNatives.listAddAll(state.ptr, index, c.size());
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        table.checkIsAlive();
        state.pushValue(this);
        int amount = 0;
        for (Object o : c) {
            if (o instanceof LuaValue) {
                state.pushValue((LuaValue)o);
                amount += 1;
            }
        }
        return LuaNatives.listRemoveAll(state.ptr, amount);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        table.checkIsAlive();
        state.pushValue(this);
        int amount = 0;
        for (Object o : c) {
            if (o instanceof LuaValue) {
                state.pushValue((LuaValue)o);
                amount += 1;
            }
        }
        return LuaNatives.listRetainAll(state.ptr, amount);
    }

    @Override
    public void clear() {
        table.checkIsAlive();
        state.pushValue(this);
        LuaNatives.listClear(state.ptr);
    }

    @Override
    public LuaValue get(int index) {
        table.checkIsAlive();
        state.pushValue(this);
        LuaNatives.listGet(state.ptr, index);
        LuaValue value = LuaValue.from(state, -1);
        state.pop(1);
        return value.isNil() ? null : value;
    }

    @Override
    public LuaValue set(int index, LuaValue element) {
        table.checkIsAlive();
        state.pushValue(this);
        state.pushValue(element);
        LuaNatives.listSet(state.ptr, index);
        LuaValue value = LuaValue.from(state, -1);
        state.pop(1);
        return value.isNil() ? null : value;
    }

    @Override
    public void add(int index, LuaValue element) {
        table.checkIsAlive();
        state.pushValue(this);
        state.pushValue(element);
        LuaNatives.listAddIndex(state.ptr, index);
    }

    @Override
    public LuaValue remove(int index) {
        table.checkIsAlive();
        state.pushValue(this);
        LuaNatives.listRemoveIndex(state.ptr, index);
        LuaValue value = LuaValue.from(state, -1);
        state.pop(1);
        return value.isNil() ? null : value;
    }

    @Override
    public int indexOf(Object o) {
        table.checkIsAlive();
        if (!(o instanceof LuaValue)) {
            return -1;
        }
        state.pushValue(this);
        state.pushValue((LuaValue)o);
        return LuaNatives.listIndexOf(state.ptr);
    }

    @Override
    public int lastIndexOf(Object o) {
        table.checkIsAlive();
        if (!(o instanceof LuaValue)) {
            return -1;
        }
        state.pushValue(this);
        state.pushValue((LuaValue)o);
        return LuaNatives.listLastIndexOf(state.ptr);
    }

    @Override
    public ListIterator<LuaValue> listIterator() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<LuaValue> listIterator(int index) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public ValueType getType() {
        return ValueType.TABLE;
    }

    @Override
    void push(LuaState state) {
        table.push(state);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ListRefValue)) {
            return false;
        }
        return table.equals(((ListRefValue)obj).table);
    }

    @Override
    public int hashCode() {
        return table.hashCode();
    }
}

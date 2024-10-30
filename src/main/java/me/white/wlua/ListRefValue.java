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
        return size() == 0;
    }

    @Override
    public Object[] toArray() {
        table.checkIsAlive();
        int size = size();
        Object[] values = new Object[size];
        for (int i = 0; i < size; ++i) {
            values[i] = table.get(LuaValue.index(i));
        }
        return values;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        table.checkIsAlive();
        int size = size();
        if (a.length < size) {
            return (T[])Arrays.copyOf(toArray(), size, a.getClass());
        }
        for (int i = 0; i < size; ++i) {
            a[i] = (T)table.get(LuaValue.index(i));
        }
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }

    @Override
    public int size() {
        table.checkIsAlive();
        state.pushValue(this);
        return LuaNatives.listSize(state.ptr);
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
        return new ListLiteralValue.LuaListIterator(this);
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
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends LuaValue> c) {
        table.checkIsAlive();
        for (LuaValue value : c) {
            if (!LuaValue.isNil(value)) {
                add(value);
            }
        }
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends LuaValue> c) {
        table.checkIsAlive();
        int size = size();
        if (index < 0 || index >= size) {
            return false;
        }
        for (LuaValue value : c) {
            if (!LuaValue.isNil(value)) {
                add(index, value);
                index += 1;
            }
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        table.checkIsAlive();
        boolean hasChanged = false;
        for (Object o : c) {
            if (!LuaValue.isNil(o)) {
                state.pushValue(this);
                state.pushValue((LuaValue)o);
                boolean bl = LuaNatives.listRemoveEvery(state.ptr);
                hasChanged = hasChanged || bl;
            }
        }
        return hasChanged;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        table.checkIsAlive();
        Set<LuaValue> values = new HashSet<>();
        for (Object value : c) {
            if (!LuaValue.isNil(value)) {
                values.add((LuaValue)value);
            }
        }
        if (values.isEmpty()) {
            return false;
        }
        boolean hasChanged = false;
        int size = size();
        for (int i = 0; i < size; ++i) {
            LuaValue value = table.get(LuaValue.index(i));
            if (!values.contains(value)) {
                table.remove(LuaValue.index(i));
                hasChanged = true;
            }
        }
        state.pushValue(this);
        LuaNatives.listCollapse(state.ptr, size, 1);
        return hasChanged;
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
        state.pop(2);
        return value.isNil() ? null : value;
    }

    @Override
    public LuaValue set(int index, LuaValue element) {
        table.checkIsAlive();
        state.pushValue(this);
        LuaNatives.listGet(state.ptr, index);
        LuaValue value = LuaValue.from(state, -1);
        state.pop(1);
        state.pushValue(element);
        LuaNatives.listSet(state.ptr, index);
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
        return new ListLiteralValue.LuaListListIterator(this, 0);
    }

    @Override
    public ListIterator<LuaValue> listIterator(int index) {
        return new ListLiteralValue.LuaListListIterator(this, index);
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

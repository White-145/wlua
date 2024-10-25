package me.white.wlua;

import java.util.*;

public final class ListLiteralValue extends LuaValue implements ListValue {
    private final TableLiteralValue table;
    // TODO add throws as javadoc says

    ListLiteralValue(TableLiteralValue table) {
        this.table = table;
    }

    public ListRefValue toReference(LuaState state) {
        state.checkIsAlive();
        state.pushValue(table);
        TableRefValue ref = state.getReference(-1, TableRefValue::new);
        state.pop(1);
        return (ListRefValue)ref.getList();
    }

    private void shift(int size, int index, int amount) {
        for (int i = 0; i < size - index; ++i) {
            int j = size - i - 1;
            table.put(LuaValue.index(j + amount), table.get(LuaValue.index(j)));
        }
    }

    private void collapse(int size, int from) {
        int lastI = from;
        for (int i = from; i < size; ++i) {
            LuaValue value = table.get(LuaValue.index(i));
            if (!LuaValue.isNil(value)) {
                if (lastI != i) {
                    table.put(LuaValue.index(lastI), value);
                }
                lastI += 1;
            }
        }
        for (int i = lastI; i < size; ++i) {
            table.remove(LuaValue.index(i));
        }
    }

    private boolean removeEvery(LuaValue value) {
        int size = size();
        boolean hasChanged = false;
        for (int i = 0; i < size; ++i) {
            if (table.get(LuaValue.index(i)).equals(value)) {
                table.remove(LuaValue.index(i));
                hasChanged = true;
            }
        }
        if (hasChanged) {
            collapse(size, 0);
        }
        return hasChanged;
    }

    @Override
    public TableValue getTable() {
        return table;
    }

    @Override
    public boolean isEmpty() {
        return !table.containsKey(LuaValue.index(0));
    }

    @Override
    public Object[] toArray() {
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
        int i = 0;
        while (table.containsKey(LuaValue.index(i))) {
            i += 1;
        }
        return i;
    }

    @Override
    public boolean contains(Object o) {
        if (LuaValue.isNil(o)) {
            return false;
        }
        int size = size();
        for (int i = 0; i < size; ++i) {
            if (table.get(LuaValue.index(i)).equals(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<LuaValue> iterator() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(LuaValue value) {
        table.put(LuaValue.index(size()), value);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        if (!(o instanceof LuaValue)) {
            return false;
        }
        int size = size();
        for (int i = 0; i < size; ++i) {
            if (table.get(LuaValue.index(i)).equals(o)) {
                table.remove(LuaValue.index(i));
                collapse(size, i);
                return true;
            }
        }
        return false;
    }

    @Override
    public void clear() {
        int size = size();
        for (int i = 0; i < size; ++i) {
            table.remove(LuaValue.index(i));
        }
    }

    @Override
    public LuaValue get(int index) {
        if (index >= 0 && index < size()) {
            return table.get(LuaValue.index(index));
        }
        return null;
    }

    @Override
    public LuaValue set(int index, LuaValue element) {
        if (index >= 0 && index < size()) {
            return table.put(LuaValue.index(index), element);
        }
        return null;
    }

    @Override
    public void add(int index, LuaValue element) {
        int size = size();
        if (index < 0 || index >= size) {
            return;
        }
        shift(size, index, 1);
        table.put(LuaValue.index(index), element);
    }

    @Override
    public LuaValue remove(int index) {
        int size = size();
        if (index < 0 || index >= size) {
            return null;
        }
        LuaValue value = table.remove(LuaValue.index(index));
        collapse(size, index);
        return value;
    }

    @Override
    public int indexOf(Object o) {
        if (!(o instanceof LuaValue)) {
            return -1;
        }
        int size = size();
        for (int i = 0; i < size; ++i) {
            if (table.get(LuaValue.index(i)).equals(o)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        if (!(o instanceof LuaValue)) {
            return -1;
        }
        int size = size();
        for (int i = size - 1; i >= 0; --i) {
            if (table.get(LuaValue.index(i)).equals(o)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends LuaValue> c) {
        boolean hasChanged = false;
        for (LuaValue value : c) {
            if (!LuaValue.isNil(value)) {
                add(value);
            }
        }
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends LuaValue> c) {
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
        boolean hasChanged = false;
        for (Object o : c) {
            if (!LuaValue.isNil(o)) {
                boolean bl = removeEvery((LuaValue)o);
                hasChanged = hasChanged || bl;
            }
        }
        return hasChanged;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
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
            if (!values.contains(table.get(LuaValue.index(i)))) {
                table.remove(LuaValue.index(i));
                hasChanged = true;
            }
        }
        collapse(size, 0);
        return hasChanged;
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
        if (!(obj instanceof List<?>)) {
            return false;
        }
        return obj.equals(this);
    }

    @Override
    public int hashCode() {
        return table.hashCode();
    }
}

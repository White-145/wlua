package me.white.wlua;

import java.util.*;

public final class ListValue extends LuaValue implements List<LuaValue> {
    private final TableValue table;
    private final LuaState state;

    ListValue(TableValue table) {
        this.table = table;
        state = table.state;
    }

    private void shift(int index, int size, int from) {
        if (size == from) {
            return;
        }
        int absindex = LuaBindings.absindex(state.address, index);
        for (int i = 0; i <= size - from; ++i) {
            int j = size - i;
            LuaBindings.rawgeti(state.address, absindex, j);
            LuaBindings.rawseti(state.address, absindex, j + 1);
        }
    }

    private void collapse(int index, int size, int from) {
        int absindex = LuaBindings.absindex(state.address, index);
        int lastI = from;
        for (int i = from; i < size; ++i) {
            if (LuaBindings.rawgeti(state.address, absindex, i + 1) != LuaBindings.TNIL) {
                if (i != lastI) {
                    LuaBindings.rawseti(state.address, absindex, lastI + 1);
                } else {
                    LuaBindings.settop(state.address, -2);
                }
                lastI += 1;
            } else {
                LuaBindings.settop(state.address, -2);
            }
        }
        for (int i = lastI; i < size; ++i) {
            LuaBindings.pushnil(state.address);
            LuaBindings.rawseti(state.address, absindex, i + 1);
        }
    }

    private void checkAddRange(int index, int size) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    private void checkNil(LuaValue value) {
        if (LuaValue.isNil(value)) {
            throw new IllegalArgumentException("Could not set nil value to the list.");
        }
    }

    public TableValue getTable() {
        return table;
    }

    public List<LuaValue> toList() {
        table.checkIsAlive();
        int size = size();
        List<LuaValue> list = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            list.add(get(i));
        }
        return list;
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
        LuaValue[] values = new LuaValue[size];
        state.pushValue(this);
        for (int i = 0; i < size; ++i) {
            LuaBindings.geti(state.address, -1, i + 1);
            values[i] = LuaValue.from(state, -1);
            LuaBindings.settop(state.address, -2);
        }
        LuaBindings.settop(state.address, -2);
        return values;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        table.checkIsAlive();
        int size = size();
        LuaValue[] array = (LuaValue[])toArray();
        if (a.length < size) {
            return (T[])Arrays.copyOf(array, size, a.getClass());
        }
        System.arraycopy(array, 0, a, 0, size);
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }

    @Override
    public int size() {
        table.checkIsAlive();
        state.pushValue(this);
        int size = 0;
        while (LuaBindings.rawgeti(state.address, -1, size + 1) != LuaBindings.TNIL) {
            LuaBindings.settop(state.address, -2);
            size += 1;
        }
        LuaBindings.settop(state.address, -3);
        return size;
    }

    @Override
    public boolean contains(Object o) {
        table.checkIsAlive();
        if (LuaValue.isNil(o)) {
            return false;
        }
        state.pushValue(this);
        state.pushValue((LuaValue)o);
        int size = size();
        for (int i = 0; i < size; ++i) {
            LuaBindings.rawgeti(state.address, -2, i + 1);
            if (LuaBindings.compare(state.address, -2, -1, LuaBindings.OPEQ) == 1) {
                LuaBindings.settop(state.address, -4);
                return true;
            }
            LuaBindings.settop(state.address, -2);
        }
        LuaBindings.settop(state.address, -3);
        return false;
    }

    @Override
    public Iterator<LuaValue> iterator() {
        table.checkIsAlive();
        return new LuaListIterator(this, 0);
    }

    @Override
    public boolean add(LuaValue value) {
        table.checkIsAlive();
        checkNil(value);
        state.pushValue(this);
        state.pushValue(value);
        LuaBindings.rawseti(state.address, -2, size() + 1);
        LuaBindings.settop(state.address, -2);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        table.checkIsAlive();
        if (LuaValue.isNil(o)) {
            return false;
        }
        int size = size();
        state.pushValue(this);
        state.pushValue((LuaValue)o);
        boolean hasChanged = false;
        for (int i = 0; i < size; ++i) {
            LuaBindings.rawgeti(state.address, -2, i + 1);
            if (LuaBindings.compare(state.address, -2, -1, LuaBindings.OPEQ) == 1) {
                LuaBindings.settop(state.address, -3);
                LuaBindings.pushnil(state.address);
                LuaBindings.rawseti(state.address, -2, i + 1);
                hasChanged = true;
                break;
            }
            LuaBindings.settop(state.address, -2);
        }
        if (!hasChanged) {
            LuaBindings.settop(state.address, -3);
            return false;
        }
        collapse(-1, size, 0);
        LuaBindings.settop(state.address, -2);
        return true;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        table.checkIsAlive();
        Objects.requireNonNull(c);
        LuaBindings.createtable(state.address, 0, 0);
        int size = size();
        state.pushValue(this);
        for (int i = 0; i < size; ++i) {
            LuaBindings.rawgeti(state.address, -1, i + 1);
            LuaBindings.pushboolean(state.address, 1);
            LuaBindings.rawset(state.address, -4);
        }
        for (Object o : c) {
            if (LuaValue.isNil(o)) {
                LuaBindings.settop(state.address, -3);
                return false;
            }
            state.pushValue((LuaValue)o);
            if (LuaBindings.rawget(state.address, -3) != LuaBindings.TBOOLEAN) {
                LuaBindings.settop(state.address, -4);
                return false;
            }
            LuaBindings.settop(state.address, -2);
        }
        LuaBindings.settop(state.address, -3);
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends LuaValue> c) {
        table.checkIsAlive();
        Objects.requireNonNull(c);
        for (LuaValue value : c) {
            add(value);
        }
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends LuaValue> c) {
        table.checkIsAlive();
        Objects.requireNonNull(c);
        checkAddRange(index, size());
        for (LuaValue value : c) {
            // NOTE have to wrap `add` method because of possible dangling values after the lists end
            add(index, value);
            index += 1;
        }
        return !c.isEmpty();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        table.checkIsAlive();
        Objects.requireNonNull(c);
        LuaBindings.createtable(state.address, 0, c.size());
        for (Object o : c) {
            if (LuaValue.isNil(o)) {
                continue;
            }
            state.pushValue((LuaValue)o);
            LuaBindings.pushboolean(state.address, 1);
            LuaBindings.rawset(state.address, -3);
        }
        boolean hasChanged = false;
        int size = size();
        state.pushValue(this);
        for (int i = 0; i < size; ++i) {
            LuaBindings.rawgeti(state.address, -1, i + 1);
            if (LuaBindings.rawget(state.address, -3) == LuaBindings.TBOOLEAN) {
                LuaBindings.pushnil(state.address);
                LuaBindings.rawseti(state.address, -3, i + 1);
                hasChanged = true;
            }
            LuaBindings.settop(state.address, -2);
        }
        if (hasChanged) {
            collapse(-1, size, 0);
        }
        LuaBindings.settop(state.address, -3);
        return hasChanged;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        table.checkIsAlive();
        Objects.requireNonNull(c);
        LuaBindings.createtable(state.address, 0, 0);
        for (Object o : c) {
            if (LuaValue.isNil(o)) {
                continue;
            }
            state.pushValue((LuaValue)o);
            LuaBindings.pushboolean(state.address, 1);
            LuaBindings.rawset(state.address, -3);
        }
        boolean hasChanged = false;
        int size = size();
        state.pushValue(this);
        for (int i = 0; i < size; ++i) {
            LuaBindings.rawgeti(state.address, -1, i + 1);
            if (LuaBindings.rawget(state.address, -3) != LuaBindings.TBOOLEAN) {
                LuaBindings.pushnil(state.address);
                LuaBindings.rawseti(state.address, -3, i + 1);
                hasChanged = true;
            }
            LuaBindings.settop(state.address, -2);
        }
        if (hasChanged) {
            collapse(-1, size, 0);
        }
        LuaBindings.settop(state.address, -3);
        return hasChanged;
    }

    @Override
    public void clear() {
        table.checkIsAlive();
        int size = size();
        state.pushValue(this);
        for (int i = 0; i < size; ++i) {
            LuaBindings.pushnil(state.address);
            LuaBindings.rawseti(state.address, -2, i + 1);
        }
        LuaBindings.settop(state.address, -2);
    }

    @Override
    public LuaValue get(int index) {
        table.checkIsAlive();
        Objects.checkIndex(index, size());
        state.pushValue(this);
        LuaBindings.rawgeti(state.address, -1, index + 1);
        LuaValue value = LuaValue.from(state, -1);
        LuaBindings.settop(state.address, -3);
        return LuaValue.orNull(value);
    }

    @Override
    public LuaValue set(int index, LuaValue element) {
        table.checkIsAlive();
        checkNil(element);
        Objects.checkIndex(index, size());
        state.pushValue(this);
        LuaBindings.rawgeti(state.address, -1, index + 1);
        LuaValue value = LuaValue.from(state, -1);
        state.pushValue(element);
        LuaBindings.rawseti(state.address, -3, index + 1);
        LuaBindings.settop(state.address, -3);
        return LuaValue.orNull(value);
    }

    @Override
    public void add(int index, LuaValue element) {
        table.checkIsAlive();
        checkNil(element);
        int size = size();
        checkAddRange(index, size);
        state.pushValue(this);
        state.pushValue(element);
        shift(-2, size, index);
        LuaBindings.rawseti(state.address, -2, index + 1);
        LuaBindings.settop(state.address, -2);
    }

    @Override
    public LuaValue remove(int index) {
        table.checkIsAlive();
        int size = size();
        Objects.checkIndex(index, size);
        state.pushValue(this);
        LuaBindings.rawgeti(state.address, -1, index + 1);
        LuaValue value = LuaValue.from(state, -1);
        LuaBindings.pushnil(state.address);
        LuaBindings.rawseti(state.address, -3, index + 1);
        collapse(-2, size, index);
        LuaBindings.settop(state.address, -3);
        return LuaValue.orNull(value);
    }

    @Override
    public int indexOf(Object o) {
        table.checkIsAlive();
        if (LuaValue.isNil(o)) {
            return -1;
        }
        int size = size();
        state.pushValue(this);
        state.pushValue((LuaValue)o);
        for (int i = 0; i < size; ++i) {
            LuaBindings.rawgeti(state.address, -2, i + 1);
            if (LuaBindings.compare(state.address, -2, -1, LuaBindings.OPEQ) == 1) {
                LuaBindings.settop(state.address, -4);
                return i;
            }
            LuaBindings.settop(state.address, -2);
        }
        LuaBindings.settop(state.address, -3);
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        table.checkIsAlive();
        if (!(o instanceof LuaValue)) {
            return -1;
        }
        int size = size();
        state.pushValue(this);
        state.pushValue((LuaValue)o);
        for (int i = size - 1; i >= 0; --i) {
            LuaBindings.rawgeti(state.address, -2, i + 1);
            if (LuaBindings.compare(state.address, -2, -1, LuaBindings.OPEQ) == 1) {
                LuaBindings.settop(state.address, -4);
                return i;
            }
            LuaBindings.settop(state.address, -2);
        }
        LuaBindings.settop(state.address, -3);
        return -1;
    }

    @Override
    public ListIterator<LuaValue> listIterator() {
        return new LuaListIterator(this, 0);
    }

    @Override
    public ListIterator<LuaValue> listIterator(int index) {
        return new LuaListIterator(this, index);
    }

    // NOTE no clue how to implement this
    @Override
    public List<LuaValue> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    void push(LuaThread thread) {
        table.push(thread);
    }

    @Override
    public ValueType getType() {
        return ValueType.TABLE;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ListValue)) {
            return false;
        }
        return table.equals(((ListValue)obj).table);
    }

    @Override
    public int hashCode() {
        return table.hashCode();
    }

    static class LuaListIterator implements ListIterator<LuaValue> {
        private final ListValue list;
        private boolean hasRemoved = false;
        private int lastIndex = -1;
        private int index;

        LuaListIterator(ListValue list, int index) {
            this.list = list;
            this.index = index;
        }

        @Override
        public boolean hasNext() {
            return index >= 0 && index < list.size();
        }

        @Override
        public LuaValue next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            hasRemoved = false;
            lastIndex = index;
            index += 1;
            return list.get(lastIndex);
        }

        @Override
        public boolean hasPrevious() {
            return index > 0 && index <= list.size();
        }

        @Override
        public LuaValue previous() {
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }
            hasRemoved = false;
            lastIndex = index;
            index -= 1;
            return list.get(index);
        }

        @Override
        public int nextIndex() {
            return index;
        }

        @Override
        public int previousIndex() {
            return index - 1;
        }

        @Override
        public void remove() {
            if (hasRemoved) {
                throw new IllegalStateException();
            }
            index = lastIndex;
            list.remove(index);
            hasRemoved = true;
        }

        @Override
        public void set(LuaValue value) {
            list.set(lastIndex, value);
        }

        @Override
        public void add(LuaValue value) {
            list.add(index, value);
            index += 1;
        }
    }
}

package me.white.wlua;

import java.util.*;

public final class ListLiteralValue extends LuaValue implements ListValue {
    private final TableLiteralValue table;

    public ListLiteralValue(TableLiteralValue table) {
        this.table = table;
    }

    public ListLiteralValue(Map<LuaValue, LuaValue> map) {
        this(new TableLiteralValue(map));
    }

    public ListLiteralValue(List<LuaValue> list) {
        this(transform(list));
    }

    public ListLiteralValue() {
        this(new HashMap<>());
    }

    private static Map<LuaValue, LuaValue> transform(List<LuaValue> list) {
        Map<LuaValue, LuaValue> map = new HashMap<>();
        for (int i = 0; i < list.size(); ++i) {
            map.put(LuaValue.ofIndex(i), list.get(i));
        }
        return map;
    }

    public ListRefValue toReference(LuaState state) {
        state.checkIsAlive();
        state.pushValue(this);
        LuaValue ref = LuaValue.from(state, -1);
        state.pop(1);
        return new ListRefValue((TableRefValue)ref);
    }

    @Override
    public TableValue getTable() {
        return table;
    }

    @Override
    public boolean isEmpty() {
        return !table.containsKey(LuaValue.ofIndex(0));
    }

    @Override
    public Object[] toArray() {
        List<LuaValue> values = new ArrayList<>();
        for (int i = 0; table.containsKey(LuaValue.ofIndex(i)); ++i) {
            values.add(table.get(LuaValue.ofIndex(i)));
        }
        return values.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        List<LuaValue> values = new ArrayList<>();
        for (int i = 0; table.containsKey(LuaValue.ofIndex(i)); ++i) {
            values.add(table.get(LuaValue.ofIndex(i)));
        }
        return values.toArray(a);
    }

    @Override
    public int size() {
        int i = 0;
        while (table.containsKey(LuaValue.ofIndex(i))) {
            i += 1;
        }
        return i;
    }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof LuaValue)) {
            return false;
        }
        for (int i = 0; table.containsKey(LuaValue.ofIndex(i)); ++i) {
            if (table.get(LuaValue.ofIndex(i)).equals(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<LuaValue> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(LuaValue value) {
        int i = 0;
        while (table.containsKey(LuaValue.ofIndex(i))) {
            i += 1;
        }
        table.put(LuaValue.ofIndex(i), value);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        if (!(o instanceof LuaValue)) {
            return false;
        }
        int i = 0;
        while (table.containsKey(LuaValue.ofIndex(i))) {
            if (table.get(LuaValue.ofIndex(i)).equals(o)) {
                break;
            }
            i += 1;
        }
        while (table.containsKey(LuaValue.ofIndex(i + 1))) {
            table.put(LuaValue.ofIndex(i), table.get(LuaValue.ofIndex(i + 1)));
            i += 1;
        }
        table.remove(LuaValue.ofIndex(i));
        return true;
    }

    @Override
    public void clear() {
        for (int i = 0; table.containsKey(LuaValue.ofIndex(i)); ++i) {
            table.remove(LuaValue.ofIndex(i));
        }
    }

    @Override
    public LuaValue get(int index) {
        for (int i = 0; table.containsKey(LuaValue.ofIndex(i)); ++i) {
            if (i == index) {
                return table.get(LuaValue.ofIndex(index));
            }
        }
        return null;
    }

    @Override
    public LuaValue set(int index, LuaValue element) {
        for (int i = 0; table.containsKey(LuaValue.ofIndex(i)); ++i) {
            if (i == index) {
                return table.put(LuaValue.ofIndex(index), element);
            }
        }
        return null;
    }

    @Override
    public void add(int index, LuaValue element) {
        int i = 0;
        while (table.containsKey(LuaValue.ofIndex(i))) {
            if (i == index) {
                break;
            }
            i += 1;
        }
        LuaValue carry = table.put(LuaValue.ofIndex(i), element);
        while (carry != null && !carry.isNil()) {
            carry = table.put(LuaValue.ofIndex(i), carry);
            i += 1;
        }
    }

    @Override
    public LuaValue remove(int index) {
        int i = 0;
        while (table.containsKey(LuaValue.ofIndex(i))) {
            if (i == index) {
                break;
            }
            i += 1;
        }
        if (i != index) {
            return null;
        }
        LuaValue value = table.remove(LuaValue.ofIndex(i));
        while (table.containsKey(LuaValue.ofIndex(i + 1))) {
            table.put(LuaValue.ofIndex(i), table.get(LuaValue.ofIndex(i + 1)));
            i += 1;
        }
        table.remove(LuaValue.ofIndex(i));
        return value;
    }

    @Override
    public int indexOf(Object o) {
        if (!(o instanceof LuaValue)) {
            return -1;
        }
        int i = 0;
        while (table.containsKey(LuaValue.ofIndex(i))) {
            if (table.get(LuaValue.ofIndex(i)).equals(o)) {
                return i;
            }
            i += 1;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        if (!(o instanceof LuaValue)) {
            return -1;
        }
        int result = -1;
        int i = 0;
        while (table.containsKey(LuaValue.ofIndex(i))) {
            if (table.get(LuaValue.ofIndex(i)).equals(o)) {
                result = i;
            }
            i += 1;
        }
        return result;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        if (c.isEmpty()) {
            return true;
        }
        Set<LuaValue> set = new HashSet<>();
        for (Object o : c) {
            if (!(o instanceof LuaValue)) {
                return false;
            }
            set.add((LuaValue)o);
        }
        int i = 0;
        while (table.containsKey(LuaValue.ofIndex(i))) {
            LuaValue value = table.get(LuaValue.ofIndex(i));
            set.remove(value);
            if (set.isEmpty()) {
                return true;
            }
            i += 1;
        }
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends LuaValue> c) {
        if (c.isEmpty()) {
            return false;
        }
        boolean hasChanged = false;
        int i = size();
        for (LuaValue value : c) {
            if (value != null && !value.isNil()) {
                table.put(LuaValue.ofIndex(i), value);
                hasChanged = true;
                i += 1;
            }
        }
        return hasChanged;
    }

    @Override
    public boolean addAll(int index, Collection<? extends LuaValue> c) {
        List<LuaValue> values = new ArrayList<>(c.size());
        for (LuaValue value : c) {
            if (value != null && !value.isNil()) {
                values.add(value);
            }
        }
        int i = 0;
        while (table.containsKey(LuaValue.ofIndex(i))) {
            if (i == index) {
                break;
            }
            i += 1;
        }
        if (i != index) {
            for (LuaValue value : values) {
                table.put(LuaValue.ofIndex(i), value);
                i += 1;
            }
            return true;
        }
        while (table.containsKey(LuaValue.ofIndex(i))) {
            i += 1;
        }
        for (int j = i - 1; j >= index; --j) {
            table.put(LuaValue.ofIndex(j + values.size()), table.get(LuaValue.ofIndex(j)));
        }
        for (LuaValue value : values) {
            table.put(LuaValue.ofIndex(index), value);
            index += 1;
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean hasChanged = false;
        int i = 0;
        while (table.containsKey(LuaValue.ofIndex(i))) {
            if (c.contains(table.get(LuaValue.ofIndex(i)))) {
                table.remove(LuaValue.ofIndex(i));
                hasChanged = true;
            }
            i += 1;
        }
        if (!hasChanged) {
            return false;
        }
        int lastI = 0;
        for (int j = 0; j < i; ++j) {
            LuaValue value = table.get(LuaValue.ofIndex(j));
            if (value != null && !value.isNil()) {
                if (lastI != j) {
                    table.put(LuaValue.ofIndex(lastI), value);
                }
                lastI += 1;
            }
        }
        return true;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        if (table.isEmpty()) {
            return false;
        }
        boolean hasChanged = false;
        int removed = 0;
        int i = 0;
        while (table.containsKey(LuaValue.ofIndex(i))) {
            if (!c.contains(table.get(LuaValue.ofIndex(i)))) {
                table.remove(LuaValue.ofIndex(i));
                removed += 1;
                hasChanged = true;
            }
            i += 1;
        }
        if (removed == i || !hasChanged) {
            return hasChanged;
        }
        int lastI = 0;
        for (int j = 0; j < i; ++j) {
            LuaValue value = table.get(LuaValue.ofIndex(j));
            if (value != null && !value.isNil()) {
                if (lastI != j) {
                    table.put(LuaValue.ofIndex(lastI), value);
                }
                lastI += 1;
            }
        }
        return true;
    }

    @Override
    public ListIterator<LuaValue> listIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<LuaValue> listIterator(int index) {
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
}

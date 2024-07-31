package me.white.wlua;

import java.lang.ref.Cleaner;
import java.util.*;

public abstract class LuaValue {
    private static final Cleaner CLEANER = Cleaner.create();

    protected static LuaValue from(LuaState state, int index) {
        if (state.isClosed) {
            throw new IllegalStateException("Could not get value from closed state.");
        }
        int type = LuaNatives.lua_type(state.ptr, index);
        if (type == LuaConsts.TYPE_NONE || type == LuaConsts.TYPE_NIL) {
            return nil();
        }
        if (type == LuaConsts.TYPE_BOOLEAN) {
            return of(LuaNatives.lua_toboolean(state.ptr, index) == 1);
        }
        if (type == LuaConsts.TYPE_NUMBER) {
            if (LuaNatives.lua_isinteger(state.ptr, index) == 1) {
                return of(LuaNatives.lua_tointeger(state.ptr, index));
            }
            return of(LuaNatives.lua_tonumber(state.ptr, index));
        }
        if (type == LuaConsts.TYPE_STRING) {
            return of(LuaNatives.lua_tostring(state.ptr, index));
        }
        if (type == LuaConsts.TYPE_TABLE) {
            return new TableRef(state, index);
        }
        if (type == LuaConsts.TYPE_FUNCTION) {
            return new FuncRef(state, index);
        }
        if (type == LuaConsts.TYPE_THREAD) {
            return new Thread(state, LuaInstances.get(LuaNatives.getThreadId(state.ptr, index)));
        }
        // TODO: userdata(s)
        return nil();
    }

    public static Bool of(boolean value) {
        return new Bool(value);
    }

    public static Int of(long value) {
        return new Int(value);
    }

    public static Number of(double value) {
        return new Number(value);
    }

    public static Str of(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Could not create string value from null.");
        }
        return new Str(value);
    }

    public static JavaFunc of(JavaFunction.Function value) {
        if (value == null) {
            throw new IllegalArgumentException("Could not create function value from null.");
        }
        return new JavaFunc(new JavaFunction(value));
    }

    public static Table of(Map<LuaValue, LuaValue> value) {
        if (value == null) {
            throw new IllegalArgumentException("Could not create table value from null.");
        }
        return new Table(value);
    }

    public static Nil of() {
        return new Nil();
    }

    public static Nil nil() {
        return new Nil();
    }

    public static Table newTable() {
        return new Table(new HashMap<>());
    }

    public static FuncRef load(LuaState state, String chunk) {
        int code = LuaNatives.luaL_loadstring(state.ptr, chunk);
        if (code != LuaConsts.OK) {
            String name = "Unknown";
            if (code == LuaConsts.ERR_SYNTAX) {
                name = "Syntax";
            }
            if (code == LuaConsts.ERR_MEM) {
                name = "Memory";
            }
            String msg = ((Str)from(state, -1)).getString();
            state.pop(1);
            throw new IllegalStateException(name + " error: " + msg);
        }
        LuaValue value = from(state, -1);
        state.pop(1);
        return (FuncRef)value;
    }

    public static boolean equals(LuaState state, LuaValue value1, LuaValue value2) {
        value1.push(state);
        value2.push(state);
        boolean equals = LuaNatives.lua_compare(state.ptr, -2, -1, LuaConsts.OP_EQ) == 1;
        LuaNatives.lua_pop(state.ptr, 2);
        return equals;
    }

    public boolean equals(LuaState state, LuaValue other) {
        return equals(state, this, other);
    }

    protected abstract void push(LuaState state);

    public void unref() { }

    public static class Bool extends LuaValue {
        private boolean value;

        public Bool(boolean value) {
            this.value = value;
        }

        @Override
        protected void push(LuaState state) {
            LuaNatives.lua_pushboolean(state.ptr, value ? 1 : 0);
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Bool)) {
                return false;
            }
            return value == ((Bool)obj).value;
        }

        @Override
        public int hashCode() {
            return value ? 1 : 0;
        }
    }

    public static class Int extends LuaValue {
        private long value;

        public Int(long value) {
            this.value = value;
        }

        public long getInteger() {
            return value;
        }

        @Override
        protected void push(LuaState state) {
            LuaNatives.lua_pushinteger(state.ptr, value);
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Int)) {
                return false;
            }
            return value == ((Int)obj).value;
        }

        @Override
        public int hashCode() {
            return (int)value;
        }
    }

    public static class Number extends LuaValue {
        private double value;

        public Number(double value) {
            this.value = value;
        }

        public double getDouble() {
            return value;
        }

        @Override
        protected void push(LuaState state) {
            LuaNatives.lua_pushnumber(state.ptr, value);
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Number)) {
                return false;
            }
            return value == ((Number)obj).value;
        }

        @Override
        public int hashCode() {
            return (int)value;
        }
    }

    public static class Str extends LuaValue {
        private String value;

        public Str(String value) {
            this.value = value;
        }

        public String getString() {
            return value;
        }

        @Override
        protected void push(LuaState state) {
            LuaNatives.lua_pushstring(state.ptr, value);
        }

        @Override
        public String toString() {
            return "\"" + value + "\"";
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Str)) {
                return false;
            }
            return value.equals(((Str)obj).value);
        }

        @Override
        public int hashCode() {
            return value.hashCode() * 9;
        }
    }

    public static class Nil extends LuaValue {
        @Override
        protected void push(LuaState state) {
            LuaNatives.lua_pushnil(state.ptr);
        }

        @Override
        public String toString() {
            return "nil";
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            return obj instanceof Nil;
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }

    public static class JavaFunc extends LuaValue {
        private JavaFunction value;

        public JavaFunc(JavaFunction value) {
            super();
            this.value = value;
        }

        public JavaFunction getFunction() {
            return value;
        }

        public VarArg run(LuaState state, VarArg args) {
            if (state.isClosed) {
                throw new IllegalStateException("Could not use reference value from closed state.");
            }
            push(state);
            for (LuaValue value : args.getValues()) {
                value.push(state);
            }
            int valueCount = LuaNatives.lua_pcall(state.ptr, args.size(), LuaConsts.MULT_RET, 0);
            if (valueCount == -1) {
                String msg = ((Str)LuaValue.from(state, -1)).getString();
                throw new IllegalStateException(msg);
            }
            LuaValue[] values = new LuaValue[valueCount];
            for (int i = 0; i < valueCount; ++i) {
                values[i] = LuaValue.from(state, i - valueCount);
            }
            return new VarArg(values);
        }

        @Override
        protected void push(LuaState state) {
            LuaNatives.pushFunction(state.ptr, value);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof JavaFunc)) {
                return false;
            }
            return value.equals(((JavaFunc)obj).value);
        }

        @Override
        public int hashCode() {
            return value.hashCode() * 9;
        }
    }

    public static class Table extends LuaValue implements Map<LuaValue, LuaValue> {
        private Map<LuaValue, LuaValue> map;

        public Table() {
            this.map = new HashMap<>();
        }

        public Table(Map<LuaValue, LuaValue> map) {
            this.map = new HashMap<>(map);
        }

        public TableRef ref(LuaState state) {
            push(state);
            TableRef ref = new TableRef(state, -1);
            state.pop(1);
            return ref;
        }

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return map.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return map.containsValue(value);
        }

        @Override
        public LuaValue get(Object key) {
            return map.get(key);
        }

        @Override
        public LuaValue put(LuaValue key, LuaValue value) {
            return map.put(key, value);
        }

        @Override
        public LuaValue remove(Object key) {
            return map.remove(key);
        }

        @Override
        public void putAll(Map<? extends LuaValue, ? extends LuaValue> m) {
            map.putAll(m);
        }

        @Override
        public void clear() {
            map.clear();
        }

        @Override
        public Set<LuaValue> keySet() {
            return map.keySet();
        }

        @Override
        public Collection<LuaValue> values() {
            return map.values();
        }

        @Override
        public Set<Entry<LuaValue, LuaValue>> entrySet() {
            return map.entrySet();
        }

        @Override
        protected void push(LuaState state) {
            LuaNatives.lua_newtable(state.ptr);
            for (Entry<LuaValue, LuaValue> entry : entrySet()) {
                entry.getKey().push(state);
                entry.getValue().push(state);
                LuaNatives.lua_settable(state.ptr, -3);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Table)) {
                return false;
            }
            return map.equals(((Table)obj).map);
        }

        @Override
        public int hashCode() {
            return map.hashCode() * 9;
        }
    }

    public static class Ref extends LuaValue {
        protected LuaState state;
        protected int reference;

        protected Ref(LuaState state, int index) {
            if (state.isClosed) {
                throw new IllegalStateException("Could not use reference value from closed state.");
            }
            this.state = state;
            int reference = LuaNatives.getRef(state.ptr, index);
            this.reference = reference;
            state.aliveReferences.add(reference);
            // TODO: cleaner not calling
            CLEANER.register(this, () -> {
                if (!state.isClosed && state.aliveReferences.contains(reference)) {
                    LuaNatives.luaL_unref(state.ptr, LuaConsts.REGISTRY_INDEX, reference);
                    state.aliveReferences.remove(reference);
                }
            });
        }

        public boolean isAlive() {
            return state.aliveReferences.contains(reference);
        }

        @Override
        public void unref() {
            if (!state.isClosed && state.aliveReferences.contains(reference)) {
                LuaNatives.luaL_unref(state.ptr, LuaConsts.REGISTRY_INDEX, reference);
                state.aliveReferences.remove(reference);
            }
        }

        @Override
        protected void push(LuaState state) {
            if (!isAlive()) {
                throw new IllegalStateException("Could not use unreferenced value.");
            }
            if (this.state.isClosed) {
                throw new IllegalStateException("Could not use reference value from closed state.");
            }
            if (this.state != state) {
                throw new IllegalStateException("Cannot move references between threads.");
            }
            LuaNatives.lua_rawgeti(state.ptr, LuaConsts.REGISTRY_INDEX, reference);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Ref)) {
                return false;
            }
            Ref ref = (Ref)obj;
            return state.equals(ref.state) && reference == ref.reference;
        }

        @Override
        public int hashCode() {
            return state.hashCode() * 57 + reference;
        }
    }

    public static class TableRef extends Ref implements Map<LuaValue, LuaValue> {
        protected TableRef(LuaState state, int index) {
            super(state, index);
        }

        public Table deref() {
            // TODO: come up with a good name for this method
            if (state.isClosed) {
                throw new IllegalStateException("Could not use reference value from closed state.");
            }
            return new Table(this);
        }

        @Override
        public int size() {
            if (state.isClosed) {
                throw new IllegalStateException("Could not use reference value from closed state.");
            }
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
            if (state.isClosed) {
                throw new IllegalStateException("Could not use reference value from closed state.");
            }
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
            if (state.isClosed) {
                throw new IllegalStateException("Could not use reference value from closed state.");
            }
            if (!(key instanceof LuaValue)) {
                return false;
            }
            push(state);
            ((LuaValue)key).push(state);
            LuaNatives.lua_gettable(state.ptr, -2);
            boolean contains = LuaNatives.lua_isnil(state.ptr, -1) == 0;
            LuaNatives.lua_pop(state.ptr, 2);
            return contains;
        }

        @Override
        public boolean containsValue(Object value) {
            if (state.isClosed) {
                throw new IllegalStateException("Could not use reference value from closed state.");
            }
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
            LuaNatives.lua_pop(state.ptr, 2);
            return false;
        }

        @Override
        public LuaValue get(Object key) {
            if (state.isClosed) {
                throw new IllegalStateException("Could not use reference value from closed state.");
            }
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
            if (state.isClosed) {
                throw new IllegalStateException("Could not use reference value from closed state.");
            }
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
            if (state.isClosed) {
                throw new IllegalStateException("Could not use reference value from closed state.");
            }
            if (!(key instanceof LuaValue)) {
                return null;
            }
            return put((LuaValue)key, LuaValue.nil());
        }

        @Override
        public void putAll(Map<? extends LuaValue, ? extends LuaValue> m) {
            if (state.isClosed) {
                throw new IllegalStateException("Could not use reference value from closed state.");
            }
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
            if (state.isClosed) {
                throw new IllegalStateException("Could not use reference value from closed state.");
            }
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
            if (state.isClosed) {
                throw new IllegalStateException("Could not use reference value from closed state.");
            }
            Set<LuaValue> keys = new HashSet<>();
            push(state);
            LuaValue.nil().push(state);
            while (LuaNatives.lua_next(state.ptr, -2) == 1) {
                LuaNatives.lua_pop(state.ptr, 1);
                keys.add(LuaValue.from(state, -1));
            }
            LuaNatives.lua_pop(state.ptr, 1);
            return keys;
        }

        @Override
        public Collection<LuaValue> values() {
            if (state.isClosed) {
                throw new IllegalStateException("Could not use reference value from closed state.");
            }
            Set<LuaValue> values = new HashSet<>();
            push(state);
            LuaValue.nil().push(state);
            while (LuaNatives.lua_next(state.ptr, -2) == 1) {
                values.add(LuaValue.from(state, -1));
                LuaNatives.lua_pop(state.ptr, 1);
            }
            LuaNatives.lua_pop(state.ptr, 1);
            return values;
        }

        @Override
        public Set<Entry<LuaValue, LuaValue>> entrySet() {
            return new TableSet();
        }

        protected class TableSet extends AbstractSet<Entry<LuaValue, LuaValue>> {
            @Override
            public Iterator<Entry<LuaValue, LuaValue>> iterator() {
                if (state.isClosed) {
                    throw new IllegalStateException("Could not use reference value from closed state.");
                }

                return new Iterator<>() {
                    LuaValue key = LuaValue.nil();

                    @Override
                    public boolean hasNext() {
                        if (state.isClosed) {
                            throw new IllegalStateException("Could not use reference value from closed state.");
                        }
                        push(state);
                        key.push(state);
                        boolean hasNext = LuaNatives.lua_next(state.ptr, -2) == 1;
                        LuaNatives.lua_pop(state.ptr, hasNext ? 3 : 1);
                        return hasNext;
                    }

                    @Override
                    public Entry<LuaValue, LuaValue> next() {
                        if (state.isClosed) {
                            throw new IllegalStateException("Could not use reference value from closed state.");
                        }
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
                        if (state.isClosed) {
                            throw new IllegalStateException("Could not use reference value from closed state.");
                        }
                        if (key instanceof Nil) {
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
                if (state.isClosed) {
                    throw new IllegalStateException("Could not use reference value from closed state.");
                }
                return TableRef.this.size();
            }
        }
    }

    public static class FuncRef extends Ref {
        protected FuncRef(LuaState state, int index) {
            super(state, index);
        }

        public VarArg run(LuaState state, VarArg args) {
            if (state.isClosed) {
                throw new IllegalStateException("Could not use reference value from closed state.");
            }
            push(state);
            for (LuaValue value : args.getValues()) {
                value.push(state);
            }
            int top = LuaNatives.lua_gettop(state.ptr);
            int result = LuaNatives.lua_pcall(state.ptr, args.size(), LuaConsts.MULT_RET, 0);
            if (result != LuaConsts.OK) {
                String name = "Unknown";
                if (result == LuaConsts.ERR_RUN) {
                    name = "Syntax";
                } else if (result == LuaConsts.ERR_MEM) {
                    name = "Memory";
                } else if (result == LuaConsts.ERR_ERR) {
                    name = "Error";
                }
                String msg = ((Str)LuaValue.from(state, -1)).getString();
                throw new IllegalStateException(name + ": " + msg);
            }
            int valueCount = LuaNatives.lua_gettop(state.ptr) - top + 1;
            LuaValue[] values = new LuaValue[valueCount];
            for (int i = 0; i < valueCount; ++i) {
                values[i] = LuaValue.from(state, i - valueCount);
            }
            state.pop(valueCount);
            return new VarArg(values);
        }
    }

    public static class Thread extends LuaValue {
        private LuaState thread;

        public Thread(LuaState mainThread, LuaState thread) {
            this.thread = thread;
        }

        public LuaState getThread() {
            return thread;
        }

        @Override
        protected void push(LuaState state) {
            if (thread.isClosed) {
                throw new IllegalStateException("Could not push to a closed thread.");
            }
            if (state != thread.mainThread && !thread.mainThread.subThreads.contains(state)) {
                throw new IllegalStateException("Could not push thread to the separate lua state.");
            }
            LuaNatives.lua_pushthread(this.thread.ptr);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Thread)) {
                return false;
            }
            return thread.equals(((Thread)obj).thread);
        }

        @Override
        public int hashCode() {
            return thread.hashCode() * 9;
        }
    }
}

package me.white.wlua;

import java.lang.ref.Cleaner;
import java.util.*;

public abstract class LuaValue {
    private static final Cleaner CLEANER = Cleaner.create();

    protected static LuaValue from(LuaState state, int index) {
        state.checkIsAlive();
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
            return new TableRefValue(state, index);
        }
        if (type == LuaConsts.TYPE_FUNCTION) {
            return new FunctionRefValue(state, index);
        }
        if (type == LuaConsts.TYPE_THREAD) {
            return new ThreadValue(state, LuaInstances.get(LuaNatives.getThreadId(state.ptr, index)));
        }
        if (type == LuaConsts.TYPE_USER_DATA || type == LuaConsts.TYPE_LIGHT_USER_DATA) {
            // TODO: userdata(s)
        }
        return nil();
    }

    public static BooleanValue of(boolean value) {
        return new BooleanValue(value);
    }

    public static IntegerValue of(long value) {
        return new IntegerValue(value);
    }

    public static NumberValue of(double value) {
        return new NumberValue(value);
    }

    public static StringValue of(String value) {
        return new StringValue(value);
    }

    public static FunctionValue of(FunctionValue.Function value) {
        return new FunctionValue(value);
    }

    public static TableValue of(Map<LuaValue, LuaValue> value) {
        return new TableValue(value);
    }

    public static NilValue of() {
        return new NilValue();
    }

    public static NilValue nil() {
        return new NilValue();
    }

    public static FunctionRefValue load(LuaState state, String chunk) {
        state.checkIsAlive();
        int code = LuaNatives.luaL_loadstring(state.ptr, chunk);
        if (code != LuaConsts.OK) {
            // TODO: centralized errors
            String name = "Unknown";
            if (code == LuaConsts.ERR_SYNTAX) {
                name = "Syntax";
            }
            if (code == LuaConsts.ERR_MEM) {
                name = "Memory";
            }
            String msg = ((StringValue)from(state, -1)).getString();
            state.pop(1);
            throw new IllegalStateException(name + " error: " + msg);
        }
        LuaValue value = from(state, -1);
        state.pop(1);
        return (FunctionRefValue)value;
    }

    public static boolean equals(LuaState state, LuaValue value1, LuaValue value2) {
        state.checkIsAlive();
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

    public static class Ref extends LuaValue {
        protected LuaState state;
        protected int reference;

        protected Ref(LuaState state, int index) {
            state.checkIsAlive();
            this.state = state;
            this.reference = LuaNatives.getRef(state.ptr, index);
            state.aliveReferences.add(reference);
            // TODO: cleaner not working :(
            CLEANER.register(this, new CleanableRef(state, reference));
        }

        public boolean isAlive() {
            return !state.isClosed() && state.aliveReferences.contains(reference);
        }

        public void checkIsAlive() {
            if (!isAlive()) {
                throw new IllegalStateException("Could not use unreferenced value.");
            }
        }

        @Override
        public void unref() {
            if (!state.isClosed() && state.aliveReferences.contains(reference)) {
                LuaNatives.luaL_unref(state.ptr, LuaConsts.REGISTRY_INDEX, reference);
                state.aliveReferences.remove(reference);
            }
        }

        @Override
        protected void push(LuaState state) {
            checkIsAlive();
            if (this.state.mainThread != state.mainThread) {
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
            return state.mainThread == ref.state.mainThread && reference == ref.reference;
        }

        @Override
        public int hashCode() {
            return state.hashCode() * 17 + reference;
        }

        private static class CleanableRef implements Runnable {
            private LuaState state;
            private int reference;

            private CleanableRef(LuaState state, int reference) {
                this.state = state;
                this.reference = reference;
            }

            @Override
            public void run() {
                System.out.println("clean!!!");
                if (!state.isClosed() && state.aliveReferences.contains(reference)) {
                    LuaNatives.luaL_unref(state.ptr, LuaConsts.REGISTRY_INDEX, reference);
                    state.aliveReferences.remove(reference);
                }
            }
        }
    }
}

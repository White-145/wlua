package me.white.wlua;

public abstract class LuaValue {
    protected static LuaValue from(LuaState state, int index) {
        if (LuaNatives.lua_isnoneornil(state.ptr, index) == 1) {
            return nil();
        }
        if (LuaNatives.lua_isboolean(state.ptr, index) == 1) {
            return of(LuaNatives.lua_toboolean(state.ptr, index) == 1);
        }
        if (LuaNatives.lua_isinteger(state.ptr, index) == 1) {
            return of(LuaNatives.lua_tointeger(state.ptr, index));
        }
        if (LuaNatives.lua_isnumber(state.ptr, index) == 1) {
            return of(LuaNatives.lua_tonumber(state.ptr, index));
        }
        if (LuaNatives.lua_isstring(state.ptr, index) == 1) {
            return of(LuaNatives.lua_tostring(state.ptr, index));
        }
        if (LuaNatives.lua_istable(state.ptr, index) == 1) {
            return new RefValue(state, index);
        }
        // function
        // table
        // thread?
        // userdata(s) - javafunction
        return null;
    }

    public static LuaValue of(boolean value) {
        return new BooleanValue(value);
    }

    public static LuaValue of(long value) {
        return new IntegerValue(value);
    }

    public static LuaValue of(double value) {
        return new NumberValue(value);
    }

    public static LuaValue of(String value) {
        return new StringValue(value == null ? "<null>" : value);
    }

    public static LuaValue nil() {
        return new NilValue();
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

    public static class BooleanValue extends LuaValue {
        private boolean value;

        public BooleanValue(boolean value) {
            this.value = value;
        }

        @Override
        protected void push(LuaState state) {
            LuaNatives.lua_pushboolean(state.ptr, value ? 1 : 0);
        }
    }

    public static class IntegerValue extends LuaValue {
        private long value;

        public IntegerValue(long value) {
            this.value = value;
        }

        @Override
        protected void push(LuaState state) {
            LuaNatives.lua_pushinteger(state.ptr, value);
        }
    }

    public static class NumberValue extends LuaValue {
        private double value;

        public NumberValue(double value) {
            this.value = value;
        }

        @Override
        protected void push(LuaState state) {
            LuaNatives.lua_pushnumber(state.ptr, value);
        }
    }

    public static class StringValue extends LuaValue {
        private String value;

        public StringValue(String value) {
            this.value = value;
        }

        @Override
        protected void push(LuaState state) {
            LuaNatives.lua_pushstring(state.ptr, value);
        }
    }

    public static class JavaFunctionValue extends LuaValue {
        private JavaFunction value;

        public JavaFunctionValue(JavaFunction value) {
            this.value = value;
        }

        @Override
        protected void push(LuaState state) {
            LuaNatives.pushFunction(state.ptr, value);
        }
    }

    public static class NilValue extends LuaValue {
        @Override
        protected void push(LuaState state) {
            LuaNatives.lua_pushnil(state.ptr);
        }
    }

    public static class RefValue extends LuaValue {
        protected LuaState state;
        protected int reference;

        protected RefValue(LuaState state, int index) {
            this.state = state;
            reference = LuaNatives.getRef(state.ptr, index);
        }

        @Override
        protected void push(LuaState state) {
            if (this.state != state) {
                throw new IllegalStateException("Cannot move references between threads.");
            }
            LuaNatives.lua_rawgeti(state.ptr, LuaConsts.REGISTRY_INDEX, reference);
        }
    }
}

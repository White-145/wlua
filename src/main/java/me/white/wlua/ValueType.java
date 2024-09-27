package me.white.wlua;

public enum ValueType {
    NIL(LuaConsts.TYPE_NIL, NilValue.class) {
        @Override
        LuaValue fromStack(LuaState state, int index) {
            return new NilValue();
        }
    },
    BOOLEAN(LuaConsts.TYPE_BOOLEAN, BooleanValue.class) {
        @Override
        LuaValue fromStack(LuaState state, int index) {
            return new BooleanValue(LuaNatives.toBoolean(state.ptr, index));
        }
    },
    NUMBER(LuaConsts.TYPE_NUMBER, NumberValue.class) {
        @Override
        LuaValue fromStack(LuaState state, int index) {
            if (LuaNatives.isInteger(state.ptr, index)) {
                return new IntegerValue(LuaNatives.toInteger(state.ptr, index));
            }
            return new NumberValue(LuaNatives.toNumber(state.ptr, index));
        }
    },
    STRING(LuaConsts.TYPE_STRING, StringValue.class) {
        @Override
        LuaValue fromStack(LuaState state, int index) {
            return new StringValue(LuaNatives.toString(state.ptr, index));
        }
    },
    TABLE(LuaConsts.TYPE_TABLE, TableValue.class) {
        @Override
        LuaValue fromStack(LuaState state, int index) {
            return new TableRefValue(state, index);
        }
    },
    FUNCTION(LuaConsts.TYPE_FUNCTION, FunctionValue.class) {
        @Override
        LuaValue fromStack(LuaState state, int index) {
            return new FunctionRefValue(state, index);
        }
    },
    USERDATA(LuaConsts.TYPE_USER_DATA, UserData.class) {
        @Override
        LuaValue fromStack(LuaState state, int index) {
            Object userdata = LuaNatives.getUserData(state.ptr, index);
            if (!(userdata instanceof UserData)) {
                throw new IllegalStateException("Could not get userdata value.");
            }
            return (UserData)userdata;
        }
    },
    THREAD(LuaConsts.TYPE_THREAD, LuaState.class) {
        @Override
        LuaValue fromStack(LuaState state, int index) {
            return LuaInstances.get(LuaNatives.getThreadId(state.ptr, index));
        }
    };

    final int id;
    final Class<?> clazz;

    ValueType(int id, Class<?> clazz) {
        this.id = id;
        this.clazz = clazz;
    }

    static ValueType fromId(int id) {
        for (ValueType type : ValueType.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }

    LuaValue fromStack(LuaState state, int index) {
        throw new UnsupportedOperationException();
    }

    public static ValueType fromClass(Class<?> clazz) {
        for (ValueType type : ValueType.values()) {
            if (type.clazz.isAssignableFrom(clazz)) {
                return type;
            }
        }
        return null;
    }
}

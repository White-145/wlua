package me.white.wlua;

public enum ValueType {
    NIL(LuaNatives.TNIL, NilValue.class) {
        @Override
        LuaValue fromStack(LuaState state, int index) {
            return NilValue.INSTANCE;
        }
    },
    BOOLEAN(LuaNatives.TBOOLEAN, BooleanValue.class) {
        @Override
        LuaValue fromStack(LuaState state, int index) {
            return LuaNatives.toBoolean(state.ptr, index) ? BooleanValue.TRUE : BooleanValue.FALSE;
        }
    },
    NUMBER(LuaNatives.TNUMBER, NumberValue.class) {
        @Override
        LuaValue fromStack(LuaState state, int index) {
            if (LuaNatives.isInteger(state.ptr, index)) {
                return new IntegerValue(LuaNatives.toInteger(state.ptr, index));
            }
            return new NumberValue(LuaNatives.toNumber(state.ptr, index));
        }
    },
    STRING(LuaNatives.TSTRING, StringValue.class) {
        @Override
        LuaValue fromStack(LuaState state, int index) {
            return new StringValue(LuaNatives.toString(state.ptr, index));
        }
    },
    TABLE(LuaNatives.TTABLE, TableValue.class) {
        @Override
        LuaValue fromStack(LuaState state, int index) {
            return state.getReference(index, TableRefValue::new);
        }
    },
    FUNCTION(LuaNatives.TFUNCTION, FunctionValue.class) {
        @Override
        LuaValue fromStack(LuaState state, int index) {
            return state.getReference(index, FunctionRefValue::new);
        }
    },
    USER_DATA(LuaNatives.TUSERDATA, UserData.class) {
        @Override
        LuaValue fromStack(LuaState state, int index) {
            Object userdata = LuaNatives.getUserData(state.ptr, index);
            if (!(userdata instanceof UserData)) {
                throw new IllegalStateException("Could not get userdata value.");
            }
            return (UserData)userdata;
        }
    },
    THREAD(LuaNatives.TTHREAD, LuaState.class) {
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

package me.white.wlua;

public enum ValueType {
    NIL(LuaConsts.TYPE_NIL) {
        @Override
        LuaValue fromStack(LuaState state, int index) {
            return new NilValue();
        }
    },
    BOOLEAN(LuaConsts.TYPE_BOOLEAN) {
        @Override
        LuaValue fromStack(LuaState state, int index) {
            return new BooleanValue(LuaNatives.toBoolean(state.ptr, index));
        }
    },
    NUMBER(LuaConsts.TYPE_NUMBER) {
        @Override
        LuaValue fromStack(LuaState state, int index) {
            if (LuaNatives.isInteger(state.ptr, index)) {
                return new IntegerValue(LuaNatives.toInteger(state.ptr, index));
            }
            return new NumberValue(LuaNatives.toNumber(state.ptr, index));
        }
    },
    STRING(LuaConsts.TYPE_STRING) {
        @Override
        LuaValue fromStack(LuaState state, int index) {
            return new StringValue(LuaNatives.toString(state.ptr, index));
        }
    },
    TABLE(LuaConsts.TYPE_TABLE) {
        @Override
        LuaValue fromStack(LuaState state, int index) {
            return new TableRefValue(state, index);
        }
    },
    FUNCTION(LuaConsts.TYPE_FUNCTION) {
        @Override
        LuaValue fromStack(LuaState state, int index) {
            return new FunctionRefValue(state, index);
        }
    },
    USERDATA(LuaConsts.TYPE_USER_DATA) {
        @Override
        LuaValue fromStack(LuaState state, int index) {
            Object userdata = LuaNatives.getUserData(state.ptr, index);
            if (!(userdata instanceof UserData)) {
                throw new IllegalStateException("Could not get userdata value.");
            }
            return (UserData)userdata;
        }
    },
    THREAD(LuaConsts.TYPE_THREAD) {
        @Override
        LuaValue fromStack(LuaState state, int index) {
            return LuaInstances.get(LuaNatives.getThreadId(state.ptr, index));
        }
    };

    final int id;

    ValueType(int id) {
        this.id = id;
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
}

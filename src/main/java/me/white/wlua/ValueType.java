package me.white.wlua;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public enum ValueType {
    NIL(LuaBindings.TNIL, "nil", NilValue.class) {
        @Override
        LuaValue fromStack(LuaThread thread, int index) {
            return NilValue.INSTANCE;
        }
    },
    BOOLEAN(LuaBindings.TBOOLEAN, "boolean", BooleanValue.class) {
        @Override
        LuaValue fromStack(LuaThread thread, int index) {
            return LuaBindings.toboolean(thread.address, index) == 1 ? BooleanValue.TRUE : BooleanValue.FALSE;
        }
    },
    NUMBER(LuaBindings.TNUMBER, "number", NumberValue.class) {
        @Override
        LuaValue fromStack(LuaThread thread, int index) {
            if (LuaBindings.isinteger(thread.address, index) == 1) {
                return new IntegerValue(LuaBindings.tointegerx(thread.address, index, MemorySegment.NULL));
            }
            return new NumberValue(LuaBindings.tonumberx(thread.address, index, MemorySegment.NULL));
        }
    },
    STRING(LuaBindings.TSTRING, "string", StringValue.class) {
        @Override
        LuaValue fromStack(LuaThread thread, int index) {
            LuaBindings.pushvalue(thread.address, index);
            String value;
            byte[] bytes;
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment lengthSegment = arena.allocate(ValueLayout.JAVA_INT);
                MemorySegment segment = LuaBindings.tolstring(thread.address, -1, lengthSegment);
                int length = lengthSegment.get(ValueLayout.JAVA_INT, 0);
                value = segment.reinterpret(length + 1).getString(0);
                bytes = segment.reinterpret(length).toArray(ValueLayout.JAVA_BYTE);
            }
            LuaBindings.settop(thread.address, -2);
            return new StringValue(value, bytes);
        }
    },
    TABLE(LuaBindings.TTABLE, "table", TableValue.class) {
        @Override
        LuaValue fromStack(LuaThread thread, int index) {
            LuaState state = thread.getState();
            return state.getReference(thread, index, reference -> new TableValue(state, reference));
        }
    },
    FUNCTION(LuaBindings.TFUNCTION, "function", FunctionValue.class) {
        @Override
        LuaValue fromStack(LuaThread thread, int index) {
            LuaState state = thread.getState();
            return state.getReference(thread, index, reference -> new FunctionValue(state, reference));
        }
    },
    USER_DATA(LuaBindings.TUSERDATA, "userdata", UserData.class) {
        @Override
        LuaValue fromStack(LuaThread thread, int index) {
            LuaBindings.getiuservalue(thread.address, index, 1);
            LuaBindings.getiuservalue(thread.address, -1, 1);
            int id = LuaBindings.tointegerx(thread.address, -1, MemorySegment.NULL);
            LuaBindings.settop(thread.address, -3);
            Object object = ObjectRegistry.get(id);
            if (!(object instanceof UserData)) {
                throw new IllegalArgumentException("Could not get userdata value.");
            }
            return (UserData)object;
        }
    },
    THREAD(LuaBindings.TTHREAD, "thread", LuaThread.class) {
        @Override
        LuaValue fromStack(LuaThread thread, int index) {
            MemorySegment address = LuaBindings.tothread(thread.address, index);
            return LuaThread.getThread(address);
        }
    };

    final int id;
    final String name;
    final Class<?> clazz;

    ValueType(int id, String name, Class<?> clazz) {
        this.id = id;
        this.name = name;
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

    public static ValueType fromClass(Class<?> clazz) {
        for (ValueType type : ValueType.values()) {
            if (type.clazz.isAssignableFrom(clazz)) {
                return type;
            }
        }
        return null;
    }

    LuaValue fromStack(LuaThread thread, int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return name;
    }
}

package me.white.wlua;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

// ASSUMING all entries follow the order of their type ids in LuaBindings
public enum ValueType {
    NIL("nil") {
        @Override
        LuaValue fromStack(LuaThread thread, int index) {
            return NilValue.INSTANCE;
        }
    },
    BOOLEAN("boolean") {
        @Override
        LuaValue fromStack(LuaThread thread, int index) {
            return LuaBindings.toboolean(thread.address, index) == 1 ? BooleanValue.TRUE : BooleanValue.FALSE;
        }
    },
    // ASSUMING is never used
    LIGHT_USER_DATA("light user data"),
    NUMBER("number") {
        @Override
        LuaValue fromStack(LuaThread thread, int index) {
            if (LuaBindings.isinteger(thread.address, index) == 1) {
                return new IntegerValue(LuaBindings.tointegerx(thread.address, index, MemorySegment.NULL));
            }
            return new NumberValue(LuaBindings.tonumberx(thread.address, index, MemorySegment.NULL));
        }
    },
    STRING("string") {
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
    TABLE("table") {
        @Override
        LuaValue fromStack(LuaThread thread, int index) {
            LuaState state = thread.getState();
            return state.references.getReference(thread, index, reference -> new TableValue(state, reference));
        }
    },
    FUNCTION("function") {
        @Override
        LuaValue fromStack(LuaThread thread, int index) {
            LuaState state = thread.getState();
            return state.references.getReference(thread, index, reference -> new FunctionValue(state, reference));
        }
    },
    USER_DATA("userdata") {
        @Override
        LuaValue fromStack(LuaThread thread, int index) {
            LuaBindings.getiuservalue(thread.address, index, 1);
            LuaBindings.getiuservalue(thread.address, -1, 1);
            int id = LuaBindings.tointegerx(thread.address, -1, MemorySegment.NULL);
            LuaBindings.settop(thread.address, -3);
            Object object = ObjectManager.get(id);
            if (!(object instanceof UserData)) {
                throw new IllegalArgumentException("Could not get userdata value.");
            }
            return (UserData)object;
        }
    },
    THREAD("thread") {
        @Override
        LuaValue fromStack(LuaThread thread, int index) {
            MemorySegment address = LuaBindings.tothread(thread.address, index);
            return LuaThread.getThread(address);
        }
    };

    final String name;

    ValueType(String name) {
        this.name = name;
    }

    // ASSUMING id is ever -1 or valid
    static ValueType fromId(int id) {
        if (id == LuaBindings.TNONE) {
            return null;
        }
        return values()[id];
    }

    LuaValue fromStack(LuaThread thread, int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return name;
    }
}

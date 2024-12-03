package me.white.wlua;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.ArrayList;
import java.util.List;

public final class FunctionValue extends RefValue {
    FunctionValue(LuaState state, int reference) {
        super(state, reference);
    }

    @Override
    public FunctionValue copy(LuaThread thread) {
        if (state.isSubThread(thread)) {
            return this;
        }
        state.pushValue(this);
        if (LuaBindings.iscfunction(state.address, -1) == 1) {
            MemorySegment name = LuaBindings.getupvalue(state.address, -1, 1);
            if (name.equals(MemorySegment.NULL)) {
                throw new IllegalStateException("Could not get function object.");
            }
            // ASSUMING function is created by wlua
            LuaBindings.getiuservalue(state.address, -1, 1);
            LuaBindings.getiuservalue(state.address, -1, 1);
            int id = LuaBindings.tointegerx(state.address, -1, MemorySegment.NULL);
            LuaBindings.settop(state.address, -5);
            Object obj = ObjectManager.get(id);
            return LuaValue.fromFunction(thread, (JavaFunction)obj);
        }
        LuaBindings.settop(state.address, -2);
        return LuaValue.fromBytes(thread, dump(false));
    }

    public byte[] dump(boolean strip) {
        state.pushValue(this);
        List<byte[]> chunks = new ArrayList<>();
        int id = ObjectManager.create(chunks);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment ud = arena.allocateFrom(ValueLayout.JAVA_INT, id);
            LuaBindings.dump(state.address, LuaState.DUMP_WRITER, ud, strip ? 1 : 0);
        }
        ObjectManager.remove(id);
        LuaBindings.settop(state.address, -2);
        int totalSize = 0;
        for (byte[] chunk : chunks) {
            totalSize += chunk.length;
        }
        byte[] bytes = new byte[totalSize];
        int offset = 0;
        for (byte[] chunk : chunks) {
            int size = chunk.length;
            System.arraycopy(chunk, 0, bytes, offset, size);
            offset += size;
        }
        return bytes;
    }

    @Override
    public ValueType getType() {
        return ValueType.FUNCTION;
    }

    public VarArg call(VarArg args) {
        return call(state, args);
    }
}

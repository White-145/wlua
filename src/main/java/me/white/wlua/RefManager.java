package me.white.wlua;

import java.lang.foreign.MemorySegment;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

class RefManager {
    private final Map<Integer, WeakReference<RefValue>> references = new HashMap<>();
    private final LuaState state;
    private int nextReference = 1;

    RefManager(LuaState state) {
        this.state = state;
    }
    
    LuaValue getReference(LuaThread thread, int index, Function<Integer, RefValue> provider) {
        int absindex = LuaBindings.absindex(state.address, index);
        LuaBindings.rawgeti(state.address, LuaBindings.REGISTRYINDEX, LuaState.RIDX_REFERENCES);
        LuaBindings.pushvalue(thread.address, absindex);
        LuaBindings.rawget(thread.address, -2);
        int reference;
        if (LuaBindings.isinteger(thread.address, -1) == 1) {
            reference = LuaBindings.tointegerx(thread.address, -1, MemorySegment.NULL);
            LuaBindings.settop(thread.address, -3);
            if (references.get(reference) != null) {
                return references.get(reference).get();
            }
        } else {
            reference = nextReference;
            nextReference += 1;
            LuaBindings.pushvalue(thread.address, absindex);
            LuaBindings.pushinteger(thread.address, reference);
            LuaBindings.settable(thread.address, -4);
            LuaBindings.pushinteger(thread.address, reference);
            LuaBindings.pushvalue(thread.address, absindex);
            LuaBindings.settable(thread.address, -4);
            LuaBindings.settop(thread.address, -3);
        }
        RefValue value = provider.apply(reference);
        references.put(reference, new WeakReference<>(value));
        return value;
    }

    boolean hasReference(int reference) {
        if (!state.isAlive()) {
            return false;
        }
        LuaBindings.rawgeti(state.address, LuaBindings.REGISTRYINDEX, LuaState.RIDX_REFERENCES);
        boolean contains = LuaBindings.rawgeti(state.address, -1, reference) != LuaBindings.TNIL;
        LuaBindings.settop(state.address, -3);
        return contains;
    }

    void fromReference(LuaThread thread, int reference) {
        if (!state.isSubThread(thread)) {
            throw new IllegalStateException("Could not move references between states.");
        }
        LuaBindings.rawgeti(thread.address, LuaBindings.REGISTRYINDEX, LuaState.RIDX_REFERENCES);
        LuaBindings.pushinteger(thread.address, reference);
        LuaBindings.rawget(thread.address, -2);
        LuaBindings.copy(thread.address, -1, -2);
        LuaBindings.settop(thread.address, -2);
    }

    void cleanReference(int reference) {
        if (!hasReference(reference)) {
            return;
        }
        references.remove(reference);
        LuaBindings.rawgeti(state.address, LuaBindings.REGISTRYINDEX, LuaState.RIDX_REFERENCES);
        LuaBindings.pushinteger(state.address, reference);
        LuaBindings.rawget(state.address, -2);
        LuaBindings.pushnil(state.address);
        LuaBindings.rawset(state.address, -3);
        LuaBindings.pushinteger(state.address, reference);
        LuaBindings.pushnil(state.address);
        LuaBindings.rawset(state.address, -3);
        LuaBindings.settop(state.address, -2);
    }

    void clear() {
        references.clear();
    }
}

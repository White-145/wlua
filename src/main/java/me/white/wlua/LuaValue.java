package me.white.wlua;

import java.lang.ref.Cleaner;
import java.util.*;

public abstract class LuaValue {
    private static final Cleaner CLEANER = Cleaner.create();

    static LuaValue from(LuaState state, int index) {
        state.checkIsAlive();
        int type = LuaNatives.getType(state.ptr, index);
        if (type == LuaConsts.TYPE_NONE || type == LuaConsts.TYPE_NIL) {
            return nil();
        }
        if (type == LuaConsts.TYPE_BOOLEAN) {
            return of(LuaNatives.toBoolean(state.ptr, index));
        }
        if (type == LuaConsts.TYPE_NUMBER) {
            if (LuaNatives.isInteger(state.ptr, index)) {
                return of(LuaNatives.toInteger(state.ptr, index));
            }
            return of(LuaNatives.toNumber(state.ptr, index));
        }
        if (type == LuaConsts.TYPE_STRING) {
            return of(LuaNatives.toString(state.ptr, index));
        }
        if (type == LuaConsts.TYPE_TABLE) {
            return new TableRefValue(state, index);
        }
        if (type == LuaConsts.TYPE_FUNCTION) {
            return new FunctionRefValue(state, index);
        }
        if (type == LuaConsts.TYPE_THREAD) {
            return LuaInstances.get(LuaNatives.getThreadId(state.ptr, index));
        }
        if (type == LuaConsts.TYPE_USER_DATA || type == LuaConsts.TYPE_LIGHT_USER_DATA) {
            Object userdata = LuaNatives.getUserData(state.ptr, index);
            if (!(userdata instanceof UserData)) {
                throw new IllegalStateException("Could not get userdata value.");
            }
            return (UserData)userdata;
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
        return nil();
    }

    public static NilValue nil() {
        return new NilValue();
    }

    public static FunctionRefValue chunk(LuaState state, String chunk) {
        state.checkIsAlive();
        int code = LuaNatives.loadString(state.ptr, chunk);
        LuaException.checkError(code, state);
        LuaValue value = from(state, -1);
        state.pop(1);
        return (FunctionRefValue)value;
    }

    public static boolean equals(LuaState state, LuaValue value1, LuaValue value2) {
        state.checkIsAlive();
        state.pushValue(value1);
        state.pushValue(value2);
        boolean equals = LuaNatives.compareValues(state.ptr, -2, -1, LuaConsts.OP_EQ);
        state.pop(2);
        return equals;
    }

    public boolean equals(LuaState state, LuaValue other) {
        return equals(state, this, other);
    }

    abstract void push(LuaState state);

    public void unref() { }

    public static class Ref extends LuaValue {
        private final Cleaner.Cleanable cleanable;
        private final CleanableRef cleanableRef;
        protected final LuaState state;
        protected final int reference;

        protected Ref(LuaState state, int index) {
            state.checkIsAlive();
            this.state = state;
            this.reference = LuaNatives.newRef(state.ptr, index);
            state.aliveReferences.add(reference);
            cleanableRef = new CleanableRef(state, reference);
            cleanable = CLEANER.register(this, cleanableRef);
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
                LuaNatives.deleteRef(state.ptr, reference);
                state.aliveReferences.remove(reference);
            }
            cleanable.clean();
        }

        @Override
        void push(LuaState state) {
            checkIsAlive();
            if (this.state.mainThread != state.mainThread) {
                throw new IllegalStateException("Cannot move references between threads.");
            }
            LuaNatives.getRef(state.ptr, reference);
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
                if (!state.isClosed() && state.aliveReferences.contains(reference)) {
                    LuaNatives.deleteRef(state.ptr, reference);
                    state.aliveReferences.remove(reference);
                }
            }
        }
    }
}

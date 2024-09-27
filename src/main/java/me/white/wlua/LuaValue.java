package me.white.wlua;

import java.lang.ref.Cleaner;
import java.util.*;

public sealed abstract class LuaValue permits BooleanValue, FunctionLiteralValue, LuaState, LuaValue.Ref, NilValue, NumberValue, StringValue, TableLiteralValue, UserData {
    private static final Cleaner CLEANER = Cleaner.create();

    static LuaValue from(LuaState state, int index) {
        state.checkIsAlive();
        ValueType valueType = ValueType.fromId(LuaNatives.getType(state.ptr, index));
        if (valueType == null) {
            return fail();
        }
        return valueType.fromStack(state, index);
    }

    public static BooleanValue valueOf(boolean value) {
        return new BooleanValue(value);
    }

    public static IntegerValue valueOf(long value) {
        return new IntegerValue(value);
    }

    public static NumberValue valueOf(double value) {
        return new NumberValue(value);
    }

    public static StringValue valueOf(String value) {
        return new StringValue(value);
    }

    public static FunctionLiteralValue valueOf(FunctionLiteralValue.Function value) {
        return new FunctionLiteralValue(value);
    }

    public static TableLiteralValue valueOf(Map<LuaValue, LuaValue> value) {
        return new TableLiteralValue(value);
    }

    public static NilValue nil() {
        return new NilValue();
    }

    static FailValue fail() {
        return new FailValue();
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

    public boolean isNil() {
        return false;
    }

    public boolean getBoolean() {
        return true;
    }

    public double getNumber() {
        throw new UnsupportedOperationException();
    }

    public long getInteger() {
        throw new UnsupportedOperationException();
    }

    public String getString() {
        return toString();
    }

    public abstract ValueType getType();

    abstract void push(LuaState state);

    public static sealed abstract class Ref extends LuaValue permits FunctionRefValue, TableRefValue {
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

        public void unref() {
            cleanable.clean();
        }

        @Override
        final void push(LuaState state) {
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
                state.cleanReference(reference);
            }
        }
    }
}

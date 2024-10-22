package me.white.wlua;

import java.lang.ref.Cleaner;
import java.util.*;

public sealed abstract class LuaValue permits BooleanValue, FunctionLiteralValue, ListLiteralValue, ListRefValue, LuaState, LuaValue.Ref, NilValue, NumberValue, StringValue, TableLiteralValue, UserData {
    private static final Cleaner CLEANER = Cleaner.create();

    static LuaValue from(LuaState state, int index) {
        return state.fromStack(index);
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

    public static FunctionLiteralValue of(FunctionLiteralValue.Function value) {
        return new FunctionLiteralValue(value);
    }

    public static TableLiteralValue of(Map<LuaValue, LuaValue> value) {
        return new TableLiteralValue(value);
    }

    public static ListLiteralValue of(List<LuaValue> list) {
        Map<LuaValue, LuaValue> map = new HashMap<>();
        for (int i = 0; i < list.size(); ++i) {
            map.put(LuaValue.index(i), list.get(i));
        }
        return (ListLiteralValue)new TableLiteralValue(map).getList();
    }

    public static IntegerValue index(int index) {
        return new IntegerValue(index + 1);
    }

    public static NilValue nil() {
        return NilValue.INSTANCE;
    }

    public static boolean equals(LuaState state, LuaValue value1, LuaValue value2) {
        state.checkIsAlive();
        state.pushValue(value1);
        state.pushValue(value2);
        boolean equals = LuaNatives.equal(state.ptr, -2, -1);
        state.pop(2);
        return equals;
    }

    public static boolean isNil(Object value) {
        return !(value instanceof LuaValue) || ((LuaValue)value).isNil();
    }

    public final boolean equals(LuaState state, LuaValue other) {
        return equals(state, this, other);
    }

    public boolean isNil() {
        return false;
    }

    public boolean isNumber() {
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
        // TODO imitate luas tostring?
        return toString();
    }

    public abstract ValueType getType();

    abstract void push(LuaState state);

    public static sealed abstract class Ref extends LuaValue permits FunctionRefValue, TableRefValue {
        private final Cleaner.Cleanable cleanable;
        private final CleanableRef cleanableRef;
        private final int reference;
        protected final LuaState state;

        protected Ref(LuaState state, int index) {
            state.checkIsAlive();
            this.state = state;
            this.reference = state.newReference(index);
            cleanableRef = new CleanableRef(state, reference);
            cleanable = CLEANER.register(this, cleanableRef);
        }

        public boolean isAlive() {
            return state.hasReference(reference);
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
            if (!state.isSubThread(this.state)) {
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
            return ref.state.isSubThread(state) && reference == ref.reference;
        }

        @Override
        public int hashCode() {
            return state.getMainThread().hashCode() * 17 + reference;
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

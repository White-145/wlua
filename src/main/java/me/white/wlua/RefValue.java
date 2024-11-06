package me.white.wlua;

import java.lang.ref.Cleaner;

public sealed abstract class RefValue extends LuaValue permits FunctionValue, TableValue {
    private static final Cleaner CLEANER = Cleaner.create();
    private final Cleaner.Cleanable cleanable;
    private final RefCleanable refCleanable;
    private final int reference;
    protected final LuaState state;

    protected RefValue(LuaState state, int reference) {
        state.checkIsAlive();
        this.state = state;
        this.reference = reference;
        refCleanable = new RefCleanable(state, reference);
        cleanable = CLEANER.register(this, refCleanable);
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
        LuaNatives.fromReference(state.ptr, reference);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof RefValue)) {
            return false;
        }
        RefValue ref = (RefValue)obj;
        return ref.state.isSubThread(state) && reference == ref.reference;
    }

    @Override
    public int hashCode() {
        return state.getMainThread().hashCode() * 17 + reference;
    }

    private static class RefCleanable implements Runnable {
        private LuaState state;
        private int reference;

        private RefCleanable(LuaState state, int reference) {
            this.state = state;
            this.reference = reference;
        }

        @Override
        public void run() {
            state.cleanReference(reference);
        }
    }

    @FunctionalInterface
    interface RefValueProvider<T extends RefValue> {
        T getReference(LuaState state, int reference);
    }
}

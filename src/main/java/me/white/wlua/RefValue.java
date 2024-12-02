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
        return state.references.hasReference(reference);
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
    final void push(LuaThread thread) {
        checkIsAlive();
        state.references.fromReference(thread, reference);
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
        return state.equals(ref.state) && reference == ref.reference;
    }

    @Override
    public int hashCode() {
        return state.hashCode() * 17 + reference;
    }

    private static class RefCleanable implements Runnable {
        private final LuaState state;
        private final int reference;

        private RefCleanable(LuaState state, int reference) {
            this.state = state;
            this.reference = reference;
        }

        @Override
        public void run() {
            state.references.cleanReference(reference);
        }
    }
}

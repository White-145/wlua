package me.white.wlua;

import java.lang.ref.WeakReference;
import java.util.*;

public final class LuaState extends LuaValue implements AutoCloseable {
    private Properties properties = new Properties();
    private boolean isClosed = false;
    private int id;
    private List<LuaState> subThreads = new ArrayList<>();
    private Map<Integer, WeakReference<LuaValue.Ref>> aliveReferences = new HashMap<>();
    private LuaState mainThread;
    long ptr;

    // TODO raw operations

    LuaState(long ptr, int stateId, LuaState mainThread) {
        if (ptr == 0) {
            throw new IllegalStateException("Could not create new lua state.");
        }
        this.ptr = ptr;
        this.id = stateId == -1 ? LuaInstances.add(this) : stateId;
        this.mainThread = mainThread == null ? this : mainThread;
        if (mainThread != null) {
            mainThread.subThreads.add(this);
            properties.putAll(mainThread.properties);
        }
        LuaNatives.initState(ptr, id);
    }

    public LuaState() {
        this(LuaNatives.newState(), -1, null);
    }

    void pop(int n) {
        checkIsAlive();
        LuaNatives.pop(ptr, n);
    }

    void pushValue(LuaValue value) {
        checkIsAlive();
        if (value == null) {
            pushNil();
        } else {
            value.push(this);
        }
    }

    void pushNil() {
        checkIsAlive();
        LuaNatives.pushNil(ptr);
    }

    LuaValue fromStack(int index) {
        checkIsAlive();
        ValueType valueType = ValueType.fromId(LuaNatives.getType(ptr, index));
        if (valueType == null) {
            return nil();
        }
        return valueType.fromStack(this, index);
    }

    @SuppressWarnings("unchecked")
    <T extends Ref> T getReference(int index, RefValueProvider<T> provider) {
        int reference = LuaNatives.newRef(ptr, index);
        if (mainThread.aliveReferences.containsKey(reference)) {
            return (T)mainThread.aliveReferences.get(reference).get();
        }
        T ref = provider.getReference(this, reference);
        mainThread.aliveReferences.put(reference, new WeakReference<>(ref));
        return ref;
    }

    boolean hasReference(int reference) {
        return !isClosed() && mainThread.aliveReferences.containsKey(reference);
    }

    void cleanReference(int reference) {
        if (!isClosed() && mainThread.aliveReferences.containsKey(reference)) {
            LuaNatives.deleteRef(ptr, reference);
            mainThread.aliveReferences.remove(reference);
        }
    }

    public void checkIsAlive() {
        if (isClosed()) {
            throw new IllegalStateException("Could not use closed lua state.");
        }
    }

    public LuaState getMainThread() {
        return mainThread;
    }

    public boolean isSubThread(LuaState thread) {
        Objects.requireNonNull(thread);
        return !isClosed() && thread.mainThread == mainThread;
    }

    public LuaState subThread() {
        checkIsAlive();
        int threadId = LuaInstances.add(id -> new LuaState(LuaNatives.newThread(mainThread.ptr), id, mainThread));
        LuaState thread = LuaInstances.get(threadId);
        mainThread.subThreads.add(thread);
        return thread;
    }

    public Properties getProperties() {
        return properties;
    }

    public void openLibs() {
        // TODO do better
        checkIsAlive();
        LuaNatives.openLibs(ptr);
    }

    public TableRefValue getGlobalTable() {
        checkIsAlive();
        LuaNatives.pushGlobalTable(ptr);
        LuaValue ref = LuaValue.from(this, -1);
        pop(1);
        return (TableRefValue)ref;
    }

    public void setGlobal(String name, LuaValue value) {
        Objects.requireNonNull(name);
        checkIsAlive();
        if (value == null) {
            pushNil();
        } else {
            pushValue(value);
        }
        LuaNatives.setGlobal(ptr, name);
    }

    public LuaValue getGlobal(String name) {
        Objects.requireNonNull(name);
        checkIsAlive();
        LuaNatives.getGlobal(ptr, name);
        LuaValue value = LuaValue.from(this, -1);
        pop(1);
        return value;
    }

    public FunctionRefValue load(String chunk, String name) {
        Objects.requireNonNull(chunk);
        Objects.requireNonNull(name);
        checkIsAlive();
        int code = LuaNatives.loadString(ptr, chunk, name);
        LuaException.checkError(code, this);
        LuaValue value = LuaValue.from(this, -1);
        pop(1);
        return (FunctionRefValue)value;
    }

    public FunctionRefValue load(String chunk) {
        return load(chunk, chunk);
    }

    public VarArg run(FunctionValue chunk, VarArg args) {
        Objects.requireNonNull(chunk);
        Objects.requireNonNull(args);
        checkIsAlive();
        int top = LuaNatives.getTop(ptr);
        pushValue((LuaValue)chunk);
        args.push(this);
        int code = LuaNatives.protectedCall(ptr, args.size(), LuaNatives.MULTRET);
        LuaException.checkError(code, this);
        int amount = LuaNatives.getTop(ptr) - top;
        return VarArg.collect(this, amount);
    }

    public VarArg run(String chunk) {
        return run(load(chunk), new VarArg());
    }

    private VarArg resume(FunctionValue chunk, VarArg args) {
        Objects.requireNonNull(args);
        checkIsAlive();
        int top = LuaNatives.getTop(ptr);
        if (chunk != null) {
            pushValue((LuaValue)chunk);
        } else if (!isSuspended()) {
            throw new IllegalStateException("Cannot resume not suspended state.");
        }
        args.push(this);
        int code = LuaNatives.resume(ptr, args.size());
        LuaException.checkError(code, this);
        int amount = LuaNatives.getTop(ptr) - top;
        return VarArg.collect(this, amount);
    }

    public VarArg start(FunctionValue chunk, VarArg args) {
        return resume(chunk, args);
    }

    public VarArg resume(VarArg args) {
        return resume(null, args);
    }

    // VarArg parameter is only used for neat `return yield(result)` syntax
    public VarArg yield(VarArg result) {
        checkIsAlive();
        if (!isYieldable()) {
            throw new IllegalStateException("Cannot yield non-yieldable state");
        }
        LuaNatives.yield(ptr);
        return result;
    }

    public VarArg yield() {
        return this.yield(new VarArg());
    }

    public boolean isYieldable() {
        return !isClosed() && LuaNatives.isYieldable(ptr);
    }

    public boolean isSuspended() {
        return !isClosed() && LuaNatives.isSuspended(ptr);
    }

    public boolean isClosed() {
        return mainThread.isClosed;
    }

    @Override
    public ValueType getType() {
        return ValueType.THREAD;
    }

    @Override
    void push(LuaState state) {
        if (!state.isSubThread(this)) {
            throw new IllegalStateException("Could not push thread to the separate lua state.");
        }
        LuaNatives.pushThread(ptr);
    }

    @Override
    public void close() {
        if (isClosed()) {
            return;
        }
        LuaInstances.remove(id);
        if (mainThread == this) {
            for (LuaState thread : subThreads) {
                LuaInstances.remove(thread.id);
            }
            subThreads.clear();
            LuaNatives.closeState(ptr);
        } else {
            mainThread.subThreads.remove(this);
            LuaNatives.removeState(ptr);
        }
        isClosed = true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LuaState)) {
            return false;
        }
        return ptr == ((LuaState)obj).ptr;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(ptr);
    }
}

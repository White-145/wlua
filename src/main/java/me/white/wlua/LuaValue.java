package me.white.wlua;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public sealed abstract class LuaValue permits BooleanValue, ListValue, LuaThread, NilValue, NumberValue, RefValue, StringValue, UserData {
    static LuaValue from(LuaThread thread, int index) {
        thread.checkIsAlive();
        ValueType valueType = ValueType.fromId(LuaBindings.type(thread.address, index));
        if (valueType == null) {
            return LuaValue.nil();
        }
        return valueType.fromStack(thread, index);
    }

    public static BooleanValue of(boolean value) {
        return value ? BooleanValue.TRUE : BooleanValue.FALSE;
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

    public static NilValue nil() {
        return NilValue.INSTANCE;
    }

    public static FunctionValue load(LuaThread thread, String chunk) {
        Objects.requireNonNull(thread);
        thread.checkIsAlive();
        Objects.requireNonNull(chunk);
        int code;
        try (Arena arena = Arena.ofConfined()) {
            code = LuaBindings.auxiliaryLoadstring(thread.address, arena.allocateFrom(chunk));
        }
        LuaException.checkError(code, thread);
        FunctionValue function = (FunctionValue)from(thread, -1);
        LuaBindings.settop(thread.address, -2);
        return function;
    }

    public static FunctionValue fromFunction(LuaThread thread, JavaFunction function) {
        Objects.requireNonNull(thread);
        thread.checkIsAlive();
        Objects.requireNonNull(function);
        thread.pushObject(function);
        LuaBindings.pushcclosure(thread.address, LuaState.RUN_FUNCTION, 1);
        LuaValue value = from(thread, -1);
        LuaBindings.settop(thread.address, -2);
        return (FunctionValue)value;
    }

    public static FunctionValue fromBytes(LuaThread thread, byte[] bytes) {
        Objects.requireNonNull(thread);
        thread.checkIsAlive();
        Objects.requireNonNull(bytes);
        int code;
        try (Arena arena = Arena.ofConfined()) {
            code = LuaBindings.auxiliaryLoadbufferx(thread.address, arena.allocateFrom(ValueLayout.JAVA_BYTE, bytes), bytes.length, arena.allocateFrom("chunk"), MemorySegment.NULL);
        }
        LuaException.checkError(code, thread);
        FunctionValue value = (FunctionValue)LuaValue.from(thread, -1);
        LuaBindings.settop(thread.address, -2);
        return value;
    }

    public static TableValue fromMap(LuaThread thread, Map<LuaValue, LuaValue> map) {
        Objects.requireNonNull(thread);
        thread.checkIsAlive();
        Objects.requireNonNull(map);
        LuaBindings.createtable(thread.address, 0, map.size());
        for (Map.Entry<LuaValue, LuaValue> entry : map.entrySet()) {
            thread.pushValue(entry.getKey());
            thread.pushValue(entry.getValue());
            LuaBindings.settable(thread.address, -3);
        }
        LuaValue value = from(thread, -1);
        LuaBindings.settop(thread.address, -2);
        return (TableValue)value;
    }

    public static ListValue fromList(LuaThread thread, List<LuaValue> list) {
        Map<LuaValue, LuaValue> map = new HashMap<>();
        for (int i = 0; i < list.size(); ++i) {
            map.put(LuaValue.index(i), list.get(i));
        }
        return fromMap(thread, map).getList();
    }

    public static TableValue table(LuaThread thread) {
        return fromMap(thread, Map.of());
    }

    public static ListValue list(LuaThread thread) {
        return table(thread).getList();
    }

    public static IntegerValue index(int index) {
        return new IntegerValue(index + 1);
    }

    public static boolean isNil(Object value) {
        return !(value instanceof LuaValue) || ((LuaValue)value).isNil();
    }

    public static LuaValue orNull(LuaValue value) {
        return isNil(value) ? null : value;
    }

    private LuaValue arith(LuaThread thread, LuaValue value, int code) {
        thread.pushValue(this);
        thread.pushValue(value);
        LuaBindings.arith(thread.address, code);
        LuaValue result = from(thread, -1);
        LuaBindings.settop(thread.address, -2);
        return result;
    }

    private LuaValue arith(LuaThread thread, int code) {
        thread.pushValue(this);
        LuaBindings.arith(thread.address, code);
        LuaValue result = from(thread, -1);
        LuaBindings.settop(thread.address, -2);
        return result;
    }

    private boolean compare(LuaThread thread, LuaValue value, int code) {
        thread.pushValue(this);
        thread.pushValue(value);
        boolean result = LuaBindings.compare(thread.address, -2, -1, code) == 1;
        LuaBindings.settop(thread.address, -3);
        return result;
    }

    public final TableValue getMetaTable(LuaThread thread) {
        thread.checkIsAlive();
        thread.pushValue(this);
        if (LuaBindings.getmetatable(thread.address, -1) == 0) {
            LuaBindings.settop(thread.address, -2);
            return null;
        }
        LuaValue value = from(thread, -1);
        LuaBindings.settop(thread.address, -3);
        return (TableValue)value;
    }

    public final void setMetaTable(LuaThread thread, TableValue metatable) {
        thread.checkIsAlive();
        thread.pushValue(this);
        thread.pushValue(metatable);
        LuaBindings.setmetatable(thread.address, -2);
        LuaBindings.settop(thread.address, -2);
    }

    public final LuaValue add(LuaThread thread, LuaValue value) {
        return arith(thread, value, LuaBindings.OPADD);
    }

    public final LuaValue sub(LuaThread thread, LuaValue value) {
        return arith(thread, value, LuaBindings.OPSUB);
    }

    public final LuaValue mul(LuaThread thread, LuaValue value) {
        return arith(thread, value, LuaBindings.OPMUL);
    }

    public final LuaValue div(LuaThread thread, LuaValue value) {
        return arith(thread, value, LuaBindings.OPDIV);
    }

    public final LuaValue intDiv(LuaThread thread, LuaValue value) {
        return arith(thread, value, LuaBindings.OPIDIV);
    }

    public final LuaValue mod(LuaThread thread, LuaValue value) {
        return arith(thread, value, LuaBindings.OPMOD);
    }

    public final LuaValue pow(LuaThread thread, LuaValue value) {
        return arith(thread, value, LuaBindings.OPPOW);
    }

    public final LuaValue minus(LuaThread thread) {
        return arith(thread, LuaBindings.OPUNM);
    }

    public final LuaValue not(LuaThread thread) {
        return arith(thread, LuaBindings.OPBNOT);
    }

    public final LuaValue and(LuaThread thread, LuaValue value) {
        return arith(thread, value, LuaBindings.OPBAND);
    }

    public final LuaValue or(LuaThread thread, LuaValue value) {
        return arith(thread, value, LuaBindings.OPBOR);
    }

    public final LuaValue xor(LuaThread thread, LuaValue value) {
        return arith(thread, value, LuaBindings.OPBXOR);
    }

    public final LuaValue shiftLeft(LuaThread thread, LuaValue value) {
        return arith(thread, value, LuaBindings.OPSHL);
    }

    public final LuaValue shiftRight(LuaThread thread, LuaValue value) {
        return arith(thread, value, LuaBindings.OPSHR);
    }

    public final boolean equals(LuaThread thread, LuaValue value) {
        return compare(thread, value, LuaBindings.OPEQ);
    }

    public final boolean lessThan(LuaThread thread, LuaValue value) {
        return compare(thread, value, LuaBindings.OPLT);
    }

    public final boolean lessEqual(LuaThread thread, LuaValue value) {
        return compare(thread, value, LuaBindings.OPLE);
    }

    public final LuaValue length(LuaThread thread) {
        thread.pushValue(this);
        LuaBindings.len(thread.address, -1);
        LuaValue result = from(thread, -1);
        LuaBindings.settop(thread.address, -3);
        return result;
    }

    public final LuaValue concat(LuaThread thread, LuaValue... values) {
        thread.pushValue(this);
        for (LuaValue value : values) {
            thread.pushValue(value);
        }
        LuaBindings.concat(thread.address, values.length + 1);
        LuaValue result = from(thread, -1);
        LuaBindings.settop(thread.address, -2);
        return result;
    }

    public final LuaValue index(LuaThread thread, LuaValue index) {
        thread.pushValue(this);
        thread.pushValue(index);
        LuaBindings.gettable(thread.address, -2);
        LuaValue result = from(thread, -1);
        LuaBindings.settop(thread.address, -3);
        return result;
    }

    public final void newIndex(LuaThread thread, LuaValue index, LuaValue value) {
        thread.pushValue(this);
        thread.pushValue(index);
        thread.pushValue(value);
        LuaBindings.settable(thread.address, -3);
        LuaBindings.settop(thread.address, -2);
    }

    public final VarArg call(LuaThread thread, VarArg args) {
        thread.checkIsAlive();
        LuaBindings.rawgeti(thread.address, LuaBindings.REGISTRYINDEX, LuaState.RIDX_ERROR_HANDLER);
        thread.pushValue(this);
        args.push(thread);
        int code = LuaBindings.pcallk(thread.address, args.size(), LuaBindings.MULTRET, -2 - args.size(), 0, MemorySegment.NULL);
        LuaException.checkError(code, thread);
        VarArg results = VarArg.collect(thread, LuaBindings.gettop(thread.address) - 1);
        LuaBindings.settop(thread.address, -2);
        return results;
    }

    public boolean isNil() {
        return false;
    }

    public boolean isNumber() {
        return false;
    }

    public boolean isInteger() {
        return false;
    }

    public boolean toBoolean() {
        return true;
    }

    public double toNumber() {
        throw new UnsupportedOperationException();
    }

    public int toInteger() {
        throw new UnsupportedOperationException();
    }

    abstract void push(LuaThread thread);

    public abstract ValueType getType();
}

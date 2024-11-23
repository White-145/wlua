package me.white.wlua;

import java.lang.foreign.Arena;
import java.util.*;

public sealed abstract class LuaValue permits BooleanValue, ListValue, LuaThread, NilValue, NumberValue, RefValue, StringValue, UserDataValue {
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

    public static UserDataValue userdata(Object value) {
        return new UserDataValue(value);
    }

    public static FunctionValue load(LuaThread thread, String chunk) {
        try (Arena arena = Arena.ofConfined()) {
            LuaBindings.auxiliaryLoadstring(thread.address, arena.allocateFrom(chunk));
        }
        FunctionValue function = (FunctionValue)from(thread, -1);
        LuaBindings.settop(thread.address, -2);
        return function;
    }

    public static FunctionValue fromFunction(LuaThread thread, JavaFunction function) {
        thread.checkIsAlive();
        ObjectRegistry.pushObject(thread, function);
        LuaBindings.pushcclosure(thread.address, LuaState.RUN_FUNCTION, 1);
        LuaValue value = from(thread, -1);
        LuaBindings.settop(thread.address, -2);
        return (FunctionValue)value;
    }

    public static TableValue fromMap(LuaThread thread, Map<LuaValue, LuaValue> map) {
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

    public static IntegerValue index(int index) {
        return new IntegerValue(index + 1);
    }

    public static boolean isNil(Object value) {
        return !(value instanceof LuaValue) || ((LuaValue)value).isNil();
    }

    public boolean isNil() {
        return false;
    }

    public boolean isNumber() {
        return false;
    }

    public boolean toBoolean() {
        return true;
    }

    public double toNumber() {
        throw new UnsupportedOperationException();
    }

    public long toInteger() {
        throw new UnsupportedOperationException();
    }

    abstract void push(LuaThread thread);

    public abstract ValueType getType();
}

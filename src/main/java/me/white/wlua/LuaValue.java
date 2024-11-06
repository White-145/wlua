package me.white.wlua;

import java.util.*;

public sealed abstract class LuaValue permits BooleanValue, ListValue, LuaState, RefValue, NilValue, NumberValue, StringValue, UserData {
    static LuaValue from(LuaState state, int index) {
        return state.fromStack(index);
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

    public static FunctionValue reference(LuaState state, JavaFunction value) {
        return state.reference(value);
    }

    public static TableValue reference(LuaState state, Map<LuaValue, LuaValue> value) {
        return state.reference(value);
    }

    public static ListValue reference(LuaState state, List<LuaValue> list) {
        Map<LuaValue, LuaValue> map = new HashMap<>();
        for (int i = 0; i < list.size(); ++i) {
            map.put(LuaValue.index(i), list.get(i));
        }
        return state.reference(map).getList();
    }

    public static IntegerValue index(int index) {
        return new IntegerValue(index + 1);
    }

    public static NilValue nil() {
        return NilValue.INSTANCE;
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
}

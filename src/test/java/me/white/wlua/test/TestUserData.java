package me.white.wlua.test;

import me.white.wlua.*;

public class TestUserData extends UserData {
    private LuaValue qux = LuaValue.nil();
    public LuaValue bar = LuaValue.of("baz");

    @LuaFunction("foo")
    public VarArg foo(LuaState state, VarArg args) {
        if (args.size() == 1 && args.get(0) instanceof NumberValue) {
            return new VarArg(new NumberValue(99 + ((NumberValue)args.get(0)).getNumber()));
        }
        return new VarArg(LuaValue.of(99));
    }

    @LuaMetaMethod(MetaMethodType.ADD)
    public LuaValue add(LuaState state, LuaValue value) {
        if (!(value instanceof NumberValue)) {
            return LuaValue.nil();
        }
        return new NumberValue(((NumberValue)value).getNumber() * 2);
    }

    @LuaMetaMethod(MetaMethodType.CALL)
    public VarArg call(LuaState state, Object args1) {
        VarArg args = (VarArg)args1;
        double sum = 0;
        if (args.get(0) instanceof NumberValue) {
            sum += ((NumberValue)args.get(0)).getNumber();
        }
        if (args.get(1) instanceof NumberValue) {
            sum += ((NumberValue)args.get(1)).getNumber();
        }
        return new VarArg(LuaValue.of(sum));
    }

    @LuaMetaMethod(MetaMethodType.LENGTH)
    public StringValue length(LuaState state) {
        return LuaValue.of("length");
    }

    @LuaField("bar")
    public LuaValue getBar(LuaState state) {
        if (bar instanceof NumberValue) {
            return new NumberValue(((NumberValue)bar).getNumber() * 2);
        }
        return bar;
    }

    @LuaField("bar")
    public void setBar(LuaState state, LuaValue value) {
        bar = value;
    }

    @LuaMetaMethod(MetaMethodType.INDEX)
    public LuaValue index(LuaState state, LuaValue key) {
        if (!key.equals(LuaValue.of("qux"))) {
            return null;
        }
        return qux;
    }

    @LuaMetaMethod(MetaMethodType.NEW_INDEX)
    public void newIndex(LuaState state, LuaValue key, LuaValue value) {
        if (!key.equals(LuaValue.of("qux"))) {
            return;
        }
        qux = value;
    }

    @Override
    public String getName() {
        return "Qwerty Data";
    }
}
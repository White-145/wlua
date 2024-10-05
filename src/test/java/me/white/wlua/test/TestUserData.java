package me.white.wlua.test;

import me.white.wlua.*;

public class TestUserData extends UserData {
    private LuaValue qux = LuaValue.nil();
    public LuaValue bar = LuaValue.valueOf("baz");

    @LuaField("spam")
    public LuaValue spam = LuaValue.valueOf(8);

    @LuaField(value = "bacon", type = FieldType.READ_ONLY)
    public LuaValue bacon = LuaValue.valueOf(42);

    @LuaField(value = "eggs", type = FieldType.WRITE_ONLY)
    public LuaValue eggs = LuaValue.valueOf(-1);

    @LuaFunction("foo")
    public VarArg foo(LuaState state, VarArg args) {
        if (args.get(0) instanceof NumberValue) {
            return new VarArg(LuaValue.valueOf(99 + args.get(0).getNumber()));
        }
        return new VarArg(LuaValue.valueOf(99));
    }

    @LuaMetaMethod(MetaMethodType.ADD)
    public LuaValue add(LuaState state, LuaValue value) {
        if (!(value instanceof NumberValue)) {
            return LuaValue.nil();
        }
        return new NumberValue(value.getNumber() * 2);
    }

    @LuaMetaMethod(MetaMethodType.CALL)
    public VarArg call(LuaState state, Object args1) {
        VarArg args = (VarArg)args1;
        double sum = 0;
        if (args.get(0) instanceof NumberValue) {
            sum += args.get(0).getNumber();
        }
        if (args.get(1) instanceof NumberValue) {
            sum += args.get(1).getNumber();
        }
        return new VarArg(LuaValue.valueOf(sum));
    }

    @LuaMetaMethod(MetaMethodType.LENGTH)
    public StringValue length(LuaState state) {
        return LuaValue.valueOf("length");
    }

    @LuaMetaMethod(MetaMethodType.CLOSE)
    public void close(LuaState state, LuaValue error) {

    }

    @LuaAccessor("bar")
    public LuaValue getBar(LuaState state) {
        if (bar instanceof NumberValue) {
            return new NumberValue(bar.getNumber() * 2);
        }
        return bar;
    }

    @LuaAccessor(value = "bar", type = AccessorType.SETTER)
    public void setBar(LuaState state, LuaValue value) {
        bar = value;
    }

    @LuaMetaMethod(MetaMethodType.INDEX)
    public LuaValue index(LuaState state, LuaValue key) {
        if (!key.getString().equals("qux")) {
            return null;
        }
        return qux;
    }

    @LuaMetaMethod(MetaMethodType.NEW_INDEX)
    public void newIndex(LuaState state, LuaValue key, LuaValue value) {
        if (!key.getString().equals("qux")) {
            return;
        }
        qux = value;
    }

    @Override
    public String getName() {
        return "Qwerty Data";
    }
}
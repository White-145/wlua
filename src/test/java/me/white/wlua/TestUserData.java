package me.white.wlua;

public class TestUserData extends UserData {
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
        if (args.size() != 2) {
            return new VarArg();
        }
        LuaValue first = args.get(0);
        LuaValue second = args.get(1);
        if (!(first instanceof NumberValue) || !(second instanceof NumberValue)) {
            return new VarArg();
        }
        return new VarArg(new NumberValue(((NumberValue)first).getNumber() + ((NumberValue)second).getNumber()));
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
}
package me.white.wlua.test;

import me.white.wlua.LuaValue;
import me.white.wlua.MetaMethodType;
import me.white.wlua.UserData;
import me.white.wlua.VarArg;
import me.white.wlua.annotation.*;

public class TestUserData extends UserData {
    @LuaField("bar")
    public LuaValue bar = LuaValue.of("baz");

    @LuaFunction("foo")
    public VarArg foo(VarArg args) {
        return new VarArg();
    }

    @LuaMetaMethod(MetaMethodType.CALL)
    public VarArg call(VarArg values) {
        for (LuaValue value : values.getValues()) {
            System.out.println(value);
        }
        return new VarArg();
    }

    // TODO: ↓↓↓ looks really nice

    // @LuaField("bar")
    // public LuaValue getBar();

    // @LuaField("bar")
    // public void setBar(LuaValue bar);
}
package me.white.wlua.test;

import me.white.wlua.*;

public class TestLibrary extends Library {
    public static final Library LIBRARY = new TestLibrary();

    private TestLibrary() { }

    @LuaFunction("count")
    public static VarArg amount(LuaState state, VarArg args) {
        return new VarArg(new NumberValue(args.size()));
    }

    @LuaFunction("greet")
    public static VarArg greet(LuaState state, VarArg args) {
        String greeting;
        if (args.size() == 0) {
            greeting = "Hello!";
        } else if (args.size() == 1) {
            greeting = "Hello, " + stringify(args.get(0)) + "!";
        } else {
            String[] separated = new String[args.size() - 1];
            for (int i = 0; i < args.size() - 1; ++i) {
                separated[i] = stringify(args.get(i));
            }
            greeting = "Hello, " + String.join(", ", separated) + " and " + stringify(args.get(args.size() - 1)) + "!";
        }
        return new VarArg(new StringValue(greeting));
    }

    @LuaFunction("nothing")
    public static VarArg nothing(LuaState state, VarArg args) {
        return VarArg.of();
    }

    @LuaFunction("yield")
    public static VarArg yield(LuaState state, VarArg args) {
        return state.yield(args);
    }

    private static String stringify(LuaValue value) {
        if (value instanceof StringValue) {
            return ((StringValue)value).getString();
        }
        return value.toString();
    }
}

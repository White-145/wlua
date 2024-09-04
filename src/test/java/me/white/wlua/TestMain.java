package me.white.wlua;

import java.util.HashMap;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.LongStream;

public class TestMain {
    public static void main(String[] args) {
        try (LuaState state = new LuaState()) {
            testValues(state);
            testConcurrencyRef(state);
            testLibraries(state);
        }
    }

    private static void measure(Consumer<Integer> func, int amount) {
        long[] times = new long[amount];
        for (int i = 0; i < amount; ++i) {
            long start = System.nanoTime();
            func.accept(i);
            long end = System.nanoTime();
            times[i] = end - start;
            double avg = LongStream.of(times).limit(i + 1).average().orElseThrow() / 1e6;
            System.out.print(Math.round(avg * 1000) / 1000.0 + "ms (" + (i + 1) + ")\r");
            System.out.flush();
        }
    }

    private static void testValues(LuaState state) {
        BooleanValue bool = LuaValue.of(true);
        state.setGlobal(bool, "value");
        LuaValue value = state.getGlobal("value");
        assert value.equals(bool);
        assert ((BooleanValue)value).getBoolean();

        IntegerValue integer = LuaValue.of(21);
        state.setGlobal(integer, "value");
        value = state.getGlobal("value");
        assert value.equals(integer);
        assert ((IntegerValue)value).getInteger() == 21;

        NumberValue number = LuaValue.of(182.4);
        state.setGlobal(number, "value");
        value = state.getGlobal("value");
        assert value.equals(number);
        assert ((NumberValue)value).getNumber() == 182.4;

        StringValue str = LuaValue.of("test string");
        state.setGlobal(str, "value");
        value = state.getGlobal("value");
        assert value.equals(str);
        assert ((StringValue)value).getString().equals("test string");

        NilValue nil = LuaValue.of();
        state.setGlobal(nil, "value");
        value = state.getGlobal("value");
        assert value.equals(nil);
        assert state.getGlobal("not existant").equals(nil);

        testTable(state);
        testFunction(state);
        testUserData(state);
    }

    private static void testTable(LuaState state) {
        TableRefValue table = new TableValue().toReference(state);
        assert table.isEmpty();
        table.put(LuaValue.of("kind value"), LuaValue.of(true));
        table.put(LuaValue.of("evil value"), LuaValue.of(false));
        assert table.size() == 2;
        assert table.get(LuaValue.of("kind value")).equals(state, LuaValue.of(true));
        assert table.get(LuaValue.of("evil value")).equals(state, LuaValue.of(false));
        // setting a key to nil is just a fancy way of removing it
        table.put(LuaValue.of("evil value"), LuaValue.nil());
        assert table.get(LuaValue.of("evil value")).equals(state, LuaValue.nil());
        assert table.size() == 1;

        HashMap<LuaValue, LuaValue> map = new HashMap<>();
        map.put(LuaValue.of(false), LuaValue.of(true));
        map.put(LuaValue.of(5), LuaValue.of("10"));
        table.putAll(map);
        assert table.size() == 3;
        assert table.get(LuaValue.of(5)).equals(state, LuaValue.of("10"));

        assert table.containsKey(LuaValue.of("kind value"));
        assert !table.containsKey(LuaValue.of("evil value"));
        assert table.containsValue(LuaValue.of(true));
        assert !table.containsValue(LuaValue.of(9.5));

        Set<LuaValue> keys = Set.of(LuaValue.of(false), LuaValue.of("kind value"), LuaValue.of(5));
        assert table.keySet().equals(keys);
        Set<LuaValue> values = Set.of(LuaValue.of(true), LuaValue.of("10"));
        assert table.values().equals(values);

        map = new HashMap<>();
        map.put(LuaValue.of(false), LuaValue.of(true));
        map.put(LuaValue.of("kind value"), LuaValue.of(true));
        map.put(LuaValue.of(5), LuaValue.of("10"));
        assert map.equals(table);

        table.clear();
        table.unref();
    }

    private static void testFunction(LuaState state) {
        FunctionValue func = LuaValue.of((lua, args) -> {
            return new VarArg(args.get(0), LuaValue.nil());
        });
        VarArg ret = func.run(state, new VarArg(LuaValue.of("string"), LuaValue.nil()));
        assert ret.size() == 2;
        LuaValue retValue = ret.get(0);
        assert retValue instanceof StringValue && ((StringValue)retValue).getString().equals("string");
        state.setGlobal(func, "value");
        LuaValue value = state.getGlobal("value");
        assert value instanceof FunctionRefValue;
        ret = ((FunctionRefValue)value).run(state, new VarArg(LuaValue.of("string"), LuaValue.nil()));
        assert ret.size() == 2;
        retValue = ret.get(0);
        assert retValue instanceof StringValue && ((StringValue)retValue).getString().equals("string");
    }

    private static void testUserData(LuaState state) {
        TestUserData test = new TestUserData();
        state.setGlobal(test, "test");
        state.run("value = test(1, 2)");
        assert state.getGlobal("value").equals(LuaValue.of(3));
        state.run("value = test + 5");
        assert state.getGlobal("value").equals(LuaValue.of(10));
        state.run("value = #test");
        assert state.getGlobal("value").equals(LuaValue.of("length"));
        state.run("value = test.bar");
        assert state.getGlobal("value").equals(LuaValue.of("baz"));
        state.run("test.bar = 9");
        state.run("value = test.bar");
        assert state.getGlobal("value").equals(LuaValue.of(18));
        assert test.bar.equals(LuaValue.of(9));
        state.run("value = test.foo(4)");
        assert state.getGlobal("value").equals(LuaValue.of(103));
        state.run("test.qux = 7");
        state.run("value = test.qux");
        assert state.getGlobal("value").equals(LuaValue.of(7));
        state.run("value = test.bat");
        assert state.getGlobal("value").equals(LuaValue.nil());
//        state.run("value = tostring(test)");
//        assert ((StringValue)state.getGlobal("value")).getString().startsWith("Qwerty Data: ");
    }

    private static void testConcurrencyRef(LuaState state) {
        UserData data = new TestUserData();
        for (int j = 0; j < 1000; ++j) {
            state.setGlobal(data, "a");
            state.run("a(123.456, 456.123)");
            state.getGlobal("a");
            state.setGlobal(LuaValue.of((a, b) -> {
                return new VarArg();
            }), "b");
            state.run("b()");
            state.getGlobal("b");
        }
    }

    private static void testLibraries(LuaState state) {
        state.openLib(TestLibrary.LIBRARY);
        state.run("value = count(1, 2, 3)");
        assert state.getGlobal("value").equals(LuaValue.of(3));
        state.run("value = greet('john', 'lisa', 'mark')");
        assert state.getGlobal("value").equals(LuaValue.of("Hello, john, lisa and mark!"));
    }
}

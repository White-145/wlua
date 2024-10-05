package me.white.wlua.test;

import me.white.wlua.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.LongStream;

public class TestMain {
    public static void main(String[] args) {
        // TODO rewrite all of tests to test literally everything
        testValues();
        testTable();
        testFunction();
        testUserData();
        testConcurrencyRef();
        testCoroutines();
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
        System.out.println();
    }

    private static void testValues() {
        try (LuaState state = new LuaState()) {
            BooleanValue bool = LuaValue.valueOf(true);
            state.setGlobal("value", bool);
            LuaValue value = state.getGlobal("value");
            assert value.equals(bool);
            assert value.getBoolean();

            IntegerValue integer = LuaValue.valueOf(21);
            state.setGlobal("value", integer);
            value = state.getGlobal("value");
            assert value.equals(integer);
            assert value.getInteger() == 21;

            NumberValue number = LuaValue.valueOf(182.4);
            state.setGlobal("value", number);
            value = state.getGlobal("value");
            assert value.equals(number);
            assert value.getNumber() == 182.4;

            StringValue str = LuaValue.valueOf("test string");
            state.setGlobal("value", str);
            value = state.getGlobal("value");
            assert value.equals(str);
            assert value.getString().equals("test string");

            NilValue nil = LuaValue.nil();
            state.setGlobal("value", nil);
            value = state.getGlobal("value");
            assert value.equals(nil);
            assert state.getGlobal("not existant").equals(nil);
        }
    }

    private static void testTable() {
        try (LuaState state = new LuaState()) {
            TableRefValue table = new TableLiteralValue().toReference(state);
            assert table.isEmpty();
            table.put(LuaValue.valueOf("kind value"), LuaValue.valueOf(true));
            table.put(LuaValue.valueOf("evil value"), LuaValue.valueOf(false));
            assert table.size() == 2;
            assert table.get(LuaValue.valueOf("kind value")).equals(state, LuaValue.valueOf(true));
            assert table.get(LuaValue.valueOf("evil value")).equals(state, LuaValue.valueOf(false));
            // setting a key to nil is just a fancy way of removing it
            table.put(LuaValue.valueOf("evil value"), LuaValue.nil());
            assert table.get(LuaValue.valueOf("evil value")) == null;
            assert table.size() == 1;

            HashMap<LuaValue, LuaValue> map = new HashMap<>();
            map.put(LuaValue.valueOf(false), LuaValue.valueOf(true));
            map.put(LuaValue.valueOf(5), LuaValue.valueOf("10"));
            table.putAll(map);
            assert table.size() == 3;
            assert table.get(LuaValue.valueOf(5)).equals(state, LuaValue.valueOf("10"));

            assert table.containsKey(LuaValue.valueOf("kind value"));
            assert !table.containsKey(LuaValue.valueOf("evil value"));
            assert table.containsValue(LuaValue.valueOf(true));
            assert !table.containsValue(LuaValue.valueOf(9.5));

            Set<LuaValue> keys = new HashSet<>(Set.of(LuaValue.valueOf(false), LuaValue.valueOf("kind value"), LuaValue.valueOf(5)));
            assert table.keySet().equals(keys);
            Set<LuaValue> values = Set.of(LuaValue.valueOf(true), LuaValue.valueOf("10"));
            assert table.values().equals(values);

            map = new HashMap<>();
            map.put(LuaValue.valueOf(false), LuaValue.valueOf(true));
            map.put(LuaValue.valueOf("kind value"), LuaValue.valueOf(true));
            map.put(LuaValue.valueOf(5), LuaValue.valueOf("10"));
            assert map.equals(table);

            assert table.size() == 3;
            Iterator<TableRefValue.Entry<LuaValue, LuaValue>> iterator = table.entrySet().iterator();
            while (iterator.hasNext()) {
                TableRefValue.Entry<LuaValue, LuaValue> entry = iterator.next();
                assert keys.contains(entry.getKey());
                keys.remove(entry.getKey());
                iterator.remove();
            }
            assert table.isEmpty();

            table.unref();
        }
    }

    private static void testFunction() {
        try (LuaState state = new LuaState()) {
            FunctionLiteralValue func = LuaValue.valueOf((lua, args) -> {
                return new VarArg(args.get(0), args.get(0));
            });
            VarArg ret = func.run(state, new VarArg(LuaValue.valueOf("string"), LuaValue.nil()));
            assert ret.size() == 2;
            LuaValue retValue = ret.get(0);
            assert retValue instanceof StringValue && retValue.getString().equals("string");
            state.setGlobal("value", func);
            LuaValue value = state.getGlobal("value");
            assert value instanceof FunctionRefValue;
            ret = ((FunctionRefValue)value).run(state, new VarArg(LuaValue.valueOf("string"), LuaValue.nil()));
            assert ret.size() == 2;
            retValue = ret.get(0);
            assert retValue instanceof StringValue && retValue.getString().equals("string");
        }
    }

    private static void testUserData() {
        try (LuaState state = new LuaState()) {
            TestUserData test = new TestUserData();
            state.setGlobal("test", test);
            state.run("value = test(1, 2)");
            assert state.getGlobal("value").equals(LuaValue.valueOf(3));
            state.run("value = test + 5");
            assert state.getGlobal("value").equals(LuaValue.valueOf(10));
            state.run("value = #test");
            assert state.getGlobal("value").equals(LuaValue.valueOf("length"));
            state.run("value = test.bar");
            assert state.getGlobal("value").equals(LuaValue.valueOf("baz"));
            state.run("test.bar = 9");
            state.run("value = test.bar");
            assert state.getGlobal("value").equals(LuaValue.valueOf(18));
            assert test.bar.equals(LuaValue.valueOf(9));
            state.run("value = test.foo(4)");
            assert state.getGlobal("value").equals(LuaValue.valueOf(103));
            state.run("test.qux = 7");
            state.run("value = test.qux");
            assert state.getGlobal("value").equals(LuaValue.valueOf(7));
            state.run("value = test.bat");
            assert state.getGlobal("value").isNil();
            state.run("value = test.spam; test.spam = 9000");
            assert state.getGlobal("value").equals(LuaValue.valueOf(8));
            assert test.spam.equals(LuaValue.valueOf(9000));
            state.run("value = test.bacon");
            assert state.getGlobal("value").equals(LuaValue.valueOf(42));
            state.run("test.bacon = 13; value = test.bacon");
            assert state.getGlobal("value").equals(LuaValue.valueOf(42));
            state.run("value = test.eggs; test.eggs = 11");
            assert state.getGlobal("value").isNil();
            assert test.eggs.equals(LuaValue.valueOf(11));

            TestUserData test2 = new TestUserData();
            state.run("value = test.foo(nil)");
            assert state.getGlobal("value").equals(LuaValue.valueOf(99));
        }
    }

    private static void testConcurrencyRef() {
        UserData data = new TestUserData();
        for (int j = 0; j < 1000; ++j) {
            try (LuaState state = new LuaState()) {
                state.setGlobal("a", data);
                state.run("a(123.456, 456.123)");
                state.getGlobal("a");
                state.setGlobal("b", LuaValue.valueOf((a, b) -> {
                    return new VarArg();
                }));
                state.run("b()");
                state.getGlobal("b");
            }
        }
    }

    private static void testCoroutines() {
        try (LuaState state = new LuaState()) {
            state.setGlobal("yield", LuaValue.valueOf((lua, args) -> {
                return lua.yield(new VarArg(LuaValue.valueOf(30)));
            }));
            FunctionRefValue chunk = state.load("""
                    value = 10
                    value = yield(20)
                    """);
            VarArg result = state.start(chunk, new VarArg());
            assert result.size() == 1 && result.get(0).equals(LuaValue.valueOf(30));
            assert state.getGlobal("value").equals(LuaValue.valueOf(10));
            state.resume(new VarArg(LuaValue.valueOf(40)));
            assert state.getGlobal("value").equals(LuaValue.valueOf(40));
            assert !state.isSuspended();
        }
    }

    private static void testThreads() {
        try (LuaState state = new LuaState()) {
            LuaState thread = state.subThread();
            state.setGlobal("yield", LuaValue.valueOf((lua, args) -> {
                return lua.yield();
            }));
            FunctionRefValue chunk = state.load("""
                    value = 10
                    yield()
                    value = 20
                    """);
            state.start(chunk, new VarArg());
            assert state.getGlobal("value").equals(LuaValue.valueOf(10));
            thread.start(chunk, new VarArg());
            state.resume(new VarArg());
            assert !state.isSuspended();
            assert thread.isSuspended();
            assert state.getGlobal("value").equals(LuaValue.valueOf(20));
            assert thread.getGlobal("value").equals(LuaValue.valueOf(10));
            thread.resume(new VarArg());
            assert thread.getGlobal("value").equals(LuaValue.valueOf(20));
        }
    }
}

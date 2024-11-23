package me.white.wlua;

import java.lang.reflect.Field;
import java.util.*;

public class TestMain {
    public static void main(String[] args) {
        testState();
        testProgram();
        testReferences();
        testValues();
        testFunction();
        testTable();
        testList();
        testUserData();
        testCoroutinesAndThreads();
    }

    public static void testState() {
        LuaState state2 = new LuaState();
        LuaState state = new LuaState();
        assert state2.getState() == state2;
        assert state2.getState() != state.getState();
        assert state2.isAlive();
        assert state2.isSubThread(state2);
        state2.checkIsAlive();
        state2.close();
        assert !state2.isAlive();
        assert state.isAlive();
        state.close();
    }

    public static void testProgram() {
        try (LuaState state = new LuaState()) {
            state.run("a = 2 * 3");
            assert state.getGlobal("a").equals(LuaValue.of(6));
            state.setGlobal("a", LuaValue.of(5));
            state.run("""
                    function b(n)
                        if n == 0 then
                            return 1
                        else
                            return n * b(n - 1)
                        end
                    end
                    a = b(a)
                    """);
            LuaValue global = state.getGlobal("a");
            assert global.equals(LuaValue.of(120));
            LuaValue func = state.getGlobal("b");
            assert func instanceof FunctionValue;
            state.setGlobal("c", func);
            state.run("a = c(6)");
            assert ((FunctionValue)func).run(new VarArg(LuaValue.of(3))).get(0).equals(LuaValue.of(6));
            assert state.getGlobal("a").equals(LuaValue.of(720));
            assert state.getGlobalTable().get(LuaValue.of("a")).equals(state.getGlobal("a"));
            state.getGlobalTable().put(LuaValue.of("b"), LuaValue.of(890));
            assert state.getGlobal("b").equals(LuaValue.of(890));
            assert state.run("return -1").size() == 1;
            assert state.run("return 'foo'").get(0).equals(LuaValue.of("foo"));
        }
    }

    public static void testValues() {
        try (LuaState state = new LuaState()) {
            LuaValue value = LuaValue.of(false);
            assert !value.isNil();
            assert !value.toBoolean();
            assert !value.isNumber();
            assert value.toString().equals("false");
            state.setGlobal("a", value);
            LuaValue value1 = state.getGlobal("a");
            assert value1.equals(value);
            assert !value1.isNil();
            assert !value1.toBoolean();
            value = LuaValue.of(10);
            assert value.isNumber();
            assert value.toNumber() == 10.0;
            state.setGlobal("a", value);
            value1 = state.getGlobal("a");
            assert value.equals(value1);
            assert value.equals(LuaValue.of(10.0));
            assert LuaValue.of(10.0).equals(value);
            value = LuaValue.of("foo");
            state.setGlobal("a", value);
            value1 = state.getGlobal("a");
            assert value.equals(value1);
            state.run("a = '5.5'");
            value1 = state.getGlobal("a");
            assert value1 instanceof StringValue;
            assert value1.isNumber();
            assert value1.toNumber() == 5.5;
            value = LuaValue.nil();
            assert value.isNil();
            state.run("""
                    a = function()
                        return nil
                    end
                    """);
            LuaValue ref1 = state.getGlobal("a");
            LuaValue ref2 = state.getGlobal("a");
            assert ref1.equals(ref2);
            assert ref1 == ref2;
        }
    }

    public static void testReferences() {
        LuaState state = new LuaState();
        Map<?, ?> references;
        try {
            Field field = LuaState.class.getDeclaredField("references");
            field.setAccessible(true);
            references = (Map<?, ?>)field.get(state);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        LuaValue.load(state, "a = nil");
        {
            LuaValue a = state.getGlobalTable();
        }
        state.close();
        assert references.isEmpty();
    }

    public static void testFunction() {
        try (LuaState state = new LuaState()) {
            state.setGlobal("b", LuaValue.fromFunction(state, (thread, args) -> {
                assert args.size() == 2;
                assert thread == state;
                LuaValue a = args.get(0);
                assert a instanceof IntegerValue;
                assert a.toInteger() == 4;
                LuaValue b = args.get(1);
                assert b instanceof NumberValue && !(b instanceof IntegerValue);
                assert b.toNumber() == 3.5;
                LuaValue result = LuaValue.of(a.toNumber() * b.toNumber());
                return new VarArg(result);
            }));
            state.run("a = b(4, 3.5)");
            assert state.getGlobal("a").equals(LuaValue.of(14));
        }
    }

    public static void testTable() {
        try (LuaState state = new LuaState()) {
            TableValue table = LuaValue.fromMap(state, Map.of(LuaValue.of("foo"), LuaValue.of(3), LuaValue.of("bar"), LuaValue.of(5), LuaValue.of("baz"), LuaValue.of(15)));
            assert table.size() == 3;
            assert table.containsKey(LuaValue.of("foo"));
            assert !table.containsKey(LuaValue.of(5));
            assert table.containsKey(LuaValue.of("baz"));
            assert table.containsValue(LuaValue.of(15));
            assert !table.containsValue(LuaValue.of("bar"));
            assert table.get(LuaValue.of("foo")).equals(LuaValue.of(3));
            assert table.get(LuaValue.of(5)) == null;
            assert table.put(LuaValue.of("bar"), LuaValue.of(6)).equals(LuaValue.of(5));
            assert table.size() == 3;
            assert table.get(LuaValue.of("bar")).equals(LuaValue.of(6));
            assert table.remove(LuaValue.of("baz")).equals(LuaValue.of(15));
            assert table.size() == 2;
            Object o = table.remove(LuaValue.of("baz"));
            assert o == null;
            table.putAll(Map.of(LuaValue.of("zank"), LuaValue.of(100), LuaValue.of("circ"), LuaValue.of(200), LuaValue.of("poil"), LuaValue.of(6)));
            assert table.size() == 5;
            assert table.get(LuaValue.of("circ")).equals(LuaValue.of(200));
            assert table.containsValue(LuaValue.of(3));
            assert table.get(LuaValue.of("foo")).equals(LuaValue.of(3));
            Set<LuaValue> keySet = new HashSet<>(Set.of(LuaValue.of("foo"), LuaValue.of("bar"), LuaValue.of("zank"), LuaValue.of("circ"), LuaValue.of("poil")));
            assert table.keySet().equals(keySet);
            Set<LuaValue> values = Set.of(LuaValue.of(3), LuaValue.of(6), LuaValue.of(100), LuaValue.of(200));
            assert table.values().equals(values);
            assert table.toMap().equals(Map.of(LuaValue.of("foo"), LuaValue.of(3), LuaValue.of("bar"), LuaValue.of(6), LuaValue.of("zank"), LuaValue.of(100), LuaValue.of("circ"), LuaValue.of(200), LuaValue.of("poil"), LuaValue.of(6)));
            for (Map.Entry<LuaValue, LuaValue> entry : table.entrySet()) {
                assert keySet.contains(entry.getKey());
                assert values.contains(entry.getValue());
                keySet.remove(entry.getKey());
            }
            Iterator<Map.Entry<LuaValue, LuaValue>> iterator = table.entrySet().iterator();
            int times = 0;
            while (iterator.hasNext()) {
                Map.Entry<LuaValue, LuaValue> next = iterator.next();
                iterator.remove();
                times += 1;
                if (times == 3) {
                    break;
                }
            }
            assert table.size() == 2;
            assert times == 3;
            table.clear();
            assert table.isEmpty();
            assert table.keySet().isEmpty();
            assert table.values().isEmpty();
            assert table.entrySet().isEmpty();

        }
    }

    private static void printList(ListValue list, String prefix) {
        int size = list.size();
        System.out.println("List of size " + size + ": (" + prefix + ")");
        for (int i = 0; i < size; ++i) {
            System.out.println(i + ": " + list.get(i));
        }
    }

    public static void testList() {
        try (LuaState state = new LuaState()) {
            TableValue table = LuaValue.fromMap(state, new HashMap<>(Map.of(LuaValue.of("foo"), LuaValue.of(7), LuaValue.of("bar"), LuaValue.of(22), LuaValue.index(0), LuaValue.of(3), LuaValue.index(1), LuaValue.of(10), LuaValue.index(3), LuaValue.of(94))));
            assert table.getList().getTable() == table;
            ListValue list = table.getList();
            assert list.size() == 2;
            assert list.get(0).equals(LuaValue.of(3));
            assert list.get(1).equals(LuaValue.of(10));
            assert list.get(3) == null;
            assert Arrays.equals(list.toArray(), new LuaValue[]{ LuaValue.of(3), LuaValue.of(10) });
            assert !list.contains(LuaValue.of(93));
            assert list.contains(LuaValue.of(3));
            list.add(LuaValue.of(31));
            assert list.size() == 4;
            assert list.get(2).equals(LuaValue.of(31));
            assert list.get(3).equals(LuaValue.of(94));
            assert table.containsKey(LuaValue.index(2));
            boolean bl = list.remove(LuaValue.of(0));
            assert !bl;
            bl = list.remove(LuaValue.of(3));
            assert bl;
            assert !list.isEmpty();
            assert list.size() == 3;
            assert table.get(LuaValue.index(3)) == null;
            assert list.get(1).equals(LuaValue.of(31));
            list.set(0, LuaValue.of(-1));
            assert list.getFirst().equals(LuaValue.of(-1));
            assert list.size() == 3;
            list.addFirst(LuaValue.of(8));
            assert Arrays.equals(list.toArray(new LuaValue[6]), new LuaValue[]{ LuaValue.of(8), LuaValue.of(-1), LuaValue.of(31), LuaValue.of(94), null, null });
            list.remove(2);
            assert list.size() == 3;
            assert list.get(2).equals(LuaValue.of(94));
            assert list.indexOf(LuaValue.of(-1)) == 1;
            int i = list.indexOf(LuaValue.of(7));
            assert i == -1;
            assert list.lastIndexOf(LuaValue.of(22)) == -1;
            List<LuaValue> sup = new ArrayList<>(List.of(LuaValue.of(-1), LuaValue.of(94)));
            assert list.containsAll(sup);
            sup.add(LuaValue.of(7));
            assert !list.containsAll(sup);
            list.addAll(sup);
            assert Arrays.equals(list.toArray(), new LuaValue[]{ LuaValue.of(8), LuaValue.of(-1), LuaValue.of(94), LuaValue.of(-1), LuaValue.of(94), LuaValue.of(7) });
            list.retainAll(sup);
            assert Arrays.equals(list.toArray(), new LuaValue[]{ LuaValue.of(-1), LuaValue.of(94), LuaValue.of(-1), LuaValue.of(94), LuaValue.of(7) });
            assert list.indexOf(LuaValue.of(-1)) == 0;
            List<LuaValue> hum = new ArrayList<>(List.of(LuaValue.of(-1), LuaValue.of(7)));
            list.removeAll(hum);
            assert Arrays.equals(list.toArray(), new LuaValue[]{ LuaValue.of(94), LuaValue.of(94) });
            assert list.lastIndexOf(LuaValue.of(94)) == 1;
            int times = 0;
            for (LuaValue value : list) {
                assert value.equals(LuaValue.of(94));
                times += 1;
            }
            assert times == 2;
            Iterator<LuaValue> iterator = list.iterator();
            while (iterator.hasNext()) {
                LuaValue value = iterator.next();
                iterator.remove();
            }
            assert list.isEmpty();
            list.add(LuaValue.of(-100));
            list.add(LuaValue.of(-99));
            list.add(LuaValue.of(-97));
            list.add(LuaValue.of(-96));
            ListIterator<LuaValue> listIterator = list.listIterator(1);
            assert listIterator.hasPrevious();
            assert listIterator.hasNext();
            LuaValue previous = listIterator.previous();
            assert previous.equals(LuaValue.of(-100));
            assert listIterator.nextIndex() == 0;
            listIterator.remove();
            assert listIterator.nextIndex() == 0;
            listIterator.next();
            listIterator.next();
            assert listIterator.nextIndex() == 2;
            listIterator.add(LuaValue.of(-98));
            assert listIterator.nextIndex() == 3;
            previous = listIterator.previous();
            assert previous.equals(LuaValue.of(-98));
            list.clear();
            assert list.isEmpty();
            assert table.get(LuaValue.of("foo")).equals(LuaValue.of(7));
            assert table.get(LuaValue.of("bar")).equals(LuaValue.of(22));
        }
    }

    public static void testUserData() {
        try (LuaState state = new LuaState()) {
            state.setGlobal("ud", LuaValue.userdata(new TestUserData()));
            Object object = ((UserDataValue)state.getGlobal("ud")).getValue();
            assert object instanceof TestUserData;
        }
    }

    public static void testCoroutinesAndThreads() {
        try (LuaState state = new LuaState()) {
            state.setGlobal("a", LuaValue.fromFunction(state, (lua, args) -> {
                assert lua.isYieldable();
                return lua.yield(VarArg.empty());
            }));
            LuaThread thread = state.subThread();
            state.start(LuaValue.load(state, "a(); b = 8"), new VarArg());
            thread.start(LuaValue.load(state, "b = 1; a(); b = 2"), new VarArg());
            assert thread.getGlobal("b").equals(LuaValue.of(1));
            assert thread.isSuspended();
            LuaThread thread2 = state.subThread();
            assert !thread2.isSuspended();
            thread2.start(LuaValue.load(state, "b = 3; a(); b = 0"), new VarArg());
            assert state.getGlobal("b").equals(LuaValue.of(3));
            thread.resume(new VarArg());
            assert state.getGlobal("b").equals(LuaValue.of(2));
            assert !thread.isSuspended();
            state.resume(new VarArg());
            assert state.getGlobal("b").equals(LuaValue.of(8));
            assert !state.isSuspended();
            assert thread2.isSuspended();
            thread2.resume(new VarArg());
            assert state.getGlobal("b").equals(LuaValue.of(0));
        }
    }

    private static class TestUserData { }
}

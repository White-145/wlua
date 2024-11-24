package me.white.wlua;

import java.lang.reflect.Field;
import java.util.*;

public class TestMain {
    // TODO: UserData identities, Reference value identities, Metatables
    
    private static void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError();
        }
    }

    public static void main(String[] args) {
        System.out.println("testing state");
        testState();
        System.out.println("testing program");
        testProgram();
        System.out.println("testing references");
        testReferences();
        System.out.println("testing values");
        testValues();
        System.out.println("testing function");
        testFunction();
        System.out.println("testing table");
        testTable();
        System.out.println("testing list");
        testList();
        System.out.println("testing userdata");
        testUserData();
        System.out.println("testing coroutines and threads");
        testCoroutinesAndThreads();
    }

    public static void testState() {
        LuaState state2 = new LuaState();
        LuaState state = new LuaState();
        assertTrue(state2.getState() == state2);
        assertTrue(state2.getState() != state.getState());
        assertTrue(state2.isAlive());
        assertTrue(state2.isSubThread(state2));
        state2.checkIsAlive();
        state2.close();
        assertTrue(!state2.isAlive());
        assertTrue(state.isAlive());
        state.close();
    }

    public static void testProgram() {
        try (LuaState state = new LuaState()) {
            state.run("a = 2 * 3");
            assertTrue(state.getGlobal("a").equals(LuaValue.of(6)));
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
            assertTrue(global.equals(LuaValue.of(120)));
            LuaValue func = state.getGlobal("b");
            assertTrue(func instanceof FunctionValue);
            state.setGlobal("c", func);
            state.run("a = c(6)");
            assertTrue(((FunctionValue)func).run(new VarArg(LuaValue.of(3))).get(0).equals(LuaValue.of(6)));
            assertTrue(state.getGlobal("a").equals(LuaValue.of(720)));
            assertTrue(state.getGlobalTable().get(LuaValue.of("a")).equals(state.getGlobal("a")));
            state.getGlobalTable().put(LuaValue.of("b"), LuaValue.of(890));
            assertTrue(state.getGlobal("b").equals(LuaValue.of(890)));
            assertTrue(state.run("return -1").size() == 1);
            assertTrue(state.run("return 'foo'").get(0).equals(LuaValue.of("foo")));
        }
    }

    public static void testValues() {
        try (LuaState state = new LuaState()) {
            LuaValue value = LuaValue.of(false);
            assertTrue(!value.isNil());
            assertTrue(!value.toBoolean());
            assertTrue(!value.isNumber());
            assertTrue(value.toString().equals("false"));
            state.setGlobal("a", value);
            LuaValue value1 = state.getGlobal("a");
            assertTrue(value1.equals(value));
            assertTrue(!value1.isNil());
            assertTrue(!value1.toBoolean());
            value = LuaValue.of(10);
            assertTrue(value.isNumber());
            assertTrue(value.toNumber() == 10.0);
            state.setGlobal("a", value);
            value1 = state.getGlobal("a");
            assertTrue(value.equals(value1));
            assertTrue(value.equals(LuaValue.of(10.0)));
            assertTrue(LuaValue.of(10.0).equals(value));
            value = LuaValue.of("foo");
            state.setGlobal("a", value);
            value1 = state.getGlobal("a");
            assertTrue(value.equals(value1));
            state.run("a = '5.5'");
            value1 = state.getGlobal("a");
            assertTrue(value1 instanceof StringValue);
            assertTrue(value1.isNumber());
            assertTrue(value1.toNumber() == 5.5);
            value = LuaValue.nil();
            assertTrue(value.isNil());
            state.run("""
                    a = function()
                        return nil
                    end
                    """);
            LuaValue ref1 = state.getGlobal("a");
            LuaValue ref2 = state.getGlobal("a");
            assertTrue(ref1.equals(ref2));
            assertTrue(ref1 == ref2);
        }
    }

    public static void testReferences() {
        LuaState state = new LuaState();
        Set<?> references;
        try {
            Field field = LuaState.class.getDeclaredField("references");
            field.setAccessible(true);
            references = (Set<?>)field.get(state);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        LuaValue.load(state, "a = nil");
        {
            LuaValue a = state.getGlobalTable();
        }
        state.close();
        assertTrue(references.isEmpty());
    }

    public static void testFunction() {
        try (LuaState state = new LuaState()) {
            state.setGlobal("b", LuaValue.fromFunction(state, (thread, args) -> {
                assertTrue(args.size() == 2);
                assertTrue(thread == state);
                LuaValue a = args.get(0);
                assertTrue(a instanceof IntegerValue);
                assertTrue(a.toInteger() == 4);
                LuaValue b = args.get(1);
                assertTrue(b instanceof NumberValue && !(b instanceof IntegerValue));
                assertTrue(b.toNumber() == 3.5);
                LuaValue result = LuaValue.of(a.toNumber() * b.toNumber());
                return new VarArg(result);
            }));
            state.run("a = b(4, 3.5)");
            assertTrue(state.getGlobal("a").equals(LuaValue.of(14)));
        }
    }

    public static void testTable() {
        try (LuaState state = new LuaState()) {
            TableValue table = LuaValue.fromMap(state, Map.of(LuaValue.of("foo"), LuaValue.of(3), LuaValue.of("bar"), LuaValue.of(5), LuaValue.of("baz"), LuaValue.of(15)));
            assertTrue(table.size() == 3);
            assertTrue(table.containsKey(LuaValue.of("foo")));
            assertTrue(!table.containsKey(LuaValue.of(5)));
            assertTrue(table.containsKey(LuaValue.of("baz")));
            assertTrue(table.containsValue(LuaValue.of(15)));
            assertTrue(!table.containsValue(LuaValue.of("bar")));
            assertTrue(table.get(LuaValue.of("foo")).equals(LuaValue.of(3)));
            assertTrue(table.get(LuaValue.of(5)) == null);
            assertTrue(table.put(LuaValue.of("bar"), LuaValue.of(6)).equals(LuaValue.of(5)));
            assertTrue(table.size() == 3);
            assertTrue(table.get(LuaValue.of("bar")).equals(LuaValue.of(6)));
            assertTrue(table.remove(LuaValue.of("baz")).equals(LuaValue.of(15)));
            assertTrue(table.size() == 2);
            Object o = table.remove(LuaValue.of("baz"));
            assertTrue(o == null);
            table.putAll(Map.of(LuaValue.of("zank"), LuaValue.of(100), LuaValue.of("circ"), LuaValue.of(200), LuaValue.of("poil"), LuaValue.of(6)));
            assertTrue(table.size() == 5);
            assertTrue(table.get(LuaValue.of("circ")).equals(LuaValue.of(200)));
            assertTrue(table.containsValue(LuaValue.of(3)));
            assertTrue(table.get(LuaValue.of("foo")).equals(LuaValue.of(3)));
            Set<LuaValue> keySet = new HashSet<>(Set.of(LuaValue.of("foo"), LuaValue.of("bar"), LuaValue.of("zank"), LuaValue.of("circ"), LuaValue.of("poil")));
            assertTrue(table.keySet().equals(keySet));
            Set<LuaValue> values = Set.of(LuaValue.of(3), LuaValue.of(6), LuaValue.of(100), LuaValue.of(200));
            assertTrue(table.values().equals(values));
            assertTrue(table.toMap().equals(Map.of(LuaValue.of("foo"), LuaValue.of(3), LuaValue.of("bar"), LuaValue.of(6), LuaValue.of("zank"), LuaValue.of(100), LuaValue.of("circ"), LuaValue.of(200), LuaValue.of("poil"), LuaValue.of(6))));
            for (Map.Entry<LuaValue, LuaValue> entry : table.entrySet()) {
                assertTrue(keySet.contains(entry.getKey()));
                assertTrue(values.contains(entry.getValue()));
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
            assertTrue(table.size() == 2);
            assertTrue(times == 3);
            table.clear();
            assertTrue(table.isEmpty());
            assertTrue(table.keySet().isEmpty());
            assertTrue(table.values().isEmpty());
            assertTrue(table.entrySet().isEmpty());

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
            assertTrue(table.getList().getTable() == table);
            ListValue list = table.getList();
            assertTrue(list.size() == 2);
            assertTrue(list.get(0).equals(LuaValue.of(3)));
            assertTrue(list.get(1).equals(LuaValue.of(10)));
            assertTrue(list.get(3) == null);
            assertTrue(Arrays.equals(list.toArray(), new LuaValue[]{ LuaValue.of(3), LuaValue.of(10) }));
            assertTrue(!list.contains(LuaValue.of(93)));
            assertTrue(list.contains(LuaValue.of(3)));
            list.add(LuaValue.of(31));
            assertTrue(list.size() == 4);
            assertTrue(list.get(2).equals(LuaValue.of(31)));
            assertTrue(list.get(3).equals(LuaValue.of(94)));
            assertTrue(table.containsKey(LuaValue.index(2)));
            boolean bl = list.remove(LuaValue.of(0));
            assertTrue(!bl);
            bl = list.remove(LuaValue.of(3));
            assertTrue(bl);
            assertTrue(!list.isEmpty());
            assertTrue(list.size() == 3);
            assertTrue(table.get(LuaValue.index(3)) == null);
            assertTrue(list.get(1).equals(LuaValue.of(31)));
            list.set(0, LuaValue.of(-1));
            assertTrue(list.getFirst().equals(LuaValue.of(-1)));
            assertTrue(list.size() == 3);
            list.addFirst(LuaValue.of(8));
            assertTrue(Arrays.equals(list.toArray(new LuaValue[6]), new LuaValue[]{ LuaValue.of(8), LuaValue.of(-1), LuaValue.of(31), LuaValue.of(94), null, null }));
            list.remove(2);
            assertTrue(list.size() == 3);
            assertTrue(list.get(2).equals(LuaValue.of(94)));
            assertTrue(list.indexOf(LuaValue.of(-1)) == 1);
            int i = list.indexOf(LuaValue.of(7));
            assertTrue(i == -1);
            assertTrue(list.lastIndexOf(LuaValue.of(22)) == -1);
            List<LuaValue> sup = new ArrayList<>(List.of(LuaValue.of(-1), LuaValue.of(94)));
            assertTrue(list.containsAll(sup));
            sup.add(LuaValue.of(7));
            assertTrue(!list.containsAll(sup));
            list.addAll(sup);
            assertTrue(Arrays.equals(list.toArray(), new LuaValue[]{ LuaValue.of(8), LuaValue.of(-1), LuaValue.of(94), LuaValue.of(-1), LuaValue.of(94), LuaValue.of(7) }));
            list.retainAll(sup);
            assertTrue(Arrays.equals(list.toArray(), new LuaValue[]{ LuaValue.of(-1), LuaValue.of(94), LuaValue.of(-1), LuaValue.of(94), LuaValue.of(7) }));
            assertTrue(list.indexOf(LuaValue.of(-1)) == 0);
            List<LuaValue> hum = new ArrayList<>(List.of(LuaValue.of(-1), LuaValue.of(7)));
            list.removeAll(hum);
            assertTrue(Arrays.equals(list.toArray(), new LuaValue[]{ LuaValue.of(94), LuaValue.of(94) }));
            assertTrue(list.lastIndexOf(LuaValue.of(94)) == 1);
            int times = 0;
            for (LuaValue value : list) {
                assertTrue(value.equals(LuaValue.of(94)));
                times += 1;
            }
            assertTrue(times == 2);
            Iterator<LuaValue> iterator = list.iterator();
            while (iterator.hasNext()) {
                LuaValue value = iterator.next();
                iterator.remove();
            }
            assertTrue(list.isEmpty());
            list.add(LuaValue.of(-100));
            list.add(LuaValue.of(-99));
            list.add(LuaValue.of(-97));
            list.add(LuaValue.of(-96));
            ListIterator<LuaValue> listIterator = list.listIterator(1);
            assertTrue(listIterator.hasPrevious());
            assertTrue(listIterator.hasNext());
            LuaValue previous = listIterator.previous();
            assertTrue(previous.equals(LuaValue.of(-100)));
            assertTrue(listIterator.nextIndex() == 0);
            listIterator.remove();
            assertTrue(listIterator.nextIndex() == 0);
            listIterator.next();
            listIterator.next();
            assertTrue(listIterator.nextIndex() == 2);
            listIterator.add(LuaValue.of(-98));
            assertTrue(listIterator.nextIndex() == 3);
            previous = listIterator.previous();
            assertTrue(previous.equals(LuaValue.of(-98)));
            list.clear();
            assertTrue(list.isEmpty());
            assertTrue(table.get(LuaValue.of("foo")).equals(LuaValue.of(7)));
            assertTrue(table.get(LuaValue.of("bar")).equals(LuaValue.of(22)));
        }
    }

    public static void testUserData() {
        try (LuaState state = new LuaState()) {
            state.setGlobal("ud", LuaValue.userdata(new TestUserData()));
            Object object = ((UserDataValue)state.getGlobal("ud")).getValue();
            assertTrue(object instanceof TestUserData);
        }
    }

    public static void testCoroutinesAndThreads() {
        try (LuaState state = new LuaState()) {
            state.setGlobal("a", LuaValue.fromFunction(state, (lua, args) -> {
                assertTrue(lua.isYieldable());
                return lua.yield(VarArg.empty());
            }));
            state.start(LuaValue.load(state, "a(); b = 8"), VarArg.empty());
            LuaThread thread = state.subThread();
            thread.start(LuaValue.load(state, "b = 1; a(); b = 2"), VarArg.empty());
            assertTrue(thread.getGlobal("b").equals(LuaValue.of(1)));
            assertTrue(thread.isSuspended());
            LuaThread thread2 = state.subThread();
            assertTrue(!thread2.isSuspended());
            thread2.start(LuaValue.load(state, "b = 3; a(); b = 0"), VarArg.empty());
            assertTrue(state.getGlobal("b").equals(LuaValue.of(3)));
            thread.resume(VarArg.empty());
            assertTrue(state.getGlobal("b").equals(LuaValue.of(2)));
            assertTrue(!thread.isSuspended());
            state.resume(VarArg.empty());
            assertTrue(state.getGlobal("b").equals(LuaValue.of(8)));
            assertTrue(!state.isSuspended());
            assertTrue(thread2.isSuspended());
            thread2.resume(VarArg.empty());
            assertTrue(state.getGlobal("b").equals(LuaValue.of(0)));
        }
    }

    private static class TestUserData { }
}

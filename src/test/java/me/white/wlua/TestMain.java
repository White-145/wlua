package me.white.wlua;

import java.util.*;

public class TestMain {
    public static void main(String[] args) {
        testState();
        testProgram();
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
        assert LuaInstances.get(0) == state2;
        assert LuaInstances.get(1) == state;
        assert state2.getMainThread() == state2;
        assert state2.getMainThread() != state.getMainThread();
        assert !state2.isClosed();
        assert state2.isSubThread(state2);
        state2.checkIsAlive();
        state2.close();
        assert LuaInstances.get(1) == state;
        assert LuaInstances.add(i -> null) == 0;
        assert LuaInstances.add(i -> null) == 2;
        LuaInstances.remove(0);
        LuaInstances.remove(2);
        assert state2.isClosed();
        try {
            state2.checkIsAlive();
            assert false;
        } catch (IllegalStateException ignored) { }
        assert !state.isClosed();
        state.close();
        assert LuaInstances.add(i -> null) == 0;
        assert LuaInstances.add(i -> null) == 1;
        LuaInstances.remove(0);
        LuaInstances.remove(1);
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
            assert global.equals(state, LuaValue.of(120));
            LuaValue func = state.getGlobal("b");
            assert func instanceof FunctionRefValue;
            state.setGlobal("c", func);
            state.run("a = c(6)");
            assert ((FunctionRefValue)func).run(state, new VarArg(LuaValue.of(3))).get(0).equals(LuaValue.of(6));
            assert state.getGlobal("a").equals(LuaValue.of(720));
            assert state.run("return -1").size() == 1;
            assert state.run("return 'foo'").get(0).equals(LuaValue.of("foo"));
        }
    }

    public static void testValues() {
        try (LuaState state = new LuaState()) {
            LuaValue value = LuaValue.of(false);
            assert !value.isNil();
            assert !value.getBoolean();
            assert !value.isNumber();
            assert value.getString().equals("false");
            state.setGlobal("a", value);
            LuaValue value1 = state.getGlobal("a");
            assert value1.equals(value);
            assert !value1.isNil();
            assert !value1.getBoolean();
            assert value.equals(state, value1);
            value = LuaValue.of(10);
            assert value.isNumber();
            assert value.getNumber() == 10.0;
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
            assert value1.getNumber() == 5.5;
            value = LuaValue.nil();
            assert value.isNil();
        }
    }

    public static void testFunction() {
        try (LuaState state = new LuaState()) {
            state.setGlobal("b", LuaValue.of((lua, args) -> {
                assert args.size() == 2;
                assert lua == state;
                LuaValue a = args.get(0);
                assert a instanceof IntegerValue;
                assert a.getInteger() == 4;
                LuaValue b = args.get(1);
                assert b instanceof NumberValue && !(b instanceof IntegerValue);
                assert b.getNumber() == 3.5;
                LuaValue result = LuaValue.of(a.getNumber() * b.getNumber());
                return new VarArg(result);
            }));
            state.run("a = b(4, 3.5)");
            assert state.getGlobal("a").equals(LuaValue.of(14));
        }
    }

    public static void testTable() {
        try (LuaState state = new LuaState()) {
            TableRefValue table = LuaValue.of(Map.of(LuaValue.of("foo"), LuaValue.of(3), LuaValue.of("bar"), LuaValue.of(5), LuaValue.of("baz"), LuaValue.of(15))).toReference(state);
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
            assert table.toLiteral().equals(Map.of(LuaValue.of("foo"), LuaValue.of(3), LuaValue.of("bar"), LuaValue.of(6), LuaValue.of("zank"), LuaValue.of(100), LuaValue.of("circ"), LuaValue.of(200), LuaValue.of("poil"), LuaValue.of(6)));
            for (Map.Entry<LuaValue, LuaValue> entry : table.entrySet()) {
                assert keySet.contains(entry.getKey());
                assert values.contains(entry.getValue());
                keySet.remove(entry.getKey());
            }
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

    private static void testList(TableValue table, ListValue list, LuaState state) {
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
        list.clear();
        assert list.isEmpty();
        assert table.get(LuaValue.of("foo")).equals(LuaValue.of(7));
        assert table.get(LuaValue.of("bar")).equals(LuaValue.of(22));
        // TODO iterators
    }

    public static void testList() {
        try (LuaState state = new LuaState()) {
            TableLiteralValue table = LuaValue.of(new HashMap<>(Map.of(LuaValue.of("foo"), LuaValue.of(7), LuaValue.of("bar"), LuaValue.of(22), LuaValue.index(0), LuaValue.of(3), LuaValue.index(1), LuaValue.of(10), LuaValue.index(3), LuaValue.of(94))));
            TableRefValue ref = table.toReference(state);
            assert table.getList().getTable() == table;
            testList(table, table.getList(), state);
            testList(ref, ref.getList(), state);
        }
    }

    public static void testUserData() {
        try (LuaState state = new LuaState()) {
            TestUserData userData = new TestUserData(state);
            state.setGlobal("ud", userData);
            state.run("a = ud + 993");
            assert state.getGlobal("a").equals(LuaValue.of("addition!"));
            state.run("a = ud - (-993)");
            assert state.getGlobal("a").equals(LuaValue.of("subtraction!"));
            state.run("a = ud * 399");
            assert state.getGlobal("a").equals(LuaValue.of("multiplication!"));
            state.run("a = ud / 939");
            assert state.getGlobal("a").equals(LuaValue.of("division!"));
            state.run("a = ud % 393");
            assert state.getGlobal("a").equals(LuaValue.of("modulation?"));
            state.run("a = ud ^ 999");
            assert state.getGlobal("a").equals(LuaValue.of("no xor for you"));
            state.run("a = -ud");
            assert state.getGlobal("a").equals(LuaValue.of("certainly not trying this one"));
            state.run("a = ud // 111");
            assert state.getGlobal("a").equals(LuaValue.of(333));
            state.run("a = ud & 717");
            assert state.getGlobal("a").equals(LuaValue.of(true));
            state.run("a = ud | 171");
            assert state.getGlobal("a").equals(LuaValue.of(false));
            state.run("a = ~ud");
            assert state.getGlobal("a").equals(LuaValue.of("absolutely not"));
            state.run("a = ud << 'shift left'");
            assert state.getGlobal("a").equals(LuaValue.of(-1));
            state.run("a = ud >> 'shift right'");
            assert state.getGlobal("a").equals(LuaValue.of(-2));
            state.run("a = ud .. 'concatenate'");
            assert state.getGlobal("a").equals(LuaValue.of(-3));
            state.run("a = #ud");
            assert state.getGlobal("a").equals(LuaValue.of(-4));
            state.run("a = ud == 'equals!'");
            assert state.getGlobal("a").equals(LuaValue.of(false));
            state.run("a = ud < 'less'");
            assert state.getGlobal("a").equals(LuaValue.of(true));
            state.run("a = ud <= 'less OR equals'");
            assert state.getGlobal("a").equals(LuaValue.of(false));
            state.run("a, b = ud('calling', nil)");
            assert state.getGlobal("a").equals(LuaValue.nil());
            assert state.getGlobal("b").equals(LuaValue.of("wrong number"));
            state.run("a = ud.index");
            assert state.getGlobal("a").equals(LuaValue.of("nil"));
            state.run("ud.new = 'val'");
            // TODO fields and functions
        }
    }

    public static void testCoroutinesAndThreads() {
        try (LuaState state = new LuaState()) {
            state.setGlobal("a", LuaValue.of((lua, args) -> {
                assert lua.isYieldable();
                return lua.yield();
            }));
            LuaState thread = state.subThread();
            state.start(state.load("a(); b = 8"), new VarArg());
            thread.start(state.load("b = 1; a(); b = 2"), new VarArg());
            assert thread.getGlobal("b").equals(LuaValue.of(1));
            assert thread.isSuspended();
            LuaState thread2 = state.subThread();
            assert !thread2.isSuspended();
            thread2.start(state.load("b = 3; a(); b = 0"), new VarArg());
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

    public static class TestUserData extends UserData {
        private LuaState originalState;
        boolean isClosed = false;

        TestUserData(LuaState state) {
            super("Test");
            originalState = state;
        }

        @LuaMetaMethod(MetaMethodType.ADD)
        public LuaValue add(LuaState state, LuaValue value) {
            assert originalState == state;
            assert value.equals(LuaValue.of(993));
            return LuaValue.of("addition!");
        }

        @LuaMetaMethod(MetaMethodType.SUBTRACT)
        public LuaValue subtract(LuaState state, LuaValue value) {
            assert originalState == state;
            assert value.equals(LuaValue.of(-993));
            return LuaValue.of("subtraction!");
        }

        @LuaMetaMethod(MetaMethodType.MULTIPLY)
        public LuaValue multiply(LuaState state, LuaValue value) {
            assert originalState == state;
            assert value.equals(LuaValue.of(399));
            return LuaValue.of("multiplication!");
        }

        @LuaMetaMethod(MetaMethodType.DIVIDE)
        public LuaValue divide(LuaState state, LuaValue value) {
            assert originalState == state;
            assert value.equals(LuaValue.of(939));
            return LuaValue.of("division!");
        }

        @LuaMetaMethod(MetaMethodType.MODULO)
        public LuaValue modulo(LuaState state, LuaValue value) {
            assert originalState == state;
            assert value.equals(LuaValue.of(393));
            return LuaValue.of("modulation?");
        }

        @LuaMetaMethod(MetaMethodType.POWER)
        public LuaValue power(LuaState state, LuaValue value) {
            assert originalState == state;
            assert value.equals(LuaValue.of(999));
            return LuaValue.of("no xor for you");
        }

        @LuaMetaMethod(MetaMethodType.UNARY_MINUS)
        public LuaValue unaryMinus(LuaState state) {
            assert originalState == state;
            return LuaValue.of("certainly not trying this one");
        }

        @LuaMetaMethod(MetaMethodType.INTEGER_DIVIDE)
        public LuaValue integerDivide(LuaState state, LuaValue value) {
            assert originalState == state;
            assert value.equals(LuaValue.of(111));
            return LuaValue.of(333);
        }

        @LuaMetaMethod(MetaMethodType.BINARY_AND)
        public LuaValue binaryAnd(LuaState state, LuaValue value) {
            assert originalState == state;
            assert value.equals(LuaValue.of(717));
            return LuaValue.of(true);
        }

        @LuaMetaMethod(MetaMethodType.BINARY_OR)
        public LuaValue binaryOr(LuaState state, LuaValue value) {
            assert originalState == state;
            assert value.equals(LuaValue.of(171));
            return LuaValue.of(false);
        }

        @LuaMetaMethod(MetaMethodType.BINARY_NOT)
        public LuaValue binaryNot(LuaState state) {
            assert originalState == state;
            return LuaValue.of("absolutely not");
        }

        @LuaMetaMethod(MetaMethodType.SHIFT_LEFT)
        public LuaValue shiftLeft(LuaState state, LuaValue value) {
            assert originalState == state;
            assert value.equals(LuaValue.of("shift left"));
            return LuaValue.of(-1);
        }

        @LuaMetaMethod(MetaMethodType.SHIFT_RIGHT)
        public LuaValue shiftRight(LuaState state, LuaValue value) {
            assert originalState == state;
            assert value.equals(LuaValue.of("shift right"));
            return LuaValue.of(-2);
        }

        @LuaMetaMethod(MetaMethodType.CONCATENATE)
        public LuaValue concatenate(LuaState state, LuaValue value) {
            assert originalState == state;
            assert value.equals(LuaValue.of("concatenate"));
            return LuaValue.of(-3);
        }

        @LuaMetaMethod(MetaMethodType.LENGTH)
        public LuaValue length(LuaState state) {
            assert originalState == state;
            return LuaValue.of(-4);
        }

        @LuaMetaMethod(MetaMethodType.EQUALS)
        public LuaValue equals0(LuaState state, LuaValue value) {
            assert originalState == state;
            assert value.equals(LuaValue.of("equals!"));
            return LuaValue.of(false);
        }

        @LuaMetaMethod(MetaMethodType.LESS_THAN)
        public LuaValue lessThan(LuaState state, LuaValue value) {
            assert originalState == state;
            assert value.equals(LuaValue.of("less"));
            return LuaValue.of(true);
        }

        @LuaMetaMethod(MetaMethodType.LESS_EQUAL)
        public LuaValue lessEqual(LuaState state, LuaValue value) {
            assert originalState == state;
            assert value.equals(LuaValue.of("less OR equals"));
            return LuaValue.of(false);
        }

        @LuaMetaMethod(MetaMethodType.CALL)
        public VarArg call(LuaState state, VarArg args) {
            assert originalState == state;
            assert args.size() == 1;
            assert args.get(0).equals(LuaValue.of("calling"));
            return new VarArg(LuaValue.nil(), LuaValue.of("wrong number"));
        }

        @LuaMetaMethod(MetaMethodType.CLOSE)
        public void close(LuaState state, LuaValue err) {
            assert originalState == state;
            isClosed = true;
        }

        @LuaMetaMethod(MetaMethodType.INDEX)
        public LuaValue index(LuaState state, LuaValue key) {
            assert originalState == state;
            key.equals(LuaValue.of("index"));
            return LuaValue.of("nil");
        }

        @LuaMetaMethod(MetaMethodType.NEW_INDEX)
        public void newIndex(LuaState state, LuaValue key, LuaValue value) {
            assert originalState == state;
            key.equals(LuaValue.of("new"));
            value.equals(LuaValue.of("val"));
        }
    }
}

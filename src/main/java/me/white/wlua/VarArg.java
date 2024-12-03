package me.white.wlua;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;

public class VarArg {
    private static final VarArg EMPTY = new VarArg();
    private final LuaValue[] values;

    public VarArg(LuaValue ...values) {
        int size = 0;
        for (int i = 0; i < values.length; ++i) {
            if (values[i] == null) {
                values[i] = LuaValue.nil();
            } else if (!values[i].isNil()) {
                size = i + 1;
            }
        }
        this.values = conform(values, size);
    }

    public VarArg(Collection<LuaValue> values) {
        this(values.toArray(new LuaValue[0]));
    }

    static VarArg collect(LuaThread thread, int amount) {
        thread.checkIsAlive();
        LuaValue[] values = new LuaValue[amount];
        for (int i = 0; i < amount; ++i) {
            values[i] = LuaValue.from(thread, i - amount);
        }
        LuaBindings.settop(thread.address, -amount - 1);
        return new VarArg(values);
    }

    public static LuaValue[] conform(LuaValue[] values, int size) {
        if (size == values.length) {
            return values;
        }
        LuaValue[] conformed = new LuaValue[size];
        System.arraycopy(values, 0, conformed, 0, Math.min(size, values.length));
        for (int i = values.length; i < size; ++i) {
            conformed[i] = LuaValue.nil();
        }
        return conformed;
    }

    public static VarArg empty() {
        return EMPTY;
    }

    void push(LuaThread thread) {
        thread.checkIsAlive();
        for (LuaValue value : values) {
            thread.pushValue(value);
        }
    }

    public LuaValue[] getValues() {
        return values;
    }

    public LuaValue get(int i) {
        if (i < 0 || i >= values.length) {
            return LuaValue.nil();
        }
        return values[i];
    }

    public int size() {
        return values.length;
    }

    private void check(int i, boolean check, String function, String details) throws LuaException {
        if (!check) {
            throw new LuaException("bad argument #" + i + " to '" + function + "' (" + details + ")");
        }
    }

    public LuaValue check(int i, Predicate<LuaValue> check, String function, String details) throws LuaException {
        LuaValue value = get(i);
        check(i, check.test(value), function, details);
        return value;
    }

    public LuaValue checkValue(int i, ValueType type, String function) throws LuaException {
        return check(i, value -> value.getType() == type, function, "expected " + type);
    }

    public void checkNil(int i, String function) throws LuaException {
        checkValue(i, ValueType.NIL, function);
    }

    public boolean checkBoolean(int i, String function) throws LuaException {
        return checkValue(i, ValueType.BOOLEAN, function).toBoolean();
    }

    public double checkNumber(int i, String function) throws LuaException {
        return check(i, LuaValue::isNumber, function, "expected number").toNumber();
    }

    public int checkInteger(int i, String function) throws LuaException {
        return check(i, LuaValue::isInteger, function, "expected integer").toInteger();
    }

    public String checkString(int i, String function) throws LuaException {
        return checkValue(i, ValueType.STRING, function).toString();
    }

    public byte[] checkBytes(int i, String function) throws LuaException {
        return ((StringValue)checkValue(i, ValueType.STRING, function)).getBytes();
    }

    public TableValue checkTable(int i, String function) throws LuaException {
        return (TableValue)checkValue(i, ValueType.TABLE, function);
    }

    public ListValue checkList(int i, String function) throws LuaException {
        return checkTable(i, function).getList();
    }

    public FunctionValue checkFunction(int i, String function) throws LuaException {
        return (FunctionValue)checkValue(i, ValueType.FUNCTION, function);
    }

    @SuppressWarnings("unchecked")
    public <T extends UserData> T checkUserData(int i, Class<T> clazz, String function, String details) throws LuaException {
        return (T)check(i, value -> clazz.isAssignableFrom(value.getClass()), function, details);
    }

    public LuaThread checkThread(int i, String function) throws LuaException {
        return (LuaThread)checkValue(i, ValueType.THREAD, function);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof VarArg)) {
            return false;
        }
        return Arrays.equals(values, ((VarArg)obj).values);
    }
}

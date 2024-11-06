package me.white.wlua;

import java.util.Collection;
import java.util.function.Predicate;

public class VarArg {
    private LuaValue[] values;

    public VarArg(LuaValue ...values) {
        int lastI = 0;
        for (int i = 0; i < values.length; ++i) {
            if (values[i] == null) {
                values[i] = LuaValue.nil();
            } else if (!values[i].isNil()) {
                lastI = i;
            }
        }
        this.values = conform(values, lastI + 1);
    }

    public VarArg(Collection<LuaValue> values) {
        this(values.toArray(new LuaValue[0]));
    }

    static VarArg collect(LuaState state, int amount) {
        state.checkIsAlive();
        LuaValue[] values = new LuaValue[amount];
        for (int i = 0; i < amount; ++i) {
            values[i] = LuaValue.from(state, i - amount);
        }
        state.pop(amount);
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

    void push(LuaState state) {
        state.checkIsAlive();
        for (LuaValue value : values) {
            state.pushValue(value);
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

    public void check(int i, boolean check, String function, String details) throws LuaException {
        if (!check) {
            throw new LuaException("bad argument #" + i + " to '" + function + "' (" + details + ")");
        }
    }

    public LuaValue check(int i, Predicate<LuaValue> check, String function, String details) throws LuaException {
        LuaValue value = get(i);
        check(i, check.test(value), function, details);
        return value;
    }

    @SuppressWarnings("unchecked")
    public <T extends UserData> T checkUserData(int i, Class<T> clazz, String function, String details) throws LuaException {
        return (T)check(i, value -> clazz.isAssignableFrom(value.getClass()), function, details);
    }

    public LuaValue checkValue(int i, ValueType type, String function) {
        return check(i, value -> value.getType() == type, function, "expected " + type);
    }
}

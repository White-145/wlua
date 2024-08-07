package me.white.wlua;

import java.util.Collection;

public class VarArg {
    private LuaValue[] values;

    public VarArg(LuaValue ...values) {
        this.values = values;
    }

    public VarArg(Collection<LuaValue> values) {
        this(values.toArray(new LuaValue[0]));
    }

    public LuaValue[] getValues() {
        return values;
    }

    public LuaValue get(int i) {
        if (i < 0) {
            i += values.length;
        }
        if (i >= values.length || i < 0) {
            return LuaValue.nil();
        }
        return values[i];
    }

    public int size() {
        return values.length;
    }

    public VarArg conform(int size) {
        if (size == values.length) {
            return this;
        }
        LuaValue[] newValues = new LuaValue[size];
        System.arraycopy(values, 0, newValues, 0, Math.min(size, values.length));
        if (values.length < size) {
            for (int i = values.length; i < size; ++i) {
                newValues[i] = LuaValue.nil();
            }
        }
        values = newValues;
        return this;
    }

    static VarArg collect(LuaState state, int amount) {
        LuaValue[] values = new LuaValue[amount];
        for (int i = 0; i < amount; ++i) {
            values[i] = LuaValue.from(state, i - amount);
        }
        state.pop(amount);
        return new VarArg(values);
    }

    void push(LuaState state) {
        for (LuaValue value : values) {
            value.push(state);
        }
    }
}

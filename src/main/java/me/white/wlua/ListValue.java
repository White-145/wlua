package me.white.wlua;

import java.util.List;

public sealed interface ListValue extends List<LuaValue> permits ListLiteralValue, ListRefValue {
    TableValue getTable();

    // no clue how to implement this
    @Override
    default List<LuaValue> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }
}

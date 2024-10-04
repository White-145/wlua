package me.white.wlua;

import java.util.Map;

public sealed interface TableValue extends Map<LuaValue, LuaValue> permits TableLiteralValue, TableRefValue {
    ListValue getList();
}

package me.white.wlua;

import java.util.Map;

// interface meant to unite literal and reference table values
public sealed interface TableValue extends Map<LuaValue, LuaValue> permits TableLiteralValue, TableRefValue {
    ListValue getList();
}

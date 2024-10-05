package me.white.wlua;

public class LuaException extends RuntimeException {
    public LuaException(String msg) {
        super(msg);
    }

    static void checkError(int code, LuaState state) {
        if (code == LuaNatives.OK || code == LuaNatives.YIELD) {
            return;
        }
        String name = switch (code) {
            case LuaNatives.ERRRUN -> "Run";
            case LuaNatives.ERRSYNTAX -> "Syntax";
            case LuaNatives.ERRMEM -> "Memory";
            case LuaNatives.ERRERR -> "Error";
            default -> "Unknown";
        };
        String msg = LuaValue.from(state, -1).getString();
        state.pop(1);
        throw new LuaException(name + ": " + msg);
    }
}

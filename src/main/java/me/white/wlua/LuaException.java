package me.white.wlua;

public class LuaException extends RuntimeException {
    public LuaException(String msg) {
        super(msg);
    }

    static void checkError(int code, LuaThread thread) {
        if (code == LuaBindings.OK || code == LuaBindings.YIELD) {
            return;
        }
        String name = switch (code) {
            case LuaBindings.ERRRUN -> "Run";
            case LuaBindings.ERRSYNTAX -> "Syntax";
            case LuaBindings.ERRMEM -> "Memory";
            case LuaBindings.ERRERR -> "Error";
            default -> "Unknown";
        };
        String msg = LuaValue.from(thread, -1).toString();
        LuaBindings.settop(thread.address, -2);
        throw new LuaException(name + ": " + msg);
    }
}

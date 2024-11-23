package me.white.wlua;

public class LuaException extends RuntimeException {
    public LuaException(String msg) {
        super(msg);
    }

    static void checkError(int code, LuaState state) {
        if (code == LuaBindings.OK || code == LuaBindings.YIELD) {
            return;
        }
        String name = switch (code) {
            case LuaBindings.ERRRUN -> "run";
            case LuaBindings.ERRSYNTAX -> "syntax";
            case LuaBindings.ERRMEM -> "memory";
            case LuaBindings.ERRERR -> "error";
            default -> "unknown";
        };
        String msg = LuaValue.from(state, -1).toString();
        LuaBindings.settop(state.address, -2);
        throw new LuaException(name + ": " + msg);
    }
}

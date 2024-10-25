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
            case LuaNatives.ERRRUN -> "run";
            case LuaNatives.ERRSYNTAX -> "syntax";
            case LuaNatives.ERRMEM -> "memory";
            case LuaNatives.ERRERR -> "error";
            default -> "unknown";
        };
        String msg = LuaValue.from(state, -1).getString();
        state.pop(1);
        throw new LuaException(name + ": " + msg);
    }
}

package me.white.wlua;

public class LuaException extends RuntimeException {
    public LuaException(String msg) {
        super(msg);
    }

    public static void checkError(int code, LuaState state) {
        if (code == LuaConsts.OK || code == LuaConsts.YIELD) {
            return;
        }
        String name = "Unknown";
        if (code == LuaConsts.ERR_RUN) {
            name = "Run";
        } else if (code == LuaConsts.ERR_SYNTAX) {
            name = "Syntax";
        } else if (code == LuaConsts.ERR_MEM) {
            name = "Memory";
        } else if (code == LuaConsts.ERR_ERR) {
            name = "Error";
        }
        String msg = ((StringValue)LuaValue.from(state, -1)).getString();
        state.pop(1);
        throw new LuaException(name + ": " + msg);
    }
}

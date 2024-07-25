package me.white.wlua;

public class TestMain {
    public static void main(String[] args) {
        try (LuaState state = new LuaState()) {
            String chunk = """
                    print("Hello, World!")
                    """;
            LuaNatives.luaL_openlibs(state.ptr);
            int load = LuaNatives.luaL_loadstring(state.ptr, chunk);
            check(state, load, "load");
            int call = LuaNatives.lua_pcall(state.ptr, 0, 0, 0);
            check(state, call, "call");
        }
    }

    public static void check(LuaState state, int code, String what) {
        if (code != 0) {
            String msg = LuaNatives.lua_tostring(state.ptr, -1);
            throw new IllegalStateException(what + " failed: code " + getErrorCodeName(code) + ", " + msg);
        }
    }

    public static String getErrorCodeName(int code) {
        if (code == LuaConsts.OK) {
            return "Ok";
        }
        if (code == LuaConsts.YIELD) {
            return "Yield";
        }
        if (code == LuaConsts.ERR_RUN) {
            return "Err Run";
        }
        if (code == LuaConsts.ERR_SYNTAX) {
            return "Err Syntax";
        }
        if (code == LuaConsts.ERR_MEM) {
            return "Err Mem";
        }
        if (code == LuaConsts.ERR_ERR) {
            return "Err Err";
        }
        return "Unknown";
    }
}

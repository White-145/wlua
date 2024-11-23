package me.white.wlua;

public enum Library {
    ALL {
        @Override
        void open(LuaState state) {
            LuaBindings.auxiliaryOpenlibs(state.address);
        }
    },
    BASE {
        @Override
        void open(LuaState state) {
            LuaBindings.openBase(state.address);
        }
    },
    COROUTINE {
        @Override
        void open(LuaState state) {
            LuaBindings.openCoroutine(state.address);
        }
    },
    PACKAGE {
        @Override
        void open(LuaState state) {
            LuaBindings.openPackage(state.address);
        }
    },
    STRING {
        @Override
        void open(LuaState state) {
            LuaBindings.openString(state.address);
        }
    },
    UTF8 {
        @Override
        void open(LuaState state) {
            LuaBindings.openUtf8(state.address);
        }
    },
    TABLE {
        @Override
        void open(LuaState state) {
            LuaBindings.openTable(state.address);
        }
    },
    MATH {
        @Override
        void open(LuaState state) {
            LuaBindings.openMath(state.address);
        }
    },
    IO {
        @Override
        void open(LuaState state) {
            LuaBindings.openIo(state.address);
        }
    },
    OS {
        @Override
        void open(LuaState state) {
            LuaBindings.openOs(state.address);
        }
    },
    DEBUG {
        @Override
        void open(LuaState state) {
            LuaBindings.openDebug(state.address);
        }
    };

    void open(LuaState state) {
        throw new UnsupportedOperationException();
    }
}

package me.white.wlua;

import com.badlogic.gdx.utils.SharedLibraryLoader;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

class LuaNatives {
    // @off
    static {
        new SharedLibraryLoader().load("wlua");
        if (!LuaNatives.initBindings()) {
            throw new IllegalStateException("Could not init native bindings.");
        }
    }

    private static int error(long ptr, String message) {
        LuaNatives.pushString(ptr, message);
        return -1;
    }

    private static int fail(long ptr) {
        LuaNatives.pushFail(ptr);
        return 1;
    }

    private static int pushOrFail(LuaState state, Object result) {
        if (result instanceof VarArg) {
            ((VarArg)result).push(state);
            return ((VarArg)result).size();
        }
        if (result instanceof LuaValue) {
            ((LuaValue)result).push(state);
            return 1;
        }
        return fail(state.ptr);
    }

    private static int adopt(int mainId, long ptr) {
        LuaState state = LuaInstances.get(mainId);
        if (state == null) {
            return error(ptr, "error getting lua state");
        }
        state.checkIsAlive();
        return LuaInstances.add((id) -> new LuaState(ptr, id, state));
    }

    private static int invoke(long callerPtr, int stateIndex, Object function, int params) {
        LuaState state = LuaInstances.get(stateIndex);
        if (state == null) {
            return error(callerPtr, "error getting lua state");
        }
        state.checkIsAlive();
        if (!(function instanceof FunctionLiteralValue.Function)) {
            return error(callerPtr, "error invoking java function");
        }
        VarArg args = VarArg.collect(state, params);
        VarArg results;
        try {
            results = ((FunctionLiteralValue.Function)function).run(state, args);
        } catch (LuaException e) {
            return error(callerPtr, e.getMessage());
        }
        return pushOrFail(state, results);
    }

    private static int invokeMeta(long callerPtr, int stateIndex, int metaMethodType) {
        LuaState state = LuaInstances.get(stateIndex);
        if (state == null) {
            return error(callerPtr, "error getting lua state");
        }
        state.checkIsAlive();
        MetaMethodType type = MetaMethodType.values()[metaMethodType];
        LuaValue userdataValue = LuaValue.from(state, 1);
        LuaNatives.remove(state.ptr, 1);
        if (!(userdataValue instanceof UserData)) {
            return error(callerPtr, "error getting userdata");
        }
        UserData userdata = (UserData)userdataValue;
        UserData.FieldData fieldData = UserData.getFieldData(userdata.getClass());
        if (fieldData == null) {
            return error(callerPtr, "error getting field data");
        }
        if (!fieldData.metaMethods.containsKey(type)) {
            return error(callerPtr, "error getting java function");
        }
        Method method = fieldData.metaMethods.get(type);
        if (type.parameters == -1) {
            int total = LuaNatives.getTop(state.ptr);
            VarArg args = VarArg.collect(state, total);
            Object results;
            try {
                results = method.invoke(userdata, state, args);
            } catch (InvocationTargetException | IllegalAccessException ignored) {
                return error(callerPtr, "error invoking java function");
            } catch (LuaException e) {
                return error(callerPtr, e.getMessage());
            }
            return pushOrFail(state, results);
        }
        if (type.doubleReference) {
            LuaNatives.remove(state.ptr, 1);
        }
        Object[] values = new Object[type.parameters + 1];
        values[0] = state;
        for (int i = 0; i < type.parameters; ++i) {
            values[i + 1] = LuaValue.from(state, i - type.parameters);
        }
        state.pop(type.parameters);
        Object results;
        try {
            results = method.invoke(userdata, values);
        } catch (InvocationTargetException | IllegalAccessException ignored) {
            return error(callerPtr, "error invoking java function");
        } catch (LuaException e) {
            return error(callerPtr, e.getMessage());
        }
        if (type.returns == 0) {
            return 0;
        }
        return pushOrFail(state, results);
    }

    public static int index(long callerPtr, int stateIndex) {
        LuaState state = LuaInstances.get(stateIndex);
        if (state == null) {
            return error(callerPtr, "error getting lua state");
        }
        state.checkIsAlive();
        LuaValue userdataValue = LuaValue.from(state, -2);
        LuaValue key = LuaValue.from(state, -1);
        if (!(userdataValue instanceof UserData)) {
            return error(callerPtr, "error getting userdata");
        }
        UserData userdata = (UserData)userdataValue;
        UserData.FieldData fieldData = UserData.getFieldData(userdata.getClass());
        if (fieldData == null) {
            return error(callerPtr, "error getting field data");
        }
        state.pop(2);
        if (key instanceof StringValue) {
            String name = key.getString();
            if (fieldData.readFields.containsKey(name)) {
                Field field = fieldData.readFields.get(name);
                Object results;
                try {
                    results = field.get(userdata);
                } catch (IllegalAccessException ignored) {
                    return error(callerPtr, "error getting field");
                } catch (LuaException e) {
                    return error(callerPtr, e.getMessage());
                }
                return pushOrFail(state, results);
            }
            if (fieldData.functions.containsKey(name)) {
                Method method = fieldData.functions.get(name);
                LuaValue.valueOf((lua, args) -> {
                    Object results;
                    try {
                        results = method.invoke(userdata, lua, args);
                    } catch (IllegalAccessException | InvocationTargetException ignored) {
                        throw new IllegalStateException("Could not invoke java function '" + method.getName() + "'.");
                    }
                    if (results instanceof VarArg) {
                        return (VarArg)results;
                    }
                    return new VarArg(LuaValue.fail());
                }).push(state);
                return 1;
            }
            if (fieldData.getters.containsKey(name)) {
                Method method = fieldData.getters.get(name);
                Object results;
                try {
                    results = method.invoke(userdata, state);
                } catch (IllegalAccessException | InvocationTargetException ignored) {
                    return error(callerPtr, "error invoking java function");
                } catch (LuaException e) {
                    return error(callerPtr, e.getMessage());
                }
                return pushOrFail(state, results);
            }
        }
        if (fieldData.metaMethods.containsKey(MetaMethodType.INDEX)) {
            Method method = fieldData.metaMethods.get(MetaMethodType.INDEX);
            Object results;
            try {
                results = method.invoke(userdata, state, key);
            } catch (IllegalAccessException | InvocationTargetException ignored) {
                return error(callerPtr, "error invoking java function");
            } catch (LuaException e) {
                return error(callerPtr, e.getMessage());
            }
            return pushOrFail(state, results);
        }
        return 0;
    }

    public static int newIndex(long callerPtr, int stateIndex) {
        LuaState state = LuaInstances.get(stateIndex);
        if (state == null) {
            return error(callerPtr, "error getting lua state");
        }
        state.checkIsAlive();
        LuaValue userdataValue = LuaValue.from(state, -3);
        if (!(userdataValue instanceof UserData)) {
            return error(callerPtr, "error getting userdata");
        }
        UserData userdata = (UserData)userdataValue;
        UserData.FieldData fieldData = UserData.getFieldData(userdata.getClass());
        if (fieldData == null) {
            return error(callerPtr, "error getting field data");
        }
        LuaValue key = LuaValue.from(state, -2);
        LuaValue value = LuaValue.from(state, -1);
        state.pop(3);
        if (key instanceof StringValue) {
            String name = key.getString();
            if (fieldData.writeFields.containsKey(name)) {
                Field field = fieldData.writeFields.get(name);
                if (Modifier.isFinal(field.getModifiers())) {
                    return error(callerPtr, "error setting field");
                }
                try {
                    field.set(userdata, value);
                } catch (IllegalAccessException ignored) {
                    return error(callerPtr, "error setting field");
                } catch (LuaException e) {
                    return error(callerPtr, e.getMessage());
                }
                return 1;
            }
            if (fieldData.setters.containsKey(name)) {
                Method method = fieldData.setters.get(name);
                try {
                    method.invoke(userdata, state, value);
                } catch (IllegalAccessException | InvocationTargetException ignored) {
                    return error(callerPtr, "error invoking java function");
                } catch (LuaException e) {
                    return error(callerPtr, e.getMessage());
                }
                return 1;
            }
        }
        if (fieldData.metaMethods.containsKey(MetaMethodType.NEW_INDEX)) {
            Method method = fieldData.metaMethods.get(MetaMethodType.NEW_INDEX);
            try {
                method.invoke(userdata, state, key, value);
            } catch (IllegalAccessException | InvocationTargetException ignored) {
                return error(callerPtr, "error invoking java function");
            } catch (LuaException e) {
                return error(callerPtr, e.getMessage());
            }
            return 1;
        }
        return 0;
    }

    /*JNI
    extern "C" {
        #include "lua.h"
        #include "lualib.h"
        #include "lauxlib.h"
    }
    #include <cstring>

    #define JAVA_STATE_INDEX "state_index"
    #define JAVA_OBJECT_GC "object_gc"
    #define JAVA_BOOLEAN(bool) (bool ? JNI_TRUE : JNI_FALSE)

    #define YIELD_FIELD "yield"

    JavaVM* java_vm = NULL;
    jint env_version;
    jclass natives_class;
    jmethodID invoke_method;
    jmethodID adopt_method;
    jmethodID invoke_meta_method;
    jmethodID throwable_tostring_method;
    jmethodID index_method;
    jmethodID new_index_method;

    int update_env(JNIEnv* env) {
        if (env->GetJavaVM(&java_vm) == 0) {
            env_version = env->GetVersion();
            return 0;
        }
        return -1;
    }

    JNIEnv* get_env(lua_State* L) {
        if (java_vm == NULL) {
            luaL_error(L, "Cannot to get JavaVM.");
            return NULL;
        }
        JNIEnv* env;
        int code = java_vm->GetEnv((void**)&env, env_version);
        if (code != JNI_OK) {
            luaL_error(L, "Cannot get JNIEnv, error code: %d.", code);
            return NULL;
        }
        return env;
    }

    int get_main_thread_id(lua_State* L) {
        lua_pushstring(L, JAVA_STATE_INDEX);
        lua_rawget(L, LUA_REGISTRYINDEX);
        int id = lua_tointeger(L, -1);
        lua_pop(L, 1);
        return id;
    }

    int create_new_id(lua_State* L) {
        int main_id = get_main_thread_id(L);
        JNIEnv* env = get_env(L);
        int new_id = env->CallStaticIntMethod(natives_class, adopt_method, (jint)main_id, (jlong)L);
        lua_pushthread(L);
        lua_pushinteger(L, new_id);
        lua_settable(L, LUA_REGISTRYINDEX);
        return new_id;
    }

    int get_state_index(lua_State* L) {
        if (lua_pushthread(L) == 1) {
            // main thread
            lua_pop(L, 1);
            return (int)get_main_thread_id(L);
        }
        lua_rawget(L, LUA_REGISTRYINDEX);
        if (lua_isnil(L, -1)) {
            // thread was created on lua side
            lua_pop(L, 1);
            return (int)create_new_id(L);
        }
        int state_index = (int)lua_tointeger(L, -1);
        lua_pop(L, 1);
        return state_index;
    }

    int return_or_error(JNIEnv* env, lua_State* L, int ret) {
        if (ret < 0) {
            return lua_error(L);
        }
        jthrowable exception = env->ExceptionOccurred();
        if (!exception) {
            return ret;
        }
        env->ExceptionClear();
        jstring message = (jstring)env->CallObjectMethod(exception, throwable_tostring_method);
        const char* str = env->GetStringUTFChars(message, NULL);
        lua_pushstring(L, str);
        env->ReleaseStringUTFChars(message, str);
        env->DeleteLocalRef((jobject)message);
        return lua_error(L);
    }

    void check_yield(lua_State* L, int results, int required) {
        lua_getfield(L, LUA_REGISTRYINDEX, YIELD_FIELD);
        int should_yield = lua_isboolean(L, -1) && lua_toboolean(L, -1);
        lua_pop(L, 1);
        if (!should_yield) {
            return;
        }
        lua_pushinteger(L, required);
        lua_setfield(L, LUA_REGISTRYINDEX, YIELD_FIELD);
        lua_yield(L, results);
    }

    int object_gc(lua_State* L) {
        jobject* global_ref = (jobject*)luaL_checkudata(L, 1, JAVA_OBJECT_GC);
        JNIEnv* env = get_env(L);
        env->DeleteGlobalRef(*global_ref);
        return 0;
    }

    int function_wrapper(lua_State* L) {
        jobject* function = (jobject*)lua_touserdata(L, lua_upvalueindex(1));
        int state_index = get_state_index(L);
        JNIEnv* env = get_env(L);
        int returns = (int)env->CallStaticIntMethod(natives_class, invoke_method, (jlong)L, (jint)state_index, *function, (jint)lua_gettop(L));
        check_yield(L, returns, -1);
        return return_or_error(env, L, returns);
    }

    int meta_method_wrapper(lua_State* L) {
        int type = lua_tointeger(L, lua_upvalueindex(1));
        int results = lua_tointeger(L, lua_upvalueindex(2));
        int state_index = get_state_index(L);
        JNIEnv* env = get_env(L);
        int returns = env->CallStaticIntMethod(natives_class, invoke_meta_method, (jlong)L, (jint)state_index, (jint)type);
        check_yield(L, returns, results);
        return return_or_error(env, L, returns);
    }

    int index_wrapper(lua_State* L) {
        int state_index = get_state_index(L);
        JNIEnv* env = get_env(L);
        int returns = env->CallStaticIntMethod(natives_class, index_method, (jlong)L, (jint)state_index);
        check_yield(L, returns, 1);
        return return_or_error(env, L, returns);
    }

    int new_index_wrapper(lua_State* L) {
        int state_index = get_state_index(L);
        JNIEnv* env = get_env(L);
        int returns = env->CallStaticIntMethod(natives_class, new_index_method, (jlong)L, (jint)state_index);
        check_yield(L, returns, 0);
        return return_or_error(env, L, returns);
    }
    */

    static native String LUA_COPYRIGHT(); /*
        return env->NewStringUTF(LUA_COPYRIGHT);
    */

    static native String LUA_AUTHORS(); /*
        return env->NewStringUTF(LUA_AUTHORS);
    */

    static native String LUA_VERSION_MAJOR(); /*
        return env->NewStringUTF(LUA_VERSION_MAJOR);
    */

    static native String LUA_VERSION_MINOR(); /*
        return env->NewStringUTF(LUA_VERSION_MINOR);
    */

    static native String LUA_VERSION_RELEASE(); /*
        return env->NewStringUTF(LUA_VERSION_RELEASE);
    */

    static native int LUA_VERSION_NUM(); /*
        return (jint)LUA_VERSION_NUM;
    */

    static native int LUA_VERSION_RELEASE_NUM(); /*
        return (jint)LUA_VERSION_RELEASE_NUM;
    */

    static native String LUA_SIGNATURE(); /*
        return env->NewStringUTF(LUA_SIGNATURE);
    */

    static native int LUA_MULTRET(); /*
        return (jint)LUA_MULTRET;
    */

    static native int LUA_REGISTRYINDEX(); /*
        return (jint)LUA_REGISTRYINDEX;
    */

    static native int LUA_OK(); /*
        return (jint)LUA_OK;
    */

    static native int LUA_YIELD(); /*
        return (jint)LUA_YIELD;
    */

    static native int LUA_ERRRUN(); /*
        return (jint)LUA_ERRRUN;
    */

    static native int LUA_ERRSYNTAX(); /*
        return (jint)LUA_ERRSYNTAX;
    */

    static native int LUA_ERRMEM(); /*
        return (jint)LUA_ERRMEM;
    */

    static native int LUA_ERRERR(); /*
        return (jint)LUA_ERRERR;
    */

    static native int LUA_TNONE(); /*
        return (jint)LUA_TNONE;
    */

    static native int LUA_TNIL(); /*
        return (jint)LUA_TNIL;
    */

    static native int LUA_TBOOLEAN(); /*
        return (jint)LUA_TBOOLEAN;
    */

    static native int LUA_TLIGHTUSERDATA(); /*
        return (jint)LUA_TLIGHTUSERDATA;
    */

    static native int LUA_TNUMBER(); /*
        return (jint)LUA_TNUMBER;
    */

    static native int LUA_TSTRING(); /*
        return (jint)LUA_TSTRING;
    */

    static native int LUA_TTABLE(); /*
        return (jint)LUA_TTABLE;
    */

    static native int LUA_TFUNCTION(); /*
        return (jint)LUA_TFUNCTION;
    */

    static native int LUA_TUSERDATA(); /*
        return (jint)LUA_TUSERDATA;
    */

    static native int LUA_TTHREAD(); /*
        return (jint)LUA_TTHREAD;
    */

    static native int LUA_NUMTYPES(); /*
        return (jint)LUA_NUMTYPES;
    */

    static native int LUA_MINSTACK(); /*
        return (jint)LUA_MINSTACK;
    */

    static native int LUA_RIDX_GLOBALS(); /*
        return (jint)LUA_RIDX_GLOBALS;
    */

    static native int LUA_RIDX_MAINTHREAD(); /*
        return (jint)LUA_RIDX_MAINTHREAD;
    */

    static native int LUA_RIDX_LAST(); /*
        return (jint)LUA_RIDX_LAST;
    */

    static native int LUA_OPADD(); /*
        return (jint)LUA_OPADD;
    */

    static native int LUA_OPSUB(); /*
        return (jint)LUA_OPSUB;
    */

    static native int LUA_OPMUL(); /*
        return (jint)LUA_OPMUL;
    */

    static native int LUA_OPMOD(); /*
        return (jint)LUA_OPMOD;
    */

    static native int LUA_OPPOW(); /*
        return (jint)LUA_OPPOW;
    */

    static native int LUA_OPDIV(); /*
        return (jint)LUA_OPDIV;
    */

    static native int LUA_OPIDIV(); /*
        return (jint)LUA_OPIDIV;
    */

    static native int LUA_OPBAND(); /*
        return (jint)LUA_OPBAND;
    */

    static native int LUA_OPBOR(); /*
        return (jint)LUA_OPBOR;
    */

    static native int LUA_OPBXOR(); /*
        return (jint)LUA_OPBXOR;
    */

    static native int LUA_OPSHL(); /*
        return (jint)LUA_OPSHL;
    */

    static native int LUA_OPSHR(); /*
        return (jint)LUA_OPSHR;
    */

    static native int LUA_OPUNM(); /*
        return (jint)LUA_OPUNM;
    */

    static native int LUA_OPBNOT(); /*
        return (jint)LUA_OPBNOT;
    */

    static native int LUA_OPEQ(); /*
        return (jint)LUA_OPEQ;
    */

    static native int LUA_OPLT(); /*
        return (jint)LUA_OPLT;
    */

    static native int LUA_OPLE(); /*
        return (jint)LUA_OPLE;
    */

    static native int LUA_GCSTOP(); /*
        return (jint)LUA_GCSTOP;
    */

    static native int LUA_GCRESTART(); /*
        return (jint)LUA_GCRESTART;
    */

    static native int LUA_GCCOLLECT(); /*
        return (jint)LUA_GCCOLLECT;
    */

    static native int LUA_GCCOUNT(); /*
        return (jint)LUA_GCCOUNT;
    */

    static native int LUA_GCCOUNTB(); /*
        return (jint)LUA_GCCOUNTB;
    */

    static native int LUA_GCSTEP(); /*
        return (jint)LUA_GCSTEP;
    */

    static native int LUA_GCISRUNNING(); /*
        return (jint)LUA_GCISRUNNING;
    */

    static native int LUA_GCGEN(); /*
        return (jint)LUA_GCGEN;
    */

    static native int LUA_GCINC(); /*
        return (jint)LUA_GCINC;
    */

    static native int LUA_HOOKCALL(); /*
        return (jint)LUA_HOOKCALL;
    */

    static native int LUA_HOOKRET(); /*
        return (jint)LUA_HOOKRET;
    */

    static native int LUA_HOOKLINE(); /*
        return (jint)LUA_HOOKLINE;
    */

    static native int LUA_HOOKCOUNT(); /*
        return (jint)LUA_HOOKCOUNT;
    */

    static native int LUA_HOOKTAILCALL(); /*
        return (jint)LUA_HOOKTAILCALL;
    */

    static native int LUA_MASKCALL(); /*
        return (jint)LUA_MASKCALL;
    */

    static native int LUA_MASKRET(); /*
        return (jint)LUA_MASKRET;
    */

    static native int LUA_MASKLINE(); /*
        return (jint)LUA_MASKLINE;
    */

    static native int LUA_MASKCOUNT(); /*
        return (jint)LUA_MASKCOUNT;
    */

    static native String LUA_GNAME(); /*
        return env->NewStringUTF(LUA_GNAME);
    */

    static native String LUA_LOADED_TABLE(); /*
        return env->NewStringUTF(LUA_LOADED_TABLE);
    */

    static native String LUA_PRELOAD_TABLE(); /*
        return env->NewStringUTF(LUA_PRELOAD_TABLE);
    */

    static native int LUA_NOREF(); /*
        return (jint)LUA_NOREF;
    */

    static native int LUA_REFNIL(); /*
        return (jint)LUA_REFNIL;
    */

    static native String LUA_FILEHANDLE(); /*
        return env->NewStringUTF(LUA_FILEHANDLE);
    */

    static native long newState(); /*
        return (jlong)luaL_newstate();
    */

    static native void closeState(long ptr); /*
        lua_State* L = (lua_State*)ptr;
        lua_close(L);
    */

    static native boolean initBindings(); /*
        if (update_env(env) != 0) {
            return JNI_FALSE;
        }
        jclass local_ref = env->FindClass("me/white/wlua/LuaNatives");
        if (!env->ExceptionOccurred()) {
            natives_class = (jclass)env->NewGlobalRef(local_ref);
        }
        invoke_method = env->GetStaticMethodID(natives_class, "invoke", "(JILjava/lang/Object;I)I");
        adopt_method = env->GetStaticMethodID(natives_class, "adopt", "(IJ)I");
        invoke_meta_method = env->GetStaticMethodID(natives_class, "invokeMeta", "(JII)I");
        index_method = env->GetStaticMethodID(natives_class, "index", "(JI)I");
        new_index_method = env->GetStaticMethodID(natives_class, "newIndex", "(JI)I");
        jclass throwable_class = env->FindClass("java/lang/Throwable");
        throwable_tostring_method = env->GetMethodID(throwable_class, "toString", "()Ljava/lang/String;");
        return !JAVA_BOOLEAN(env->ExceptionOccurred());
    */

    static native void initState(long ptr, int id); /*
        lua_State* L = (lua_State*)ptr;
        if (luaL_newmetatable(L, JAVA_OBJECT_GC) == 1) {
            lua_pushcfunction(L, &object_gc);
            lua_setfield(L, -2, "__gc");
        }
        lua_pop(L, 1);
        if (lua_pushthread(L) == 1) {
            lua_pop(L, 1);
            lua_pushstring(L, JAVA_STATE_INDEX);
        }
        lua_pushinteger(L, id);
        lua_rawset(L, LUA_REGISTRYINDEX);
    */

    static native void removeState(long ptr); /*
        lua_State* L = (lua_State*)ptr;
        if (lua_pushthread(L) == 1) {
            lua_pop(L, 1);
            return;
        }
        lua_pushnil(L);
        lua_rawset(L, LUA_REGISTRYINDEX);
    */

    static native int getType(long ptr, int index); /*
        lua_State* L = (lua_State*)ptr;
        return (jint)lua_type(L, index);
    */

    static native boolean isInteger(long ptr, int index); /*
        lua_State* L = (lua_State*)ptr;
        return JAVA_BOOLEAN(lua_isinteger(L, index));
    */

    static native boolean toBoolean(long ptr, int index); /*
        lua_State* L = (lua_State*)ptr;
        return JAVA_BOOLEAN(lua_toboolean(L, index));
    */

    static native long toInteger(long ptr, int index); /*
        lua_State* L = (lua_State*)ptr;
        return (jlong)lua_tointeger(L, index);
    */

    static native double toNumber(long ptr, int index); /*
        lua_State* L = (lua_State*)ptr;
        return (jdouble)lua_tonumber(L, index);
    */

    static native String toString(long ptr, int index); /*
        lua_State* L = (lua_State*)ptr;
        return env->NewStringUTF(lua_tostring(L, index));
    */

    static native int getTop(long ptr); /*
        lua_State* L = (lua_State*)ptr;
        return (jint)lua_gettop(L);
    */

    static native void pop(long ptr, int n); /*
        lua_State* L = (lua_State*)ptr;
        lua_pop(L, n);
    */

    static native void remove(long ptr, int i); /*
        lua_State* L = (lua_State*)ptr;
        lua_remove(L, i);
    */

    static native void pushBoolean(long ptr, boolean value); /*
        lua_State* L = (lua_State*)ptr;
        lua_pushboolean(L, value == JNI_TRUE ? 1 : 0);
    */

    static native void pushInteger(long ptr, long value); /*
        lua_State* L = (lua_State*)ptr;
        lua_pushinteger(L, value);
    */

    static native void pushNil(long ptr); /*
        lua_State* L = (lua_State*)ptr;
        lua_pushnil(L);
    */

    static native void pushString(long ptr, String value); /*
        lua_State* L = (lua_State*)ptr;
        lua_pushstring(L, value);
    */

    static native void pushNumber(long ptr, double value); /*
        lua_State* L = (lua_State*)ptr;
        lua_pushnumber(L, value);
    */

    static native void pushThread(long ptr); /*
        lua_State* L = (lua_State*)ptr;
        lua_pushthread(L);
    */

    static native void pushFunction(long ptr, FunctionLiteralValue.Function function); /*
        lua_State* L = (lua_State*)ptr;
        jobject global_ref = env->NewGlobalRef(function);
        if (env->ExceptionOccurred()) {
            lua_pushstring(L, "error creating global reference");
            lua_error(L);
            return;
        }
        jobject* userdata = (jobject*)lua_newuserdatauv(L, sizeof(global_ref), 0);
        *userdata = global_ref;
        luaL_setmetatable(L, JAVA_OBJECT_GC);
        lua_pushcclosure(L, &function_wrapper, 1);
    */

    static native void pushFail(long ptr); /*
        lua_State* L = (lua_State*)ptr;
        lua_pushnil(L);
    */

    static native void newTable(long ptr, int size); /*
        lua_State* L = (lua_State*)ptr;
        lua_createtable(L, 0, size);
    */

    static native int newRef(long ptr, int index); /*
        lua_State* L = (lua_State*)ptr;
        lua_pushvalue(L, index);
        return (jint)luaL_ref(L, LUA_REGISTRYINDEX);
    */

    static native void getRef(long ptr, int ref); /*
        lua_State* L = (lua_State*)ptr;
        lua_rawgeti(L, LUA_REGISTRYINDEX, ref);
    */

    static native void deleteRef(long ptr, int ref); /*
        lua_State* L = (lua_State*)ptr;
        luaL_unref(L, LUA_REGISTRYINDEX, ref);
    */

    static native int getThreadId(long ptr, int index); /*
        lua_State* L = (lua_State*)ptr;
        if (!lua_isthread(L, index)) {
            return -1;
        }
        lua_State* thread = lua_tothread(L, index);
        return (jint)get_state_index(thread);
    */

    static native boolean newMetaTable(long ptr, String name, String displayName); /*
        lua_State* L = (lua_State*)ptr;
        int isNew = luaL_newmetatable(L, name);
        if (isNew) {
            lua_pushstring(L, displayName);
            lua_setfield(L, -2, "__name");
            lua_pushcfunction(L, &object_gc);
            lua_setfield(L, -2, "__gc");
            lua_pushcfunction(L, &index_wrapper);
            lua_setfield(L, -2, "__index");
            lua_pushcfunction(L, &new_index_wrapper);
            lua_setfield(L, -2, "__newindex");
        }
        return JAVA_BOOLEAN(isNew);
    */

    static native void setMetaMethod(long ptr, String name, int type, int returns); /*
        lua_State* L = (lua_State*)ptr;
        lua_pushinteger(L, type);
        lua_pushinteger(L, returns);
        lua_pushcclosure(L, &meta_method_wrapper, 2);
        lua_setfield(L, -2, name);
    */

    static native void newUserData(long ptr, Object obj); /*
        lua_State* L = (lua_State*)ptr;
        jobject global_ref = env->NewGlobalRef(obj);
        if (env->ExceptionOccurred()) {
            lua_pushstring(L, "error creating global reference");
            lua_error(L);
            return;
        }
        jobject* userdata = (jobject*)lua_newuserdatauv(L, sizeof(global_ref), 0);
        *userdata = global_ref;
    */

    static native void setMetaTable(long ptr); /*
        lua_State* L = (lua_State*)ptr;
        lua_setmetatable(L, -2);
    */

    static native Object getUserData(long ptr, int index); /*
        lua_State* L = (lua_State*)ptr;
        if (!lua_isuserdata(L, index)) {
            return NULL;
        }
        jobject* userdata = (jobject*)lua_touserdata(L, index);
        return *userdata;
    */

    static native int tableSize(long ptr); /*
        lua_State* L = (lua_State*)ptr;
        int length = 0;
        lua_pushnil(L);
        while (lua_next(L, -2)) {
            length += 1;
            lua_pop(L, 1);
        }
        lua_pop(L, 1);
        return (jint)length;
    */

    static native boolean isTableEmpty(long ptr); /*
        lua_State* L = (lua_State*)ptr;
        lua_pushnil(L);
        int has_next = lua_next(L, -2);
        lua_pop(L, has_next ? 3 : 1);
        return !JAVA_BOOLEAN(has_next);
    */

    static native boolean tableContainsKey(long ptr); /*
        lua_State* L = (lua_State*)ptr;
        lua_gettable(L, -2);
        int contains = !lua_isnil(L, -1);
        lua_pop(L, 2);
        return JAVA_BOOLEAN(contains);
    */

    static native boolean tableContainsValue(long ptr); /*
        lua_State* L = (lua_State*)ptr;
        lua_pushnil(L);
        while (lua_next(L, -3)) {
            if (lua_compare(L, -3, -1, LUA_OPEQ)) {
                lua_pop(L, 4);
                return JNI_TRUE;
            }
            lua_pop(L, 1);
        }
        lua_pop(L, 2);
        return JNI_FALSE;
    */

    static native void tableGet(long ptr); /*
        lua_State* L = (lua_State*)ptr;
        lua_gettable(L, -2);
    */

    static native void tableSet(long ptr); /*
        lua_State* L = (lua_State*)ptr;
        lua_settable(L, -3);
    */

    static native void tableSetField(long ptr, String name); /*
        lua_State* L = (lua_State*)ptr;
        lua_setfield(L, -2, name);
    */

    static native void tableRemove(long ptr); /*
        lua_State* L = (lua_State*)ptr;
        lua_pushnil(L);
        lua_settable(L, -3);
    */

    static native void tableClear(long ptr); /*
        lua_State* L = (lua_State*)ptr;
        lua_pushnil(L);
        while (lua_next(L, -2)) {
            lua_pop(L, 1);
            lua_pushvalue(L, -1);
            lua_pushnil(L);
            lua_settable(L, -4);
        }
        lua_pop(L, 1);
    */

    static native boolean tableNext(long ptr); /*
        lua_State* L = (lua_State*)ptr;
        return JAVA_BOOLEAN(lua_next(L, -2));
    */

    static native void openLibs(long ptr); /*
        lua_State* L = (lua_State*)ptr;
        luaL_openlibs(L);
    */

    static native void setGlobal(long ptr, String name); /*
        lua_State* L = (lua_State*)ptr;
        lua_setglobal(L, name);
    */

    static native void getGlobal(long ptr, String name); /*
        lua_State* L = (lua_State*)ptr;
        lua_getglobal(L, name);
    */

    static native int protectedCall(long ptr, int args, int returns); /*
        lua_State* L = (lua_State*)ptr;
        return (jint)lua_pcall(L, args, returns, 0);
    */

    static native int loadString(long ptr, String string, String name); /*
        lua_State* L = (lua_State*)ptr;
        return (jint)luaL_loadbuffer(L, string, std::strlen(string), name);
    */

    static native boolean compareValues(long ptr, int index1, int index2, int op); /*
        lua_State* L = (lua_State*)ptr;
        return JAVA_BOOLEAN(lua_compare(L, index1, index2, op));
    */

    static native int resume(long ptr, int args); /*
        lua_State* L = (lua_State*)ptr;
        lua_getfield(L, LUA_REGISTRYINDEX, YIELD_FIELD);
        if (lua_isinteger(L, -1)) {
            int required = lua_tointeger(L, -1);
            lua_pop(L, 1);
            lua_pushnil(L);
            lua_setfield(L, LUA_REGISTRYINDEX, YIELD_FIELD);
            if (required != -1 && args != required) {
                lua_pushstring(L, "wrong amount of arguments to resume a coroutine");
                lua_error(L);
                return -1;
            }
        } else {
            lua_pop(L, 1);
        }
        int results;
        int code = lua_resume(L, (lua_State*)NULL, args, &results);
        return code;
    */

    static native boolean isYieldable(long ptr); /*
        lua_State* L = (lua_State*)ptr;
        return JAVA_BOOLEAN(lua_isyieldable(L));
    */

    static native boolean isSuspended(long ptr); /*
        lua_State* L = (lua_State*)ptr;
        return JAVA_BOOLEAN(lua_status(L) == LUA_YIELD);
    */

    static native void yield(long ptr); /*
        lua_State* L = (lua_State*)ptr;
        lua_pushboolean(L, 1);
        lua_setfield(L, LUA_REGISTRYINDEX, YIELD_FIELD);
    */

    static native long newThread(long ptr); /*
        lua_State* L = (lua_State*)ptr;
        jlong thread_ptr = (jlong)lua_newthread(L);
        lua_pop(L, 1);
        return thread_ptr;
    */
}

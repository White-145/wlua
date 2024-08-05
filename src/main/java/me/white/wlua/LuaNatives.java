package me.white.wlua;

import com.badlogic.gdx.utils.SharedLibraryLoader;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class LuaNatives {
    // @off
    static {
        new SharedLibraryLoader().load("wlua");
        if (LuaNatives.initBindings() != 0) {
            throw new IllegalStateException("Could not init native bindings.");
        }
    }

    private static int error(long ptr, String message) {
        LuaNatives.lua_pushstring(ptr, message);
        return -1;
    }

    private static int invoke(long callerPtr, int stateIndex, Object function, int params) {
        LuaState state = LuaInstances.get(stateIndex);
        if (state == null) {
            return error(callerPtr, "error getting lua state");
        }
        if (!(function instanceof FunctionValue)) {
            return error(callerPtr, "error invoking java function");
        }
        VarArg args = VarArg.collect(state, params);
        VarArg results = ((FunctionValue)function).run(state, args);
        results.push(state);
        return results.size();
    }

    private static int adopt(int mainId, long ptr) {
        LuaState lua = LuaInstances.get(mainId);
        return LuaInstances.add((id) -> {
            LuaState child = new LuaState(ptr, id, lua);
            lua.addSubThread(child);
            return child;
        });
    }

    private static int invokeMeta(long callerPtr, int stateIndex, int metaMethodType) {
        LuaState state = LuaInstances.get(stateIndex);
        if (state == null) {
            return error(callerPtr, "error getting lua state");
        }
        MetaMethodType type = MetaMethodType.values()[metaMethodType];
        LuaValue userdata = LuaValue.from(state, 1);
        if (!(userdata instanceof UserData)) {
            return error(callerPtr, "error getting userdata");
        }
        if (!((UserData)userdata).metaMethods.containsKey(type)) {
            return error(callerPtr, "error getting java function");
        }
        Method method = ((UserData)userdata).metaMethods.get(type);
        if (type.parameters == -1) {
            int total = LuaNatives.lua_gettop(state.ptr);
            VarArg args = VarArg.collect(state, total - 1);
            state.pop(1);
            Object returnValue;
            try {
                returnValue = method.invoke(userdata, state, args);
            } catch (InvocationTargetException | IllegalAccessException ignored) {
                return error(callerPtr, "error invoking java function");
            }
            if (returnValue instanceof VarArg) {
                ((VarArg)returnValue).push(state);
                return ((VarArg)returnValue).size();
            }
            return 0;
        }
        if (type.doubleReference) {
            state.pop(1);
        }
        Object[] values = new Object[type.parameters + 1];
        values[0] = state;
        for (int i = 0; i < type.parameters; ++i) {
            values[i + 1] = LuaValue.from(state, i - type.parameters);
        }
        state.pop(type.parameters);
        Object returnValue;
        try {
            returnValue = method.invoke(userdata, values);
        } catch (InvocationTargetException | IllegalAccessException ignored) {
            return error(callerPtr, "error invoking java function");
        }
        if (type.returns == 0) {
            return 0;
        }
        if (returnValue instanceof LuaValue) {
            ((LuaValue)returnValue).push(state);
        } else {
            LuaNatives.lua_pushnil(state.ptr);
        }
        return 1;
    }

    public static int index(long callerPtr, int stateIndex) {
        LuaState state = LuaInstances.get(stateIndex);
        if (state == null) {
            return error(callerPtr, "error getting lua state");
        }
        LuaValue userdataValue = LuaValue.from(state, -2);
        if (!(userdataValue instanceof UserData)) {
            return error(callerPtr, "error getting userdata");
        }
        UserData userdata = (UserData)userdataValue;
        LuaValue key = LuaValue.from(state, -1);
        state.pop(2);
        if (key instanceof StringValue) {
            String name = ((StringValue)key).getString();
            if (userdata.fields.containsKey(name)) {
                Field field = userdata.fields.get(name);
                Object result;
                try {
                    result = field.get(userdata);
                } catch (IllegalAccessException ignored) {
                    return error(callerPtr, "error getting field");
                }
                if (result instanceof LuaValue) {
                    ((LuaValue)result).push(state);
                } else {
                    LuaNatives.lua_pushnil(state.ptr);
                }
                return 1;
            }
            if (userdata.functions.containsKey(name)) {
                Method method = userdata.functions.get(name);
                LuaValue.of((lua, args) -> {
                    Object result;
                    try {
                        result = method.invoke(userdata, lua, args);
                    } catch (IllegalAccessException | InvocationTargetException ignored) {
                        throw new IllegalStateException("Could not invoke java function '" + method.getName() + "'.");
                    }
                    if (result instanceof VarArg) {
                        return (VarArg)result;
                    }
                    return new VarArg();
                }).push(state);
                return 1;
            }
            if (userdata.getters.containsKey(name)) {
                Method method = userdata.getters.get(name);
                Object result;
                try {
                    result = method.invoke(userdata, state);
                } catch (IllegalAccessException | InvocationTargetException ignored) {
                    return error(callerPtr, "error invoking java function");
                }
                if (result instanceof LuaValue) {
                    ((LuaValue)result).push(state);
                } else {
                    LuaNatives.lua_pushnil(state.ptr);
                }
                return 1;
            }
        }
        if (userdata.metaMethods.containsKey(MetaMethodType.INDEX)) {
            Method method = userdata.metaMethods.get(MetaMethodType.INDEX);
            Object returns;
            try {
                returns = method.invoke(userdata, state, key);
            } catch (IllegalAccessException | InvocationTargetException ignored) {
                return error(callerPtr, "error invoking java function");
            }
            if (returns instanceof LuaValue) {
                ((LuaValue)returns).push(state);
            } else {
                LuaNatives.lua_pushnil(state.ptr);
            }
            return 1;
        }
        return 0;
    }

    public static int newIndex(long callerPtr, int stateIndex) {
        LuaState state = LuaInstances.get(stateIndex);
        if (state == null) {
            return error(callerPtr, "error getting lua state");
        }
        LuaValue userdataValue = LuaValue.from(state, -3);
        if (!(userdataValue instanceof UserData)) {
            return error(callerPtr, "error getting userdata");
        }
        UserData userdata = (UserData)userdataValue;
        LuaValue key = LuaValue.from(state, -2);
        LuaValue value = LuaValue.from(state, -1);
        state.pop(3);
        if (key instanceof StringValue) {
            String name = ((StringValue)key).getString();
            if (userdata.fields.containsKey(name)) {
                Field field = userdata.fields.get(name);
                if (Modifier.isFinal(field.getModifiers())) {
                    return error(callerPtr, "error setting field");
                }
                try {
                    field.set(userdata, value);
                } catch (IllegalAccessException ignored) {
                    return error(callerPtr, "error setting field");
                }
                return 1;
            }
            if (userdata.setters.containsKey(name)) {
                Method method = userdata.setters.get(name);
                try {
                    method.invoke(userdata, state, value);
                } catch (IllegalAccessException | InvocationTargetException ignored) {
                    return error(callerPtr, "error invoking java function");
                }
                return 1;
            }
        }
        if (userdata.metaMethods.containsKey(MetaMethodType.NEW_INDEX)) {
            Method method = userdata.metaMethods.get(MetaMethodType.NEW_INDEX);
            try {
                method.invoke(userdata, state, key, value);
            } catch (IllegalAccessException | InvocationTargetException ignored) {
                return error(callerPtr, "error invoking java function");
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

    #define JAVA_STATE_INDEX "state_index"
    #define JAVA_OBJECT_GC "object_gc"

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
        int new_id = env->CallStaticIntMethod(natives_class, adopt_method, main_id, (jlong)L);
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
        int value = (int)env->CallStaticIntMethod(natives_class, invoke_method, (jlong)L, (jint)state_index, *function, (jint)lua_gettop(L));
        return return_or_error(env, L, value);
    }

    int meta_method_wrapper(lua_State* L) {
        int type = lua_tointeger(L, lua_upvalueindex(1));
        int state_index = get_state_index(L);
        JNIEnv* env = get_env(L);
        int returns = env->CallStaticIntMethod(natives_class, invoke_meta_method, (jlong)L, (jint)state_index, (jint)type);
        return return_or_error(env, L, returns);
    }

    int index_wrapper(lua_State* L) {
        int state_index = get_state_index(L);
        JNIEnv* env = get_env(L);
        int returns = env->CallStaticIntMethod(natives_class, index_method, (jlong)L, (jint)state_index);
        return return_or_error(env, L, returns);
    }

    int new_index_wrapper(lua_State* L) {
        int state_index = get_state_index(L);
        JNIEnv* env = get_env(L);
        int returns = env->CallStaticIntMethod(natives_class, new_index_method, (jlong)L, (jint)state_index);
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

    static native int lua_absindex(long ptr, int idx); /*
        return (jint)lua_absindex((lua_State*)ptr, (int)idx);
    */

    static native void lua_arith(long ptr, int op); /*
        lua_arith((lua_State*)ptr, (int)op);
    */

    static native int lua_checkstack(long ptr, int n); /*
        return (jint)lua_checkstack((lua_State*)ptr, (int)n);
    */

    static native void lua_close(long ptr); /*
        lua_close((lua_State*)ptr);
    */

    static native void lua_closeslot(long ptr, int index); /*
        lua_closeslot((lua_State*)ptr, (int)index);
    */

    static native int lua_closethread(long ptr, long from); /*
        return (jint)lua_closethread((lua_State*)ptr, (lua_State*)from);
    */

    static native int lua_compare(long ptr, int index1, int index2, int op); /*
        return (jint)lua_compare((lua_State*)ptr, (int)index1, (int)index2, (int)op);
    */

    static native void lua_concat(long ptr, int n); /*
        lua_concat((lua_State*)ptr, (int)n);
    */

    static native void lua_copy(long ptr, int fromidx, int toidx); /*
        lua_copy((lua_State*)ptr, (int)fromidx, (int)toidx);
    */

    static native void lua_createtable(long ptr, int narr, int nrec); /*
        lua_createtable((lua_State*)ptr, (int)narr, (int)nrec);
    */

    static native int lua_error(long ptr); /*
        return (jint)lua_error((lua_State*)ptr);
    */

    static native int lua_getfield(long ptr, int index, String k); /*
        return (jint)lua_getfield((lua_State*)ptr, (int)index, k);
    */

    static native long lua_getextraspace(long ptr); /*
        return (jlong)lua_getextraspace((lua_State*)ptr);
    */

    static native int lua_getglobal(long ptr, String name); /*
        return (jint)lua_getglobal((lua_State*)ptr, name);
    */

    static native int lua_geti(long ptr, int index, long i); /*
        return (jint)lua_geti((lua_State*)ptr, (int)index, (lua_Integer)i);
    */

    static native int lua_getmetatable(long ptr, int index); /*
        return (jint)lua_getmetatable((lua_State*)ptr, (int)index);
    */

    static native int lua_gettable(long ptr, int index); /*
        return (jint)lua_gettable((lua_State*)ptr, (int)index);
    */

    static native int lua_gettop(long ptr); /*
        return (jint)lua_gettop((lua_State*)ptr);
    */

    static native int lua_getiuservalue(long ptr, int index, int n); /*
        return (jint)lua_getiuservalue((lua_State*)ptr, (int)index, (int)n);
    */

    static native void lua_insert(long ptr, int index); /*
        lua_insert((lua_State*)ptr, (int)index);
    */

    static native int lua_isboolean(long ptr, int index); /*
        return (jint)lua_isboolean((lua_State*)ptr, (int)index);
    */

    static native int lua_iscfunction(long ptr, int index); /*
        return (jint)lua_iscfunction((lua_State*)ptr, (int)index);
    */

    static native int lua_isfunction(long ptr, int index); /*
        return (jint)lua_isfunction((lua_State*)ptr, (int)index);
    */

    static native int lua_isinteger(long ptr, int index); /*
        return (jint)lua_isinteger((lua_State*)ptr, (int)index);
    */

    static native int lua_islightuserdata(long ptr, int index); /*
        return (jint)lua_islightuserdata((lua_State*)ptr, (int)index);
    */

    static native int lua_isnil(long ptr, int index); /*
        return (jint)lua_isnil((lua_State*)ptr, (int)index);
    */

    static native int lua_isnone(long ptr, int index); /*
        return (jint)lua_isnone((lua_State*)ptr, (int)index);
    */

    static native int lua_isnoneornil(long ptr, int index); /*
        return (jint)lua_isnoneornil((lua_State*)ptr, (int)index);
    */

    static native int lua_isnumber(long ptr, int index); /*
        return (jint)lua_isnumber((lua_State*)ptr, (int)index);
    */

    static native int lua_isstring(long ptr, int index); /*
        return (jint)lua_isstring((lua_State*)ptr, (int)index);
    */

    static native int lua_istable(long ptr, int index); /*
        return (jint)lua_istable((lua_State*)ptr, (int)index);
    */

    static native int lua_isthread(long ptr, int index); /*
        return (jint)lua_isthread((lua_State*)ptr, (int)index);
    */

    static native int lua_isuserdata(long ptr, int index); /*
        return (jint)lua_isuserdata((lua_State*)ptr, (int)index);
    */

    static native int lua_isyieldable(long ptr); /*
        return (jint)lua_isyieldable((lua_State*)ptr);
    */

    static native void lua_len(long ptr, int index); /*
        lua_len((lua_State*)ptr, (int)index);
    */

    static native void lua_newtable(long ptr); /*
        lua_newtable((lua_State*)ptr);
    */

    static native long lua_newthread(long ptr); /*
        return (jlong)lua_newthread((lua_State*)ptr);
    */

    static native long lua_newuserdatauv(long ptr, long size, int nuvalue); /*
        return (jlong)lua_newuserdatauv((lua_State*)ptr, (size_t)size, (int)nuvalue);
    */

    static native int lua_next(long ptr, int index); /*
        return (jint)lua_next((lua_State*)ptr, (int)index);
    */

    static native int lua_numbertointeger(double n, long p); /*
        return (jint)lua_numbertointeger((lua_Number)n, (lua_Integer*)p);
    */

    static native int lua_pcall(long ptr, int nargs, int nresults, int msgh); /*
        return (jint)lua_pcall((lua_State*)ptr, (int)nargs, (int)nresults, (int)msgh);
    */

    static native void lua_pop(long ptr, int n); /*
        lua_pop((lua_State*)ptr, (int)n);
    */

    static native void lua_pushboolean(long ptr, int b); /*
        lua_pushboolean((lua_State*)ptr, (int)b);
    */

    static native void lua_pushglobaltable(long ptr); /*
        lua_pushglobaltable((lua_State*)ptr);
    */

    static native void lua_pushinteger(long ptr, long n); /*
        lua_pushinteger((lua_State*)ptr, (lua_Integer)n);
    */

    static native void lua_pushlightuserdata(long ptr, long p); /*
        lua_pushlightuserdata((lua_State*)ptr, (void*)p);
    */

    static native void lua_pushnil(long ptr); /*
        lua_pushnil((lua_State*)ptr);
    */

    static native void lua_pushnumber(long ptr, double n); /*
        lua_pushnumber((lua_State*)ptr, (lua_Number)n);
    */

    static native void lua_pushstring(long ptr, String s); /*
        lua_pushstring((lua_State*)ptr, s);
    */

    static native int lua_pushthread(long ptr); /*
        return (jint)lua_pushthread((lua_State*)ptr);
    */

    static native void lua_pushvalue(long ptr, int index); /*
        lua_pushvalue((lua_State*)ptr, (int)index);
    */

    static native int lua_rawequal(long ptr, int index1, int index2); /*
        return (jint)lua_rawequal((lua_State*)ptr, (int)index1, (int)index2);
    */

    static native int lua_rawget(long ptr, int index); /*
        return (jint)lua_rawget((lua_State*)ptr, (int)index);
    */

    static native int lua_rawgeti(long ptr, int index, long n); /*
        return (jint)lua_rawgeti((lua_State*)ptr, (int)index, (lua_Integer)n);
    */

    static native int lua_rawgetp(long ptr, int index, long p); /*
        return (jint)lua_rawgetp((lua_State*)ptr, (int)index, (const void*)p);
    */

    static native long lua_rawlen(long ptr, int index); /*
        return (jlong)lua_rawlen((lua_State*)ptr, (int)index);
    */

    static native void lua_rawset(long ptr, int index); /*
        lua_rawset((lua_State*)ptr, (int)index);
    */

    static native void lua_rawseti(long ptr, int index, long i); /*
        lua_rawseti((lua_State*)ptr, (int)index, (lua_Integer)i);
    */

    static native void lua_rawsetp(long ptr, int index, long p); /*
        lua_rawsetp((lua_State*)ptr, (int)index, (const void*)p);
    */

    static native void lua_remove(long ptr, int index); /*
        lua_remove((lua_State*)ptr, (int)index);
    */

    static native void lua_replace(long ptr, int index); /*
        lua_replace((lua_State*)ptr, (int)index);
    */

    static native int lua_resetthread(long ptr); /*
        return (jint)lua_resetthread((lua_State*)ptr);
    */

    static native int lua_resume(long ptr, long from, int nargs, long nresults); /*
        return (jint)lua_resume((lua_State*)ptr, (lua_State*)from, (int)nargs, (int*)nresults);
    */

    static native void lua_rotate(long ptr, int idx, int n); /*
        lua_rotate((lua_State*)ptr, (int)idx, (int)n);
    */

    static native void lua_setfield(long ptr, int index, String k); /*
        lua_setfield((lua_State*)ptr, (int)index, k);
    */

    static native void lua_setglobal(long ptr, String name); /*
        lua_setglobal((lua_State*)ptr, name);
    */

    static native void lua_seti(long ptr, int index, long n); /*
        lua_seti((lua_State*)ptr, (int)index, (lua_Integer)n);
    */

    static native int lua_setiuservalue(long ptr, int index, int n); /*
        return (jint)lua_setiuservalue((lua_State*)ptr, (int)index, (int)n);
    */

    static native int lua_setmetatable(long ptr, int index); /*
        return (jint)lua_setmetatable((lua_State*)ptr, (int)index);
    */

    static native void lua_settable(long ptr, int index); /*
        lua_settable((lua_State*)ptr, (int)index);
    */

    static native void lua_settop(long ptr, int index); /*
        lua_settop((lua_State*)ptr, (int)index);
    */

    static native int lua_status(long ptr); /*
        return (jint)lua_status((lua_State*)ptr);
    */

    static native long lua_stringtonumber(long ptr, String s); /*
        return (jlong)lua_stringtonumber((lua_State*)ptr, s);
    */

    static native int lua_toboolean(long ptr, int index); /*
        return (jint)lua_toboolean((lua_State*)ptr, (int)index);
    */

    static native void lua_toclose(long ptr, int index); /*
        lua_toclose((lua_State*)ptr, (int)index);
    */

    static native long lua_tointeger(long ptr, int index); /*
        return (jlong)lua_tointeger((lua_State*)ptr, (int)index);
    */

    static native long lua_tointegerx(long ptr, int index, long isnum); /*
        return (jlong)lua_tointegerx((lua_State*)ptr, (int)index, (int*)isnum);
    */

    static native double lua_tonumber(long ptr, int index); /*
        return (jdouble)lua_tonumber((lua_State*)ptr, (int)index);
    */

    static native double lua_tonumberx(long ptr, int index, long isnum); /*
        return (jdouble)lua_tonumberx((lua_State*)ptr, (int)index, (int*)isnum);
    */

    static native long lua_topointer(long ptr, int index); /*
        return (jlong)lua_topointer((lua_State*)ptr, (int)index);
    */

    static native String lua_tostring(long ptr, int index); /*
        lua_State* L = (lua_State*)ptr;
        lua_pushvalue(L, index);
        const char* str = lua_tostring(L, -1);
        lua_pop(L, 1);
        return env->NewStringUTF(str);
    */

    static native long lua_tothread(long ptr, int index); /*
        return (jlong)lua_tothread((lua_State*)ptr, (int)index);
    */

    static native long lua_touserdata(long ptr, int index); /*
        return (jlong)lua_touserdata((lua_State*)ptr, (int)index);
    */

    static native int lua_type(long ptr, int index); /*
        return (jint)lua_type((lua_State*)ptr, (int)index);
    */

    static native String lua_typename(long ptr, int tp); /*
        return env->NewStringUTF(lua_typename((lua_State*)ptr, (int)tp));
    */

    static native int lua_upvalueindex(int i); /*
        return (jint)lua_upvalueindex((int)i);
    */

    static native double lua_version(long ptr); /*
        return (jdouble)lua_version((lua_State*)ptr);
    */

    static native void lua_warning(long ptr, String msg, int tocont); /*
        lua_warning((lua_State*)ptr, msg, (int)tocont);
    */

    static native void lua_xmove(long from, long to, int n); /*
        lua_xmove((lua_State*)from, (lua_State*)to, (int)n);
    */

    static native int lua_yield(long ptr, int nresults); /*
        return (jint)lua_yield((lua_State*)ptr, (int)nresults);
    */

    static native int luaL_callmeta(long ptr, int obj, String e); /*
        return (jint)luaL_callmeta((lua_State*)ptr, (int)obj, e);
    */

    static native int luaL_dostring(long ptr, String str); /*
        return (jint)luaL_dostring((lua_State*)ptr, str);
    */

    static native int luaL_execresult(long ptr, int stat); /*
        return (jint)luaL_execresult((lua_State*)ptr, (int)stat);
    */

    static native int luaL_fileresult(long ptr, int stat, String fname); /*
        return (jint)luaL_fileresult((lua_State*)ptr, (int)stat, fname);
    */

    static native int luaL_getmetafield(long ptr, int obj, String e); /*
        return (jint)luaL_getmetafield((lua_State*)ptr, (int)obj, e);
    */

    static native int luaL_getmetatable(long ptr, String tname); /*
        return (jint)luaL_getmetatable((lua_State*)ptr, tname);
    */

    static native int luaL_getsubtable(long ptr, int idx, String fname); /*
        return (jint)luaL_getsubtable((lua_State*)ptr, (int)idx, fname);
    */

    static native String luaL_gsub(long ptr, String s, String p, String r); /*
        return env->NewStringUTF(luaL_gsub((lua_State*)ptr, s, p, r));
    */

    static native long luaL_len(long ptr, int index); /*
        return (jlong)luaL_len((lua_State*)ptr, (int)index);
    */

    static native int luaL_loadstring(long ptr, String s); /*
        return (jint)luaL_loadstring((lua_State*)ptr, s);
    */

    static native int luaL_newmetatable(long ptr, String tname); /*
        return (jint)luaL_newmetatable((lua_State*)ptr, tname);
    */

    static native long luaL_newstate(); /*
        return (jlong)luaL_newstate();
    */

    static native void luaL_openlibs(long ptr); /*
        luaL_openlibs((lua_State*)ptr);
    */

    static native void luaL_pushfail(long ptr); /*
        luaL_pushfail((lua_State*)ptr);
    */

    static native int luaL_ref(long ptr, int t); /*
        return (jint)luaL_ref((lua_State*)ptr, (int)t);
    */

    static native void luaL_setmetatable(long ptr, String tname); /*
        luaL_setmetatable((lua_State*)ptr, tname);
    */

    static native long luaL_testudata(long ptr, int arg, String tname); /*
        return (jlong)luaL_testudata((lua_State*)ptr, (int)arg, tname);
    */

    static native String luaL_tolstring(long ptr, int idx, long len); /*
        return env->NewStringUTF(luaL_tolstring((lua_State*)ptr, (int)idx, (size_t*)len));
    */

    static native void luaL_traceback(long ptr, long ptr1, String msg, int level); /*
        luaL_traceback((lua_State*)ptr, (lua_State*)ptr1, msg, (int)level);
    */

    static native String luaL_typename(long ptr, int index); /*
        return env->NewStringUTF(luaL_typename((lua_State*)ptr, (int)index));
    */

    static native void luaL_unref(long ptr, int t, int ref); /*
        luaL_unref((lua_State*)ptr, (int)t, (int)ref);
    */

    static native void luaL_where(long ptr, int lvl); /*
        luaL_where((lua_State*)ptr, (int)lvl);
    */

    static native long newState(); /*
        return (jlong)luaL_newstate();
    */

    static native int initBindings(); /*
        if (update_env(env) != 0) {
            return -1;
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
        return (jint)(env->ExceptionOccurred() ? -1 : 0);
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

    static native void pushFunction(long ptr, FunctionValue function); /*
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

    static native int getRef(long ptr, int i); /*
        lua_State* L = (lua_State*)ptr;
        lua_pushvalue(L, i);
        return (jint)luaL_ref(L, LUA_REGISTRYINDEX);
    */

    static native int getThreadId(long ptr, int i); /*
        lua_State* L = (lua_State*)ptr;
        if (!lua_isthread(L, i)) {
            return -1;
        }
        lua_State* thread = lua_tothread(L, i);
        return get_state_index(thread);
    */

    static native int newMetaTable(long ptr, String name); /*
        lua_State* L = (lua_State*)ptr;
        int isNew = luaL_newmetatable(L, name);
        if (isNew) {
            lua_pushcfunction(L, &object_gc);
            lua_setfield(L, -2, "__gc");
            lua_pushcfunction(L, &index_wrapper);
            lua_setfield(L, -2, "__index");
            lua_pushcfunction(L, &new_index_wrapper);
            lua_setfield(L, -2, "__newindex");
        }
        return isNew;
    */

    static native void setMetaMethod(long ptr, String name, int type, int index); /*
        lua_State* L = (lua_State*)ptr;
        lua_pushstring(L, name);
        lua_pushinteger(L, (int)type);
        lua_pushcclosure(L, &meta_method_wrapper, 1);
        lua_settable(L, (int)index < 0 ? (int)index - 2 : (int)index);
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

    static native Object getUserData(long ptr, int index); /*
        lua_State* L = (lua_State*)ptr;
        if (!lua_isuserdata(L, index)) {
            return NULL;
        }
        jobject* userdata = (jobject*)lua_touserdata(L, index);
        return *userdata;
    */
}

package me.white.wlua;

import com.badlogic.gdx.utils.SharedLibraryLoader;

public class LuaNatives {
    // @off
    static {
        new SharedLibraryLoader().load("lua");
        if (init_bindings() != 0) {
            throw new IllegalStateException("Could not init native bindings.");
        }
    }

    private static int invoke(int stateIndex, Object function, int params) {
        LuaState state = LuaInstances.get(stateIndex);
        if (state == null) {
            lua_pushstring(state.ptr, "error getting lua state");
            return -1;
        }
        if (!(function instanceof JavaFunction)) {
            lua_pushstring(state.ptr, "error invoking java function");
            return -1;
        }
        return ((JavaFunction)function).run(state, params);
    }

    private static int adopt(int mainId, long ptr) {
        LuaState lua = LuaInstances.get(mainId);
        return LuaInstances.add((id) -> {
            LuaState child = new LuaState(ptr, id, lua);
            lua.addSubThread(child);
            return child;
        });
    }

    /*JNI
    extern "C" {
        #include "lua.h"
        #include "lualib.h"
        #include "lauxlib.h"
    }

    #define JAVA_STATE_INDEX "state_index"
    #define JAVA_OBJECT_METATABLE "object_meta"
    #define JAVA_FUNCTION_DATA "function_data"

    JavaVM* java_vm = NULL;
    jint env_version;
    jclass natives_class;
    jmethodID invoke_method;
    jmethodID adopt_method;
    jmethodID throwable_tostring_method;

    int update_env(JNIEnv * env) {
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

    int function_wrapper(lua_State* L) {
        jobject* function = (jobject*)lua_touserdata(L, lua_upvalueindex(1));
        int state_index = get_state_index(L);
        JNIEnv* env = get_env(L);
        int value = (int)env->CallStaticIntMethod(natives_class, invoke_method, (jint)state_index, *function, lua_gettop(L));
        return return_or_error(env, L, value);
    }

    int gc_java(lua_State* L) {
        jobject* global_ref = (jobject*)luaL_checkudata(L, 1, JAVA_FUNCTION_DATA);
        JNIEnv* env = get_env(L);
        env->DeleteGlobalRef(*global_ref);
        return 0;
    }
    */

    protected static native String LUA_COPYRIGHT(); /*
        return env->NewStringUTF(LUA_COPYRIGHT);
    */

    protected static native String LUA_AUTHORS(); /*
        return env->NewStringUTF(LUA_AUTHORS);
    */

    protected static native String LUA_VERSION_MAJOR(); /*
        return env->NewStringUTF(LUA_VERSION_MAJOR);
    */

    protected static native String LUA_VERSION_MINOR(); /*
        return env->NewStringUTF(LUA_VERSION_MINOR);
    */

    protected static native String LUA_VERSION_RELEASE(); /*
        return env->NewStringUTF(LUA_VERSION_RELEASE);
    */

    protected static native int LUA_VERSION_NUM(); /*
        return (jint)LUA_VERSION_NUM;
    */

    protected static native int LUA_VERSION_RELEASE_NUM(); /*
        return (jint)LUA_VERSION_RELEASE_NUM;
    */

    protected static native String LUA_SIGNATURE(); /*
        return env->NewStringUTF(LUA_SIGNATURE);
    */

    protected static native int LUA_MULTRET(); /*
        return (jint)LUA_MULTRET;
    */

    protected static native int LUA_REGISTRYINDEX(); /*
        return (jint)LUA_REGISTRYINDEX;
    */

    protected static native int LUA_OK(); /*
        return (jint)LUA_OK;
    */

    protected static native int LUA_YIELD(); /*
        return (jint)LUA_YIELD;
    */

    protected static native int LUA_ERRRUN(); /*
        return (jint)LUA_ERRRUN;
    */

    protected static native int LUA_ERRSYNTAX(); /*
        return (jint)LUA_ERRSYNTAX;
    */

    protected static native int LUA_ERRMEM(); /*
        return (jint)LUA_ERRMEM;
    */

    protected static native int LUA_ERRERR(); /*
        return (jint)LUA_ERRERR;
    */

    protected static native int LUA_TNONE(); /*
        return (jint)LUA_TNONE;
    */

    protected static native int LUA_TNIL(); /*
        return (jint)LUA_TNIL;
    */

    protected static native int LUA_TBOOLEAN(); /*
        return (jint)LUA_TBOOLEAN;
    */

    protected static native int LUA_TLIGHTUSERDATA(); /*
        return (jint)LUA_TLIGHTUSERDATA;
    */

    protected static native int LUA_TNUMBER(); /*
        return (jint)LUA_TNUMBER;
    */

    protected static native int LUA_TSTRING(); /*
        return (jint)LUA_TSTRING;
    */

    protected static native int LUA_TTABLE(); /*
        return (jint)LUA_TTABLE;
    */

    protected static native int LUA_TFUNCTION(); /*
        return (jint)LUA_TFUNCTION;
    */

    protected static native int LUA_TUSERDATA(); /*
        return (jint)LUA_TUSERDATA;
    */

    protected static native int LUA_TTHREAD(); /*
        return (jint)LUA_TTHREAD;
    */

    protected static native int LUA_NUMTYPES(); /*
        return (jint)LUA_NUMTYPES;
    */

    protected static native int LUA_MINSTACK(); /*
        return (jint)LUA_MINSTACK;
    */

    protected static native int LUA_RIDX_GLOBALS(); /*
        return (jint)LUA_RIDX_GLOBALS;
    */

    protected static native int LUA_RIDX_MAINTHREAD(); /*
        return (jint)LUA_RIDX_MAINTHREAD;
    */

    protected static native int LUA_RIDX_LAST(); /*
        return (jint)LUA_RIDX_LAST;
    */

    protected static native int LUA_OPADD(); /*
        return (jint)LUA_OPADD;
    */

    protected static native int LUA_OPSUB(); /*
        return (jint)LUA_OPSUB;
    */

    protected static native int LUA_OPMUL(); /*
        return (jint)LUA_OPMUL;
    */

    protected static native int LUA_OPMOD(); /*
        return (jint)LUA_OPMOD;
    */

    protected static native int LUA_OPPOW(); /*
        return (jint)LUA_OPPOW;
    */

    protected static native int LUA_OPDIV(); /*
        return (jint)LUA_OPDIV;
    */

    protected static native int LUA_OPIDIV(); /*
        return (jint)LUA_OPIDIV;
    */

    protected static native int LUA_OPBAND(); /*
        return (jint)LUA_OPBAND;
    */

    protected static native int LUA_OPBOR(); /*
        return (jint)LUA_OPBOR;
    */

    protected static native int LUA_OPBXOR(); /*
        return (jint)LUA_OPBXOR;
    */

    protected static native int LUA_OPSHL(); /*
        return (jint)LUA_OPSHL;
    */

    protected static native int LUA_OPSHR(); /*
        return (jint)LUA_OPSHR;
    */

    protected static native int LUA_OPUNM(); /*
        return (jint)LUA_OPUNM;
    */

    protected static native int LUA_OPBNOT(); /*
        return (jint)LUA_OPBNOT;
    */

    protected static native int LUA_OPEQ(); /*
        return (jint)LUA_OPEQ;
    */

    protected static native int LUA_OPLT(); /*
        return (jint)LUA_OPLT;
    */

    protected static native int LUA_OPLE(); /*
        return (jint)LUA_OPLE;
    */

    protected static native int LUA_GCSTOP(); /*
        return (jint)LUA_GCSTOP;
    */

    protected static native int LUA_GCRESTART(); /*
        return (jint)LUA_GCRESTART;
    */

    protected static native int LUA_GCCOLLECT(); /*
        return (jint)LUA_GCCOLLECT;
    */

    protected static native int LUA_GCCOUNT(); /*
        return (jint)LUA_GCCOUNT;
    */

    protected static native int LUA_GCCOUNTB(); /*
        return (jint)LUA_GCCOUNTB;
    */

    protected static native int LUA_GCSTEP(); /*
        return (jint)LUA_GCSTEP;
    */

    protected static native int LUA_GCISRUNNING(); /*
        return (jint)LUA_GCISRUNNING;
    */

    protected static native int LUA_GCGEN(); /*
        return (jint)LUA_GCGEN;
    */

    protected static native int LUA_GCINC(); /*
        return (jint)LUA_GCINC;
    */

    protected static native int LUA_HOOKCALL(); /*
        return (jint)LUA_HOOKCALL;
    */

    protected static native int LUA_HOOKRET(); /*
        return (jint)LUA_HOOKRET;
    */

    protected static native int LUA_HOOKLINE(); /*
        return (jint)LUA_HOOKLINE;
    */

    protected static native int LUA_HOOKCOUNT(); /*
        return (jint)LUA_HOOKCOUNT;
    */

    protected static native int LUA_HOOKTAILCALL(); /*
        return (jint)LUA_HOOKTAILCALL;
    */

    protected static native int LUA_MASKCALL(); /*
        return (jint)LUA_MASKCALL;
    */

    protected static native int LUA_MASKRET(); /*
        return (jint)LUA_MASKRET;
    */

    protected static native int LUA_MASKLINE(); /*
        return (jint)LUA_MASKLINE;
    */

    protected static native int LUA_MASKCOUNT(); /*
        return (jint)LUA_MASKCOUNT;
    */

    protected static native String LUA_GNAME(); /*
        return env->NewStringUTF(LUA_GNAME);
    */

    protected static native String LUA_LOADED_TABLE(); /*
        return env->NewStringUTF(LUA_LOADED_TABLE);
    */

    protected static native String LUA_PRELOAD_TABLE(); /*
        return env->NewStringUTF(LUA_PRELOAD_TABLE);
    */

    protected static native int LUA_NOREF(); /*
        return (jint)LUA_NOREF;
    */

    protected static native int LUA_REFNIL(); /*
        return (jint)LUA_REFNIL;
    */

    protected static native String LUA_FILEHANDLE(); /*
        return env->NewStringUTF(LUA_FILEHANDLE);
    */

    protected static native int lua_absindex(long ptr, int idx); /*
        return (jint)lua_absindex((lua_State*)ptr, (int)idx);
    */

    protected static native void lua_arith(long ptr, int op); /*
        lua_arith((lua_State*)ptr, (int)op);
    */

    protected static native int lua_checkstack(long ptr, int n); /*
        return (jint)lua_checkstack((lua_State*)ptr, (int)n);
    */

    protected static native void lua_close(long ptr); /*
        lua_close((lua_State*)ptr);
    */

    protected static native void lua_closeslot(long ptr, int index); /*
        lua_closeslot((lua_State*)ptr, (int)index);
    */

    protected static native int lua_closethread(long ptr, long from); /*
        return (jint)lua_closethread((lua_State*)ptr, (lua_State*)from);
    */

    protected static native int lua_compare(long ptr, int index1, int index2, int op); /*
        return (jint)lua_compare((lua_State*)ptr, (int)index1, (int)index2, (int)op);
    */

    protected static native void lua_concat(long ptr, int n); /*
        lua_concat((lua_State*)ptr, (int)n);
    */

    protected static native void lua_copy(long ptr, int fromidx, int toidx); /*
        lua_copy((lua_State*)ptr, (int)fromidx, (int)toidx);
    */

    protected static native void lua_createtable(long ptr, int narr, int nrec); /*
        lua_createtable((lua_State*)ptr, (int)narr, (int)nrec);
    */

    protected static native int lua_error(long ptr); /*
        return (jint)lua_error((lua_State*)ptr);
    */

    protected static native int lua_getfield(long ptr, int index, String k); /*
        return (jint)lua_getfield((lua_State*)ptr, (int)index, k);
    */

    protected static native long lua_getextraspace(long ptr); /*
        return (jlong)lua_getextraspace((lua_State*)ptr);
    */

    protected static native int lua_getglobal(long ptr, String name); /*
        return (jint)lua_getglobal((lua_State*)ptr, name);
    */

    protected static native int lua_geti(long ptr, int index, long i); /*
        return (jint)lua_geti((lua_State*)ptr, (int)index, (lua_Integer)i);
    */

    protected static native int lua_getmetatable(long ptr, int index); /*
        return (jint)lua_getmetatable((lua_State*)ptr, (int)index);
    */

    protected static native int lua_gettable(long ptr, int index); /*
        return (jint)lua_gettable((lua_State*)ptr, (int)index);
    */

    protected static native int lua_gettop(long ptr); /*
        return (jint)lua_gettop((lua_State*)ptr);
    */

    protected static native int lua_getiuservalue(long ptr, int index, int n); /*
        return (jint)lua_getiuservalue((lua_State*)ptr, (int)index, (int)n);
    */

    protected static native void lua_insert(long ptr, int index); /*
        lua_insert((lua_State*)ptr, (int)index);
    */

    protected static native int lua_isboolean(long ptr, int index); /*
        return (jint)lua_isboolean((lua_State*)ptr, (int)index);
    */

    protected static native int lua_iscfunction(long ptr, int index); /*
        return (jint)lua_iscfunction((lua_State*)ptr, (int)index);
    */

    protected static native int lua_isfunction(long ptr, int index); /*
        return (jint)lua_isfunction((lua_State*)ptr, (int)index);
    */

    protected static native int lua_isinteger(long ptr, int index); /*
        return (jint)lua_isinteger((lua_State*)ptr, (int)index);
    */

    protected static native int lua_islightuserdata(long ptr, int index); /*
        return (jint)lua_islightuserdata((lua_State*)ptr, (int)index);
    */

    protected static native int lua_isnil(long ptr, int index); /*
        return (jint)lua_isnil((lua_State*)ptr, (int)index);
    */

    protected static native int lua_isnone(long ptr, int index); /*
        return (jint)lua_isnone((lua_State*)ptr, (int)index);
    */

    protected static native int lua_isnoneornil(long ptr, int index); /*
        return (jint)lua_isnoneornil((lua_State*)ptr, (int)index);
    */

    protected static native int lua_isnumber(long ptr, int index); /*
        return (jint)lua_isnumber((lua_State*)ptr, (int)index);
    */

    protected static native int lua_isstring(long ptr, int index); /*
        return (jint)lua_isstring((lua_State*)ptr, (int)index);
    */

    protected static native int lua_istable(long ptr, int index); /*
        return (jint)lua_istable((lua_State*)ptr, (int)index);
    */

    protected static native int lua_isthread(long ptr, int index); /*
        return (jint)lua_isthread((lua_State*)ptr, (int)index);
    */

    protected static native int lua_isuserdata(long ptr, int index); /*
        return (jint)lua_isuserdata((lua_State*)ptr, (int)index);
    */

    protected static native int lua_isyieldable(long ptr); /*
        return (jint)lua_isyieldable((lua_State*)ptr);
    */

    protected static native void lua_len(long ptr, int index); /*
        lua_len((lua_State*)ptr, (int)index);
    */

    protected static native void lua_newtable(long ptr); /*
        lua_newtable((lua_State*)ptr);
    */

    protected static native long lua_newthread(long ptr); /*
        return (jlong)lua_newthread((lua_State*)ptr);
    */

    protected static native long lua_newuserdatauv(long ptr, long size, int nuvalue); /*
        return (jlong)lua_newuserdatauv((lua_State*)ptr, (size_t)size, (int)nuvalue);
    */

    protected static native int lua_next(long ptr, int index); /*
        return (jint)lua_next((lua_State*)ptr, (int)index);
    */

    protected static native int lua_numbertointeger(double n, long p); /*
        return (jint)lua_numbertointeger((lua_Number)n, (lua_Integer*)p);
    */

    protected static native int lua_pcall(long ptr, int nargs, int nresults, int msgh); /*
        return (jint)lua_pcall((lua_State*)ptr, (int)nargs, (int)nresults, (int)msgh);
    */

    protected static native void lua_pop(long ptr, int n); /*
        lua_pop((lua_State*)ptr, (int)n);
    */

    protected static native void lua_pushboolean(long ptr, int b); /*
        lua_pushboolean((lua_State*)ptr, (int)b);
    */

    protected static native void lua_pushglobaltable(long ptr); /*
        lua_pushglobaltable((lua_State*)ptr);
    */

    protected static native void lua_pushinteger(long ptr, long n); /*
        lua_pushinteger((lua_State*)ptr, (lua_Integer)n);
    */

    protected static native void lua_pushlightuserdata(long ptr, long p); /*
        lua_pushlightuserdata((lua_State*)ptr, (void*)p);
    */

    protected static native void lua_pushnil(long ptr); /*
        lua_pushnil((lua_State*)ptr);
    */

    protected static native void lua_pushnumber(long ptr, double n); /*
        lua_pushnumber((lua_State*)ptr, (lua_Number)n);
    */

    protected static native String lua_pushstring(long ptr, String s); /*
        return env->NewStringUTF(lua_pushstring((lua_State*)ptr, s));
    */

    protected static native int lua_pushthread(long ptr); /*
        return (jint)lua_pushthread((lua_State*)ptr);
    */

    protected static native void lua_pushvalue(long ptr, int index); /*
        lua_pushvalue((lua_State*)ptr, (int)index);
    */

    protected static native int lua_rawequal(long ptr, int index1, int index2); /*
        return (jint)lua_rawequal((lua_State*)ptr, (int)index1, (int)index2);
    */

    protected static native int lua_rawget(long ptr, int index); /*
        return (jint)lua_rawget((lua_State*)ptr, (int)index);
    */

    protected static native int lua_rawgeti(long ptr, int index, long n); /*
        return (jint)lua_rawgeti((lua_State*)ptr, (int)index, (lua_Integer)n);
    */

    protected static native int lua_rawgetp(long ptr, int index, long p); /*
        return (jint)lua_rawgetp((lua_State*)ptr, (int)index, (const void*)p);
    */

    protected static native long lua_rawlen(long ptr, int index); /*
        return (jlong)lua_rawlen((lua_State*)ptr, (int)index);
    */

    protected static native void lua_rawset(long ptr, int index); /*
        lua_rawset((lua_State*)ptr, (int)index);
    */

    protected static native void lua_rawseti(long ptr, int index, long i); /*
        lua_rawseti((lua_State*)ptr, (int)index, (lua_Integer)i);
    */

    protected static native void lua_rawsetp(long ptr, int index, long p); /*
        lua_rawsetp((lua_State*)ptr, (int)index, (const void*)p);
    */

    protected static native void lua_remove(long ptr, int index); /*
        lua_remove((lua_State*)ptr, (int)index);
    */

    protected static native void lua_replace(long ptr, int index); /*
        lua_replace((lua_State*)ptr, (int)index);
    */

    protected static native int lua_resetthread(long ptr); /*
        return (jint)lua_resetthread((lua_State*)ptr);
    */

    protected static native int lua_resume(long ptr, long from, int nargs, long nresults); /*
        return (jint)lua_resume((lua_State*)ptr, (lua_State*)from, (int)nargs, (int*)nresults);
    */

    protected static native void lua_rotate(long ptr, int idx, int n); /*
        lua_rotate((lua_State*)ptr, (int)idx, (int)n);
    */

    protected static native void lua_setfield(long ptr, int index, String k); /*
        lua_setfield((lua_State*)ptr, (int)index, k);
    */

    protected static native void lua_setglobal(long ptr, String name); /*
        lua_setglobal((lua_State*)ptr, name);
    */

    protected static native void lua_seti(long ptr, int index, long n); /*
        lua_seti((lua_State*)ptr, (int)index, (lua_Integer)n);
    */

    protected static native int lua_setiuservalue(long ptr, int index, int n); /*
        return (jint)lua_setiuservalue((lua_State*)ptr, (int)index, (int)n);
    */

    protected static native int lua_setmetatable(long ptr, int index); /*
        return (jint)lua_setmetatable((lua_State*)ptr, (int)index);
    */

    protected static native void lua_settable(long ptr, int index); /*
        lua_settable((lua_State*)ptr, (int)index);
    */

    protected static native void lua_settop(long ptr, int index); /*
        lua_settop((lua_State*)ptr, (int)index);
    */

    protected static native int lua_status(long ptr); /*
        return (jint)lua_status((lua_State*)ptr);
    */

    protected static native long lua_stringtonumber(long ptr, String s); /*
        return (jlong)lua_stringtonumber((lua_State*)ptr, s);
    */

    protected static native int lua_toboolean(long ptr, int index); /*
        return (jint)lua_toboolean((lua_State*)ptr, (int)index);
    */

    protected static native void lua_toclose(long ptr, int index); /*
        lua_toclose((lua_State*)ptr, (int)index);
    */

    protected static native long lua_tointeger(long ptr, int index); /*
        return (jlong)lua_tointeger((lua_State*)ptr, (int)index);
    */

    protected static native long lua_tointegerx(long ptr, int index, long isnum); /*
        return (jlong)lua_tointegerx((lua_State*)ptr, (int)index, (int*)isnum);
    */

    protected static native double lua_tonumber(long ptr, int index); /*
        return (jdouble)lua_tonumber((lua_State*)ptr, (int)index);
    */

    protected static native double lua_tonumberx(long ptr, int index, long isnum); /*
        return (jdouble)lua_tonumberx((lua_State*)ptr, (int)index, (int*)isnum);
    */

    protected static native long lua_topointer(long ptr, int index); /*
        return (jlong)lua_topointer((lua_State*)ptr, (int)index);
    */

    protected static native String lua_tostring(long ptr, int index); /*
        return env->NewStringUTF(lua_tostring((lua_State*)ptr, (int)index));
    */

    protected static native long lua_tothread(long ptr, int index); /*
        return (jlong)lua_tothread((lua_State*)ptr, (int)index);
    */

    protected static native long lua_touserdata(long ptr, int index); /*
        return (jlong)lua_touserdata((lua_State*)ptr, (int)index);
    */

    protected static native int lua_type(long ptr, int index); /*
        return (jint)lua_type((lua_State*)ptr, (int)index);
    */

    protected static native String lua_typename(long ptr, int tp); /*
        return env->NewStringUTF(lua_typename((lua_State*)ptr, (int)tp));
    */

    protected static native int lua_upvalueindex(int i); /*
        return (jint)lua_upvalueindex((int)i);
    */

    protected static native double lua_version(long ptr); /*
        return (jdouble)lua_version((lua_State*)ptr);
    */

    protected static native void lua_warning(long ptr, String msg, int tocont); /*
        lua_warning((lua_State*)ptr, msg, (int)tocont);
    */

    protected static native void lua_xmove(long from, long to, int n); /*
        lua_xmove((lua_State*)from, (lua_State*)to, (int)n);
    */

    protected static native int lua_yield(long ptr, int nresults); /*
        return (jint)lua_yield((lua_State*)ptr, (int)nresults);
    */

    protected static native int luaL_callmeta(long ptr, int obj, String e); /*
        return (jint)luaL_callmeta((lua_State*)ptr, (int)obj, e);
    */

    protected static native int luaL_dostring(long ptr, String str); /*
        return (jint)luaL_dostring((lua_State*)ptr, str);
    */

    protected static native int luaL_execresult(long ptr, int stat); /*
        return (jint)luaL_execresult((lua_State*)ptr, (int)stat);
    */

    protected static native int luaL_fileresult(long ptr, int stat, String fname); /*
        return (jint)luaL_fileresult((lua_State*)ptr, (int)stat, fname);
    */

    protected static native int luaL_getmetafield(long ptr, int obj, String e); /*
        return (jint)luaL_getmetafield((lua_State*)ptr, (int)obj, e);
    */

    protected static native int luaL_getmetatable(long ptr, String tname); /*
        return (jint)luaL_getmetatable((lua_State*)ptr, tname);
    */

    protected static native int luaL_getsubtable(long ptr, int idx, String fname); /*
        return (jint)luaL_getsubtable((lua_State*)ptr, (int)idx, fname);
    */

    protected static native String luaL_gsub(long ptr, String s, String p, String r); /*
        return env->NewStringUTF(luaL_gsub((lua_State*)ptr, s, p, r));
    */

    protected static native long luaL_len(long ptr, int index); /*
        return (jlong)luaL_len((lua_State*)ptr, (int)index);
    */

    protected static native int luaL_loadstring(long ptr, String s); /*
        return (jint)luaL_loadstring((lua_State*)ptr, s);
    */

    protected static native int luaL_newmetatable(long ptr, String tname); /*
        return (jint)luaL_newmetatable((lua_State*)ptr, tname);
    */

    protected static native long luaL_newstate(); /*
        return (jlong)luaL_newstate();
    */

    protected static native void luaL_openlibs(long ptr); /*
        luaL_openlibs((lua_State*)ptr);
    */

    protected static native void luaL_pushfail(long ptr); /*
        luaL_pushfail((lua_State*)ptr);
    */

    protected static native int luaL_ref(long ptr, int t); /*
        return (jint)luaL_ref((lua_State*)ptr, (int)t);
    */

    protected static native void luaL_setmetatable(long ptr, String tname); /*
        luaL_setmetatable((lua_State*)ptr, tname);
    */

    protected static native long luaL_testudata(long ptr, int arg, String tname); /*
        return (jlong)luaL_testudata((lua_State*)ptr, (int)arg, tname);
    */

    protected static native String luaL_tolstring(long ptr, int idx, long len); /*
        return env->NewStringUTF(luaL_tolstring((lua_State*)ptr, (int)idx, (size_t*)len));
    */

    protected static native void luaL_traceback(long ptr, long ptr1, String msg, int level); /*
        luaL_traceback((lua_State*)ptr, (lua_State*)ptr1, msg, (int)level);
    */

    protected static native String luaL_typename(long ptr, int index); /*
        return env->NewStringUTF(luaL_typename((lua_State*)ptr, (int)index));
    */

    protected static native void luaL_unref(long ptr, int t, int ref); /*
        luaL_unref((lua_State*)ptr, (int)t, (int)ref);
    */

    protected static native void luaL_where(long ptr, int lvl); /*
        luaL_where((lua_State*)ptr, (int)lvl);
    */

    protected static native int init_bindings(); /*
        if (update_env(env) != 0) {
            return -1;
        }
        jclass local_ref = env->FindClass("me/white/wlua/LuaNatives");
        if (!env->ExceptionOccurred()) {
            natives_class = (jclass)env->NewGlobalRef(local_ref);
        }
        invoke_method = env->GetStaticMethodID(natives_class, "invoke", "(ILjava/lang/Object;I)I");
        adopt_method = env->GetStaticMethodID(natives_class, "adopt", "(IJ)I");
        jclass throwable_class = env->FindClass("java/lang/Throwable");
        throwable_tostring_method = env->GetMethodID(throwable_class, "toString", "()Ljava/lang/String;");
        return env->ExceptionOccurred() ? -1 : 0;
    */

    protected static native void init_state(long ptr, int id); /*
        lua_State* L = (lua_State*)ptr;
        if (luaL_newmetatable(L, JAVA_OBJECT_METATABLE) == 1) {
            lua_pushcfunction(L, &gc_java);
            lua_setfield(L, -2, "__gc");
        }
        lua_pop(L, 1);
        lua_pushstring(L, JAVA_STATE_INDEX);
        lua_pushinteger(L, id);
        lua_rawset(L, LUA_REGISTRYINDEX);
    */

    protected static native void push_function(long ptr, JavaFunction function); /*
        lua_State* L = (lua_State*)ptr;
        jobject global_ref = env->NewGlobalRef(function);
        if (env->ExceptionOccurred()) {
            return;
        }
        jobject* userdata = (jobject*)lua_newuserdatauv(L, sizeof(global_ref), 0);
        *userdata = function;
        luaL_setmetatable(L, JAVA_OBJECT_METATABLE);
        lua_pushcclosure(L, &function_wrapper, 1);
    */
}

package me.white.wlua;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

class LuaBindings {
    private static final MemoryLayout INTEGER_LAYOUT = ValueLayout.JAVA_LONG;
    private static final MemoryLayout NUMBER_LAYOUT = ValueLayout.JAVA_DOUBLE;
    private static final MemoryLayout UNSIGNED_LAYOUT = ValueLayout.JAVA_LONG;
    private static final MemoryLayout CONTEXT_LAYOUT = ValueLayout.JAVA_LONG;
    private static final MemoryLayout SIZE_LAYOUT = ValueLayout.JAVA_INT;
    private static final MethodHandle ABSINDEX_HANDLE = downcallHandle("lua_absindex", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle ARITH_HANDLE = downcallHandle("lua_arith", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle ATPANIC_HANDLE = downcallHandle("lua_atpanic", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle CALLK_HANDLE = downcallHandle("lua_callk", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle CHECKSTACK_HANDLE = downcallHandle("lua_checkstack", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle CLOSE_HANDLE = downcallHandle("lua_close", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
    private static final MethodHandle CLOSESLOT_HANDLE = downcallHandle("lua_closeslot", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle CLOSETHREAD_HANDLE = downcallHandle("lua_closethread", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle COMPARE_HANDLE = downcallHandle("lua_compare", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
    private static final MethodHandle CONCAT_HANDLE = downcallHandle("lua_concat", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle COPY_HANDLE = downcallHandle("lua_copy", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
    private static final MethodHandle CREATETABLE_HANDLE = downcallHandle("lua_createtable", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
    private static final MethodHandle DUMP_HANDLE = downcallHandle("lua_dump", FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle ERROR_HANDLE = downcallHandle("lua_error", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle GC_HANDLE = downcallHandle("lua_gc", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle GC_STEP_HANDLE = downcallHandle("lua_gc", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
    private static final MethodHandle GC_INC_HANDLE = downcallHandle("lua_gc", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
    private static final MethodHandle GC_GEN_HANDLE = downcallHandle("lua_gc", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
    private static final MethodHandle GETALLOCF_HANDLE = downcallHandle("lua_getallocf", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle GETFIELD_HANDLE = downcallHandle("lua_getfield", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle GETGLOBAL_HANDLE = downcallHandle("lua_getglobal", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle GETHOOK_HANDLE = downcallHandle("lua_gethook", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle GETHOOKCOUNT_HANDLE = downcallHandle("lua_gethookcount", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle GETHOOKMASK_HANDLE = downcallHandle("lua_gethookmask", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle GETI_HANDLE = downcallHandle("lua_geti", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, INTEGER_LAYOUT));
    private static final MethodHandle GETINFO_HANDLE = downcallHandle("lua_getinfo", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle GETIUSERVALUE_HANDLE = downcallHandle("lua_getiuservalue", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
    private static final MethodHandle GETLOCAL_HANDLE = downcallHandle("lua_getlocal", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle GETMETATABLE_HANDLE = downcallHandle("lua_getmetatable", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle GETSTACK_HANDLE = downcallHandle("lua_getstack", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle GETTABLE_HANDLE = downcallHandle("lua_gettable", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle GETTOP_HANDLE = downcallHandle("lua_gettop", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle GETUPVALUE_HANDLE = downcallHandle("lua_getupvalue", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
    private static final MethodHandle ISCFUNCTION_HANDLE = downcallHandle("lua_iscfunction", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle ISINTEGER_HANDLE = downcallHandle("lua_isinteger", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle ISNUMBER_HANDLE = downcallHandle("lua_isnumber", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle ISSTRING_HANDLE = downcallHandle("lua_isstring", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle ISUSERDATA_HANDLE = downcallHandle("lua_isuserdata", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle ISYIELDABLE_HANDLE = downcallHandle("lua_isyieldable", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle LEN_HANDLE = downcallHandle("lua_len", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle LOAD_HANDLE = downcallHandle("lua_load", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle NEWSTATE_HANDLE = downcallHandle("lua_newstate", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle NEWTHREAD_HANDLE = downcallHandle("lua_newthread", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle NEWUSERDATAUV_HANDLE = downcallHandle("lua_newuserdatauv", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, SIZE_LAYOUT, ValueLayout.JAVA_INT));
    private static final MethodHandle NEXT_HANDLE = downcallHandle("lua_next", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle PCALLK_HANDLE = downcallHandle("lua_pcallk", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, CONTEXT_LAYOUT, ValueLayout.ADDRESS));
    private static final MethodHandle PUSHBOOLEAN_HANDLE = downcallHandle("lua_pushboolean", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle PUSHCCLOSURE_HANDLE = downcallHandle("lua_pushcclosure", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle PUSHFSTRING_HANDLE = downcallHandle("lua_pushfstring", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)); // vararg
    private static final MethodHandle PUSHINTEGER_HANDLE = downcallHandle("lua_pushinteger", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, INTEGER_LAYOUT));
    private static final MethodHandle PUSHLIGHTUSERDATA_HANDLE = downcallHandle("lua_pushlightuserdata", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle PUSHLSTRING_HANDLE = downcallHandle("lua_pushlstring", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, SIZE_LAYOUT));
    private static final MethodHandle PUSHNIL_HANDLE = downcallHandle("lua_pushnil", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
    private static final MethodHandle PUSHNUMBER_HANDLE = downcallHandle("lua_pushnumber", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, NUMBER_LAYOUT));
    private static final MethodHandle PUSHSTRING_HANDLE = downcallHandle("lua_pushstring", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle PUSHTHREAD_HANDLE = downcallHandle("lua_pushthread", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle PUSHVALUE_HANDLE = downcallHandle("lua_pushvalue", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle PUSHVFSTRING_HANDLE = downcallHandle("lua_pushvfstring", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)); // va_list?
    private static final MethodHandle RAWEQUAL_HANDLE = downcallHandle("lua_rawequal", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
    private static final MethodHandle RAWGET_HANDLE = downcallHandle("lua_rawget", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle RAWGETI_HANDLE = downcallHandle("lua_rawgeti", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, INTEGER_LAYOUT));
    private static final MethodHandle RAWGETP_HANDLE = downcallHandle("lua_rawgetp", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle RAWLEN_HANDLE = downcallHandle("lua_rawlen", FunctionDescriptor.of(UNSIGNED_LAYOUT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle RAWSET_HANDLE = downcallHandle("lua_rawset", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle RAWSETI_HANDLE = downcallHandle("lua_rawseti", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, INTEGER_LAYOUT));
    private static final MethodHandle RAWSETP_HANDLE = downcallHandle("lua_rawsetp", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle RESETTHREAD_HANDLE = downcallHandle("lua_resetthread", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle RESUME_HANDLE = downcallHandle("lua_resume", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle ROTATE_HANDLE = downcallHandle("lua_rotate", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
    private static final MethodHandle SETALLOCF_HANDLE = downcallHandle("lua_setallocf", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle SETFIELD_HANDLE = downcallHandle("lua_setfield", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle SETGLOBAL_HANDLE = downcallHandle("lua_setglobal", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle SETHOOK_HANDLE = downcallHandle("lua_sethook", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
    private static final MethodHandle SETI_HANDLE = downcallHandle("lua_seti", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, INTEGER_LAYOUT));
    private static final MethodHandle SETLOCAL_HANDLE = downcallHandle("lua_setlocal", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle SETMETATABLE_HANDLE = downcallHandle("lua_setmetatable", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle SETTABLE_HANDLE = downcallHandle("lua_settable", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle SETTOP_HANDLE = downcallHandle("lua_settop", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle SETUPVALUE_HANDLE = downcallHandle("lua_setupvalue", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
    private static final MethodHandle SETWARNF_HANDLE = downcallHandle("lua_setwarnf", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle STATUS_HANDLE = downcallHandle("lua_status", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle STRINGTONUMBER_HANDLE = downcallHandle("lua_stringtonumber", FunctionDescriptor.of(SIZE_LAYOUT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle TOBOOLEAN_HANDLE = downcallHandle("lua_toboolean", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle TOCFUNCTION_HANDLE = downcallHandle("lua_tocfunction", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle TOCLOSE_HANDLE = downcallHandle("lua_toclose", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle TOINTEGERX_HANDLE = downcallHandle("lua_tointegerx", FunctionDescriptor.of(INTEGER_LAYOUT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle TOLSTRING_HANDLE = downcallHandle("lua_tolstring", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle TONUMBERX_HANDLE = downcallHandle("lua_tonumberx", FunctionDescriptor.of(NUMBER_LAYOUT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle TOPOINTER_HANDLE = downcallHandle("lua_topointer", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle TOTHREAD_HANDLE = downcallHandle("lua_tothread", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle TOUSERDATA_HANDLE = downcallHandle("lua_touserdata", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle TYPE_HANDLE = downcallHandle("lua_type", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle TYPENAME_HANDLE = downcallHandle("lua_typename", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle UPVALUEID_HANDLE = downcallHandle("lua_upvalueid", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
    private static final MethodHandle UPVALUEJOIN_HANDLE = downcallHandle("lua_upvaluejoin", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
    private static final MethodHandle VERSION_HANDLE = downcallHandle("lua_version", FunctionDescriptor.of(NUMBER_LAYOUT, ValueLayout.ADDRESS));
    private static final MethodHandle WARNING_HANDLE = downcallHandle("lua_warning", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle XMOVE_HANDLE = downcallHandle("lua_xmove", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle YIELDK_HANDLE = downcallHandle("lua_yieldk", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, CONTEXT_LAYOUT, ValueLayout.ADDRESS));
    private static final MethodHandle AUXILIARY_ADDGSUB_HANDLE = downcallHandle("luaL_addgsub", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle AUXILIARY_ADDLSTRING_HANDLE = downcallHandle("luaL_addlstring", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, SIZE_LAYOUT));
    private static final MethodHandle AUXILIARY_ADDSTRING_HANDLE = downcallHandle("luaL_addstring", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle AUXILIARY_ADDVALUE_HANDLE = downcallHandle("luaL_addvalue", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
    private static final MethodHandle AUXILIARY_ARGERROR_HANDLE = downcallHandle("luaL_argerror", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle AUXILIARY_BUFFINIT_HANDLE = downcallHandle("luaL_buffinit", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle AUXILIARY_BUFFINITSIZE_HANDLE = downcallHandle("luaL_buffinitsize", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, SIZE_LAYOUT));
    private static final MethodHandle AUXILIARY_CALLMETA_HANDLE = downcallHandle("luaL_callmeta", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle AUXILIARY_CHECKANY_HANDLE = downcallHandle("luaL_checkany", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle AUXILIARY_CHECKINTEGER_HANDLE = downcallHandle("luaL_checkinteger", FunctionDescriptor.of(INTEGER_LAYOUT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle AUXILIARY_CHECKLSTRING_HANDLE = downcallHandle("luaL_checklstring", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle AUXILIARY_CHECKNUMBER_HANDLE = downcallHandle("luaL_checknumber", FunctionDescriptor.of(NUMBER_LAYOUT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle AUXILIARY_CHECKOPTION_HANDLE = downcallHandle("luaL_checkoption", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle AUXILIARY_CHECKSTACK_HANDLE = downcallHandle("luaL_checkstack", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle AUXILIARY_CHECKTYPE_HANDLE = downcallHandle("luaL_checktype", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
    private static final MethodHandle AUXILIARY_CHECKUDATA_HANDLE = downcallHandle("luaL_checkudata", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle AUXILIARY_CHECKVERSION__HANDLE = downcallHandle("luaL_checkversion_", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
    private static final MethodHandle AUXILIARY_ERROR_HANDLE = downcallHandle("luaL_error", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS)); // vararg
    private static final MethodHandle AUXILIARY_EXECRESULT_HANDLE = downcallHandle("luaL_execresult", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle AUXILIARY_FILERESULT_HANDLE = downcallHandle("luaL_fileresult", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle AUXILIARY_GETMETAFIELD_HANDLE = downcallHandle("luaL_getmetafield", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle AUXILIARY_GETSUBTABLE_HANDLE = downcallHandle("luaL_getsubtable", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle AUXILIARY_GSUB_HANDLE = downcallHandle("luaL_gsub", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle AUXILIARY_LEN_HANDLE = downcallHandle("luaL_len", FunctionDescriptor.of(INTEGER_LAYOUT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle AUXILIARY_LOADBUFFERX_HANDLE = downcallHandle("luaL_loadbufferx", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, SIZE_LAYOUT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle AUXILIARY_LOADFILEX_HANDLE = downcallHandle("luaL_loadfilex", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle AUXILIARY_LOADSTRING_HANDLE = downcallHandle("luaL_loadstring", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle AUXILIARY_NEWMETATABLE_HANDLE = downcallHandle("luaL_newmetatable", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle AUXILIARY_NEWSTATE_HANDLE = downcallHandle("luaL_newstate", FunctionDescriptor.of(ValueLayout.ADDRESS));
    private static final MethodHandle AUXILIARY_OPENLIBS_HANDLE = downcallHandle("luaL_openlibs", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
    private static final MethodHandle AUXILIARY_OPTINTEGER_HANDLE = downcallHandle("luaL_optinteger", FunctionDescriptor.of(INTEGER_LAYOUT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, INTEGER_LAYOUT));
    private static final MethodHandle AUXILIARY_OPTLSTRING_HANDLE = downcallHandle("luaL_optlstring", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle AUXILIARY_PREPBUFFSIZE_HANDLE = downcallHandle("luaL_prepbuffsize", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, SIZE_LAYOUT));
    private static final MethodHandle AUXILIARY_PUSHRESULT_HANDLE = downcallHandle("luaL_pushresult", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
    private static final MethodHandle AUXILIARY_PUSHRESULTSIZE_HANDLE = downcallHandle("luaL_pushresultsize", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, SIZE_LAYOUT));
    private static final MethodHandle AUXILIARY_REF_HANDLE = downcallHandle("luaL_ref", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle AUXILIARY_REQUIREF_HANDLE = downcallHandle("luaL_requiref", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle AUXILIARY_SETFUNCS_HANDLE = downcallHandle("luaL_setfuncs", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle AUXILIARY_SETMETATABLE_HANDLE = downcallHandle("luaL_setmetatable", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    private static final MethodHandle AUXILIARY_TESTUDATA_HANDLE = downcallHandle("luaL_testudata", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle AUXILIARY_TOLSTRING_HANDLE = downcallHandle("luaL_tolstring", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle AUXILIARY_TRACEBACK_HANDLE = downcallHandle("luaL_traceback", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle AUXILIARY_TYPEERROR_HANDLE = downcallHandle("luaL_typerror", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle AUXILIARY_UNREF_HANDLE = downcallHandle("luaL_unref", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
    private static final MethodHandle AUXILIARY_WHERE_HANDLE = downcallHandle("luaL_where", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    private static final MethodHandle OPEN_BASE_HANDLE = downcallHandle("luaopen_base", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle OPEN_COROUTINE_HANDLE = downcallHandle("luaopen_coroutine", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle OPEN_DEBUG_HANDLE = downcallHandle("luaopen_debug", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle OPEN_IO_HANDLE = downcallHandle("luaopen_io", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle OPEN_MATH_HANDLE = downcallHandle("luaopen_math", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle OPEN_OS_HANDLE = downcallHandle("luaopen_os", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle OPEN_PACKAGE_HANDLE = downcallHandle("luaopen_package", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle OPEN_STRING_HANDLE = downcallHandle("luaopen_string", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle OPEN_TABLE_HANDLE = downcallHandle("luaopen_table", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final MethodHandle OPEN_UTF8_HANDLE = downcallHandle("luaopen_utf8", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    private static final FunctionDescriptor ALLOC_DESCRIPTOR = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG);
    private static final FunctionDescriptor C_FUNCTION_DESCRIPTOR = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
    private static final FunctionDescriptor HOOK_DESCRIPTOR = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS);
    private static final FunctionDescriptor K_FUNCTION_DESCRIPTOR = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, CONTEXT_LAYOUT);
    private static final FunctionDescriptor READER_DESCRIPTOR = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
    private static final FunctionDescriptor WARN_FUNCTION_DESCRIPTOR = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
    private static final FunctionDescriptor WRITER_DESCRIPTOR = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS);
    private static final MethodHandle ALLOC_HANDLE = upcallHandle(Alloc.class, "apply", ALLOC_DESCRIPTOR);
    private static final MethodHandle C_FUNCTION_HANDLE = upcallHandle(CFunction.class, "apply", C_FUNCTION_DESCRIPTOR);
    private static final MethodHandle HOOK_HANDLE = upcallHandle(Hook.class, "apply", HOOK_DESCRIPTOR);
    private static final MethodHandle K_FUNCTION_HANDLE = upcallHandle(KFunction.class, "apply", K_FUNCTION_DESCRIPTOR);
    private static final MethodHandle READER_HANDLE = upcallHandle(Reader.class, "apply", READER_DESCRIPTOR);
    private static final MethodHandle WARN_FUNCTION_HANDLE = upcallHandle(WarnFunction.class, "apply", WARN_FUNCTION_DESCRIPTOR);
    private static final MethodHandle WRITER_HANDLE = upcallHandle(Writer.class, "apply", WRITER_DESCRIPTOR);
    private static final SymbolLookup SYMBOL_LOOKUP = SymbolLookup.loaderLookup().or(Linker.nativeLinker().defaultLookup());
    private static boolean isLoaded = false;
    static final int REGISTRYINDEX = -1001000;
    static final int RIDX_MAINTHREAD = 1;
    static final int RIDX_GLOBALS = 2;
    static final int MULTRET = -1;
    static final int OK = 0;
    static final int YIELD = 1;
    static final int ERRRUN = 2;
    static final int ERRSYNTAX = 3;
    static final int ERRMEM = 4;
    static final int ERRERR = 5;
    static final int TNIL = 0;
    static final int TBOOLEAN = 1;
    static final int TNUMBER = 3;
    static final int TSTRING = 4;
    static final int TTABLE = 5;
    static final int TFUNCTION = 6;
    static final int TUSERDATA = 7;
    static final int TTHREAD = 8;
    static final int OPADD = 0;
    static final int OPSUB = 1;
    static final int OPMUL = 2;
    static final int OPMOD = 3;
    static final int OPPOW = 4;
    static final int OPDIV = 5;
    static final int OPIDIV = 6;
    static final int OPBAND = 7;
    static final int OPBOR = 8;
    static final int OPBXOR = 9;
    static final int OPSHL = 10;
    static final int OPSHR = 11;
    static final int OPUNM = 12;
    static final int OPBNOT = 13;
    static final int OPEQ = 0;
    static final int OPLT = 1;
    static final int OPLE = 2;

    private static MethodHandle downcallHandle(String name, FunctionDescriptor descriptor) {
        if (!isLoaded) {
            System.loadLibrary("lua54");
            isLoaded = true;
        }
        MemorySegment address = SYMBOL_LOOKUP.find(name).orElseThrow(() -> new UnsatisfiedLinkError("lua is not loaded"));
        return Linker.nativeLinker().downcallHandle(address, descriptor);
    }

    private static MethodHandle upcallHandle(Class<?> clazz, String name, FunctionDescriptor descriptor) {
        try {
            return MethodHandles.lookup().findVirtual(clazz, name, descriptor.toMethodType());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static Object invoke(MethodHandle handle, Object... args) {
        try {
            return handle.invokeExact(args);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int absindex(MemorySegment L, int idx) {
        return (Integer)invoke(ABSINDEX_HANDLE, L, idx);
    }

    static void arith(MemorySegment L, int op) {
        invoke(ARITH_HANDLE, L, op);
    }

    static MemorySegment atpanic(MemorySegment L, MemorySegment panicf) {
        return (MemorySegment)invoke(ATPANIC_HANDLE, L, panicf);
    }

    static void callk(MemorySegment L, int nargs, int nresults, int ctx, MemorySegment k) {
        invoke(CALLK_HANDLE, L, nargs, nresults, ctx, k);
    }

    static int checkstack(MemorySegment L, int n) {
        return (Integer)invoke(CHECKSTACK_HANDLE, L, n);
    }

    static void close(MemorySegment L) {
        invoke(CLOSE_HANDLE, L);
    }

    static void closeslot(MemorySegment L, int index) {
        invoke(CLOSESLOT_HANDLE, L, index);
    }

    static int closethread(MemorySegment L, MemorySegment from) {
        return (Integer)invoke(CLOSETHREAD_HANDLE, L, from);
    }

    static int compare(MemorySegment L, int index1, int index2, int op) {
        return (Integer)invoke(COMPARE_HANDLE, L, index1, index2, op);
    }

    static void concat(MemorySegment L, int n) {
        invoke(CONCAT_HANDLE, L, n);
    }

    static void copy(MemorySegment L, int fromidx, int toidx) {
        invoke(COPY_HANDLE, L, fromidx, toidx);
    }

    static void createtable(MemorySegment L, int narr, int nrec) {
        invoke(CREATETABLE_HANDLE, L, narr, nrec);
    }

    static int dump(MemorySegment L, MemorySegment writer, MemorySegment data, int strip) {
        return (Integer)invoke(DUMP_HANDLE, L, writer, data, strip);
    }

    static int error(MemorySegment L) {
        return (Integer)invoke(ERROR_HANDLE, L);
    }

    static int gc(MemorySegment L, int what) {
        return (Integer)invoke(GC_HANDLE, L, what);
    }

    static int gcStep(MemorySegment L, int what, int stepsize) {
        return (Integer)invoke(GC_STEP_HANDLE, L, what, stepsize);
    }

    static int gcInc(MemorySegment L, int what, int pause, int stepmul, int stepsize) {
        return (Integer)invoke(GC_INC_HANDLE, L, what, pause, stepmul, stepsize);
    }

    static int gcGen(MemorySegment L, int what, int minormul, int majormul) {
        return (Integer)invoke(GC_GEN_HANDLE, L, what, minormul, majormul);
    }

    static MemorySegment getallocf(MemorySegment L, MemorySegment ud) {
        return (MemorySegment)invoke(GETALLOCF_HANDLE, L, ud);
    }

    static int getfield(MemorySegment L, int index, MemorySegment k) {
        return (Integer)invoke(GETFIELD_HANDLE, L, index, k);
    }

    static int getglobal(MemorySegment L, MemorySegment name) {
        return (Integer)invoke(GETGLOBAL_HANDLE, L, name);
    }

    static MemorySegment gethook(MemorySegment L) {
        return (MemorySegment)invoke(GETHOOK_HANDLE, L);
    }

    static int gethookcount(MemorySegment L) {
        return (Integer)invoke(GETHOOKCOUNT_HANDLE, L);
    }

    static int gethookmask(MemorySegment L) {
        return (Integer)invoke(GETHOOKMASK_HANDLE, L);
    }

    static int geti(MemorySegment L, int index, long i) {
        return (Integer)invoke(GETI_HANDLE, L, index, i);
    }

    static int getinfo(MemorySegment L, MemorySegment what, MemorySegment ar) {
        return (Integer)invoke(GETINFO_HANDLE, L, what, ar);
    }

    static int getiuservalue(MemorySegment L, int index, int n) {
        return (Integer)invoke(GETIUSERVALUE_HANDLE, L, index, n);
    }

    static MemorySegment getlocal(MemorySegment L, MemorySegment ar, int n) {
        return (MemorySegment)invoke(GETLOCAL_HANDLE, L, ar, n);
    }

    static int getmetatable(MemorySegment L, MemorySegment tname) {
        return (Integer)invoke(GETMETATABLE_HANDLE, L, tname);
    }

    static int getstack(MemorySegment L, int level, MemorySegment ar) {
        return (Integer)invoke(GETSTACK_HANDLE, L, level, ar);
    }

    static int gettable(MemorySegment L, int index) {
        return (Integer)invoke(GETTABLE_HANDLE, L, index);
    }

    static int gettop(MemorySegment L) {
        return (Integer)invoke(GETTOP_HANDLE, L);
    }

    static MemorySegment getupvalue(MemorySegment L, int funcindex, int n) {
        return (MemorySegment)invoke(GETUPVALUE_HANDLE, L, funcindex, n);
    }

    static int iscfunction(MemorySegment L, int index) {
        return (Integer)invoke(ISCFUNCTION_HANDLE, L, index);
    }

    static int isinteger(MemorySegment L, int index) {
        return (Integer)invoke(ISINTEGER_HANDLE, L, index);
    }

    static int isnumber(MemorySegment L, int index) {
        return (Integer)invoke(ISNUMBER_HANDLE, L, index);
    }

    static int isstring(MemorySegment L, int index) {
        return (Integer)invoke(ISSTRING_HANDLE, L, index);
    }

    static int isuserdata(MemorySegment L, int index) {
        return (Integer)invoke(ISUSERDATA_HANDLE, L, index);
    }

    static int isyieldable(MemorySegment L) {
        return (Integer)invoke(ISYIELDABLE_HANDLE, L);
    }

    static void len(MemorySegment L, int index) {
        invoke(LEN_HANDLE, L, index);
    }

    static int load(MemorySegment L, MemorySegment reader, MemorySegment data, MemorySegment chunkname, MemorySegment mode) {
        return (Integer)invoke(LOAD_HANDLE, L, reader, data, chunkname, mode);
    }

    static MemorySegment newstate(MemorySegment f, MemorySegment ud) {
        return (MemorySegment)invoke(NEWSTATE_HANDLE, f, ud);
    }

    static MemorySegment newthread(MemorySegment L) {
        return (MemorySegment)invoke(NEWTHREAD_HANDLE, L);
    }

    static MemorySegment newuserdatauv(MemorySegment L, int size, int nuvalue) {
        return (MemorySegment)invoke(NEWUSERDATAUV_HANDLE, L, size, nuvalue);
    }

    static int next(MemorySegment L, int index) {
        return (Integer)invoke(NEXT_HANDLE, L, index);
    }

    static int pcallk(MemorySegment L, int nargs, int nresults, int msgh, long ctx, MemorySegment k) {
        return (Integer)invoke(PCALLK_HANDLE, L, nargs, nresults, msgh, ctx, k);
    }

    static void pushboolean(MemorySegment L, int b) {
        invoke(PUSHBOOLEAN_HANDLE, L, b);
    }

    static void pushcclosure(MemorySegment L, MemorySegment fn, int n) {
        invoke(PUSHCCLOSURE_HANDLE, L, fn, n);
    }

    static MemorySegment pushfstring(MemorySegment L, MemorySegment fmt) {
        return (MemorySegment)invoke(PUSHFSTRING_HANDLE, L, fmt);
    }

    static void pushinteger(MemorySegment L, long n) {
        invoke(PUSHINTEGER_HANDLE, L, n);
    }

    static void pushlightuserdata(MemorySegment L, MemorySegment p) {
        invoke(PUSHLIGHTUSERDATA_HANDLE, L, p);
    }

    static MemorySegment pushlstring(MemorySegment L, MemorySegment s, int len) {
        return (MemorySegment)invoke(PUSHLSTRING_HANDLE, L, s, len);
    }

    static void pushnil(MemorySegment L) {
        invoke(PUSHNIL_HANDLE, L);
    }

    static void pushnumber(MemorySegment L, double n) {
        invoke(PUSHNUMBER_HANDLE, L, n);
    }

    static MemorySegment pushstring(MemorySegment L, MemorySegment s) {
        return (MemorySegment)invoke(PUSHSTRING_HANDLE, L, s);
    }

    static int pushthread(MemorySegment L) {
        return (Integer)invoke(PUSHTHREAD_HANDLE, L);
    }

    static void pushvalue(MemorySegment L, int index) {
        invoke(PUSHVALUE_HANDLE, L, index);
    }

    static MemorySegment pushvfstring(MemorySegment L, MemorySegment fmt, MemorySegment argp) {
        return (MemorySegment)invoke(PUSHVFSTRING_HANDLE, L, fmt, argp);
    }

    static int rawequal(MemorySegment L, int index1, int index2) {
        return (Integer)invoke(RAWEQUAL_HANDLE, L, index1, index2);
    }

    static int rawget(MemorySegment L, int index) {
        return (Integer)invoke(RAWGET_HANDLE, L, index);
    }

    static int rawgeti(MemorySegment L, int index, long n) {
        return (Integer)invoke(RAWGETI_HANDLE, L, index, n);
    }

    static int rawgetp(MemorySegment L, int index, MemorySegment p) {
        return (Integer)invoke(RAWGETP_HANDLE, L, index, p);
    }

    static long rawlen(MemorySegment L, int index) {
        return (Long)invoke(RAWLEN_HANDLE, L, index);
    }

    static void rawset(MemorySegment L, int index) {
        invoke(RAWSET_HANDLE, L, index);
    }

    static void rawseti(MemorySegment L, int index, long i) {
        invoke(RAWSETI_HANDLE, L, index, i);
    }

    static void rawsetp(MemorySegment L, int index, MemorySegment p) {
        invoke(RAWSETP_HANDLE, L, index, p);
    }

    static int resetthread(MemorySegment L) {
        return (Integer)invoke(RESETTHREAD_HANDLE, L);
    }

    static int resume(MemorySegment L, MemorySegment from, int nargs, MemorySegment nresults) {
        return (Integer)invoke(RESUME_HANDLE, L, from, nargs, nresults);
    }

    static void rotate(MemorySegment L, int idx, int n) {
        invoke(ROTATE_HANDLE, L, idx, n);
    }

    static void setallocf(MemorySegment L, MemorySegment f, MemorySegment ud) {
        invoke(SETALLOCF_HANDLE, L, f, ud);
    }

    static void setfield(MemorySegment L, int index, MemorySegment k) {
        invoke(SETFIELD_HANDLE, L, index, k);
    }

    static void setglobal(MemorySegment L, MemorySegment name) {
        invoke(SETGLOBAL_HANDLE, L, name);
    }

    static void sethook(MemorySegment L, MemorySegment f, int mask, int count) {
        invoke(SETHOOK_HANDLE, L, f, mask, count);
    }

    static void seti(MemorySegment L, int index, long n) {
        invoke(SETI_HANDLE, L, index, n);
    }

    static MemorySegment setlocal(MemorySegment L, MemorySegment ar, int n) {
        return (MemorySegment)invoke(SETLOCAL_HANDLE, L, ar, n);
    }

    static int setmetatable(MemorySegment L, int index) {
        return (Integer)invoke(SETMETATABLE_HANDLE, L, index);
    }

    static void settable(MemorySegment L, int index) {
        invoke(SETTABLE_HANDLE, L, index);
    }

    static void settop(MemorySegment L, int index) {
        invoke(SETTOP_HANDLE, L, index);
    }

    static MemorySegment setupvalue(MemorySegment L, int funcindex, int n) {
        return (MemorySegment)invoke(SETUPVALUE_HANDLE, L, funcindex, n);
    }

    static void setwarnf(MemorySegment L, MemorySegment f, MemorySegment ud) {
        invoke(SETWARNF_HANDLE, L, f, ud);
    }

    static int status(MemorySegment L) {
        return (Integer)invoke(STATUS_HANDLE, L);
    }

    static int stringtonumber(MemorySegment L, MemorySegment s) {
        return (Integer)invoke(STRINGTONUMBER_HANDLE, L, s);
    }

    static int toboolean(MemorySegment L, int index) {
        return (Integer)invoke(TOBOOLEAN_HANDLE, L, index);
    }

    static MemorySegment tocfunction(MemorySegment L, int index) {
        return (MemorySegment)invoke(TOCFUNCTION_HANDLE, L, index);
    }

    static void toclose(MemorySegment L, int index) {
        invoke(TOCLOSE_HANDLE, L, index);
    }

    static long tointegerx(MemorySegment L, int index, MemorySegment isnum) {
        return (Long)invoke(TOINTEGERX_HANDLE, L, index, isnum);
    }

    static MemorySegment tolstring(MemorySegment L, int index, MemorySegment len) {
        return (MemorySegment)invoke(TOLSTRING_HANDLE, L, index, len);
    }

    static double tonumberx(MemorySegment L, int index, MemorySegment isnum) {
        return (Double)invoke(TONUMBERX_HANDLE, L, index, isnum);
    }

    static MemorySegment topointer(MemorySegment L, int index) {
        return (MemorySegment)invoke(TOPOINTER_HANDLE, L, index);
    }

    static MemorySegment tothread(MemorySegment L, int index) {
        return (MemorySegment)invoke(TOTHREAD_HANDLE, L, index);
    }

    static MemorySegment touserdata(MemorySegment L, int index) {
        return (MemorySegment)invoke(TOUSERDATA_HANDLE, L, index);
    }

    static int type(MemorySegment L, int index) {
        return (Integer)invoke(TYPE_HANDLE, L, index);
    }

    static MemorySegment typename(MemorySegment L, int tp) {
        return (MemorySegment)invoke(TYPENAME_HANDLE, L, tp);
    }

    static MemorySegment upvalueid(MemorySegment L, int funcindex, int n) {
        return (MemorySegment)invoke(UPVALUEID_HANDLE, L, funcindex, n);
    }

    static void upvaluejoin(MemorySegment L, int funcindex1, int n1, int funcindex2, int n2) {
        invoke(UPVALUEJOIN_HANDLE, L, funcindex1, n1, funcindex2, n2);
    }

    static double version(MemorySegment L) {
        return (Double)invoke(VERSION_HANDLE, L);
    }

    static void warning(MemorySegment L, MemorySegment msg, int tocont) {
        invoke(WARNING_HANDLE, L, msg, tocont);
    }

    static void xmove(MemorySegment from, MemorySegment to, int n) {
        invoke(XMOVE_HANDLE, from, to, n);
    }

    static int yieldk(MemorySegment L, int nresults, long ctx, MemorySegment k) {
        return (Integer)invoke(YIELDK_HANDLE, L, nresults, ctx, k);
    }

    static MemorySegment auxiliaryAddgsub(MemorySegment B, MemorySegment s, MemorySegment p, MemorySegment r) {
        return (MemorySegment)invoke(AUXILIARY_ADDGSUB_HANDLE, B, s, p, r);
    }

    static void auxiliaryAddlstring(MemorySegment B, MemorySegment s, int l) {
        invoke(AUXILIARY_ADDLSTRING_HANDLE, B, s, l);
    }

    static void auxiliaryAddstring(MemorySegment B, MemorySegment s) {
        invoke(AUXILIARY_ADDSTRING_HANDLE, B, s);
    }

    static void auxiliaryAddvalue(MemorySegment B) {
        invoke(AUXILIARY_ADDVALUE_HANDLE, B);
    }

    static int auxiliaryArgerror(MemorySegment L, int arg, MemorySegment extramsg) {
        return (Integer)invoke(AUXILIARY_ARGERROR_HANDLE, L, arg, extramsg);
    }

    static int auxiliaryBuffinit(MemorySegment L, MemorySegment B) {
        return (Integer)invoke(AUXILIARY_BUFFINIT_HANDLE, L, B);
    }

    static MemorySegment auxiliaryBuffinitsize(MemorySegment L, MemorySegment B, int sz) {
        return (MemorySegment)invoke(AUXILIARY_BUFFINITSIZE_HANDLE, L, B, sz);
    }

    static int auxiliaryCallmeta(MemorySegment L, int obj, MemorySegment e) {
        return (Integer)invoke(AUXILIARY_CALLMETA_HANDLE, L, obj, e);
    }

    static void auxiliaryCheckany(MemorySegment L, int arg) {
        invoke(AUXILIARY_CHECKANY_HANDLE, L, arg);
    }

    static long auxiliaryCheckinteger(MemorySegment L, int arg) {
        return (Long)invoke(AUXILIARY_CHECKINTEGER_HANDLE, L, arg);
    }

    static MemorySegment auxiliaryChecklstring(MemorySegment L, int arg, MemorySegment l) {
        return (MemorySegment)invoke(AUXILIARY_CHECKLSTRING_HANDLE, L, arg, l);
    }

    static double auxiliaryChecknumber(MemorySegment L, int arg) {
        return (Double)invoke(AUXILIARY_CHECKNUMBER_HANDLE, L, arg);
    }

    static int auxiliaryCheckoption(MemorySegment L, int arg, MemorySegment def, MemorySegment lst) {
        return (Integer)invoke(AUXILIARY_CHECKOPTION_HANDLE, L, arg, def, lst);
    }

    static int auxiliaryCheckstack(MemorySegment L, int sz, MemorySegment msg) {
        return (Integer)invoke(AUXILIARY_CHECKSTACK_HANDLE, L, sz, msg);
    }

    static void auxiliaryChecktype(MemorySegment L, int arg, int t) {
        invoke(AUXILIARY_CHECKTYPE_HANDLE, L, arg, t);
    }

    static MemorySegment auxiliaryCheckudata(MemorySegment L, int arg, MemorySegment tname) {
        return (MemorySegment)invoke(AUXILIARY_CHECKUDATA_HANDLE, L, arg, tname);
    }

    static void auxiliaryCheckversion_(MemorySegment L) {
        invoke(AUXILIARY_CHECKVERSION__HANDLE, L);
    }

    static int auxiliaryError(MemorySegment L, MemorySegment fmt) {
        return (Integer)invoke(AUXILIARY_ERROR_HANDLE, L, fmt);
    }

    static int auxiliaryExecresult(MemorySegment L, int stat) {
        return (Integer)invoke(AUXILIARY_EXECRESULT_HANDLE, L, stat);
    }

    static int auxiliaryFileresult(MemorySegment L, int stat, MemorySegment fname) {
        return (Integer)invoke(AUXILIARY_FILERESULT_HANDLE, L, stat, fname);
    }

    static int auxiliaryGetmetafield(MemorySegment L, int obj, MemorySegment e) {
        return (Integer)invoke(AUXILIARY_GETMETAFIELD_HANDLE, L, obj, e);
    }

    static int auxiliaryGetsubtable(MemorySegment L, int idx, MemorySegment fname) {
        return (Integer)invoke(AUXILIARY_GETSUBTABLE_HANDLE, L, idx, fname);
    }

    static MemorySegment auxiliaryGsub(MemorySegment L, MemorySegment s, MemorySegment p, MemorySegment r) {
        return (MemorySegment)invoke(AUXILIARY_GSUB_HANDLE, L, s, p, r);
    }

    static long auxiliaryLen(MemorySegment L, int index) {
        return (Long)invoke(AUXILIARY_LEN_HANDLE, L, index);
    }

    static int auxiliaryLoadbufferx(MemorySegment L, MemorySegment buff, int sz, MemorySegment name, MemorySegment mode) {
        return (Integer)invoke(AUXILIARY_LOADBUFFERX_HANDLE, L, buff, sz, name, mode);
    }

    static int auxiliaryLoadfilex(MemorySegment L, MemorySegment filename, MemorySegment mode) {
        return (Integer)invoke(AUXILIARY_LOADFILEX_HANDLE, L, filename, mode);
    }

    static int auxiliaryLoadstring(MemorySegment L, MemorySegment s) {
        return (Integer)invoke(AUXILIARY_LOADSTRING_HANDLE, L, s);
    }

    static int auxiliaryNewmetatable(MemorySegment L, MemorySegment tname) {
        return (Integer)invoke(AUXILIARY_NEWMETATABLE_HANDLE, L, tname);
    }

    static MemorySegment auxiliaryNewstate() {
        return (MemorySegment)invoke(AUXILIARY_NEWSTATE_HANDLE);
    }

    static void auxiliaryOpenlibs(MemorySegment L) {
        invoke(AUXILIARY_OPENLIBS_HANDLE, L);
    }

    static long auxiliaryOptinteger(MemorySegment L, int arg, long d) {
        return (Long)invoke(AUXILIARY_OPTINTEGER_HANDLE, L, arg, d);
    }

    static MemorySegment auxiliaryOptlstring(MemorySegment L, int arg, MemorySegment d, MemorySegment l) {
        return (MemorySegment)invoke(AUXILIARY_OPTLSTRING_HANDLE, L, arg, d, l);
    }

    static MemorySegment auxiliaryPrepbuffsize(MemorySegment B, int sz) {
        return (MemorySegment)invoke(AUXILIARY_PREPBUFFSIZE_HANDLE, B, sz);
    }

    static void auxiliaryPushresult(MemorySegment B) {
        invoke(AUXILIARY_PUSHRESULT_HANDLE, B);
    }

    static void auxiliaryPushresultsize(MemorySegment B, int sz) {
        invoke(AUXILIARY_PUSHRESULTSIZE_HANDLE, B, sz);
    }

    static int auxiliaryRef(MemorySegment L, int t) {
        return (Integer)invoke(AUXILIARY_REF_HANDLE, L, t);
    }

    static void auxiliaryRequiref(MemorySegment L, MemorySegment modname, MemorySegment openf, int glb) {
        invoke(AUXILIARY_REQUIREF_HANDLE, L, modname, openf, glb);
    }

    static void auxiliarySetfuncs(MemorySegment L, MemorySegment l, int nup) {
        invoke(AUXILIARY_SETFUNCS_HANDLE, L, l, nup);
    }

    static void auxiliarySetmetatable(MemorySegment L, MemorySegment tname) {
        invoke(AUXILIARY_SETMETATABLE_HANDLE, L, tname);
    }

    static MemorySegment auxiliaryTestudata(MemorySegment L, int arg, MemorySegment tname) {
        return (MemorySegment)invoke(AUXILIARY_TESTUDATA_HANDLE, L, arg, tname);
    }

    static MemorySegment auxiliaryTolstring(MemorySegment L, int idx, MemorySegment len) {
        return (MemorySegment)invoke(AUXILIARY_TOLSTRING_HANDLE, L, idx, len);
    }

    static void auxiliaryTraceback(MemorySegment L, MemorySegment L1, MemorySegment msg, int level) {
        invoke(AUXILIARY_TRACEBACK_HANDLE, L, L1, msg, level);
    }

    static int auxiliaryTyperror(MemorySegment L, int arg, MemorySegment tname) {
        return (Integer)invoke(AUXILIARY_TYPEERROR_HANDLE, L, arg, tname);
    }

    static void auxiliaryUnref(MemorySegment L, int t, int ref) {
        invoke(AUXILIARY_UNREF_HANDLE, L, t, ref);
    }

    static void auxiliaryWhere(MemorySegment L, int lvl) {
        invoke(AUXILIARY_WHERE_HANDLE, L, lvl);
    }

    static int openBase(MemorySegment L) {
        return (Integer)invoke(OPEN_BASE_HANDLE, L);
    }

    static int openCoroutine(MemorySegment L) {
        return (Integer)invoke(OPEN_COROUTINE_HANDLE, L);
    }

    static int openDebug(MemorySegment L) {
        return (Integer)invoke(OPEN_DEBUG_HANDLE, L);
    }

    static int openIo(MemorySegment L) {
        return (Integer)invoke(OPEN_IO_HANDLE, L);
    }

    static int openMath(MemorySegment L) {
        return (Integer)invoke(OPEN_MATH_HANDLE, L);
    }

    static int openOs(MemorySegment L) {
        return (Integer)invoke(OPEN_OS_HANDLE, L);
    }

    static int openPackage(MemorySegment L) {
        return (Integer)invoke(OPEN_PACKAGE_HANDLE, L);
    }

    static int openString(MemorySegment L) {
        return (Integer)invoke(OPEN_STRING_HANDLE, L);
    }

    static int openTable(MemorySegment L) {
        return (Integer)invoke(OPEN_TABLE_HANDLE, L);
    }

    static int openUtf8(MemorySegment L) {
        return (Integer)invoke(OPEN_UTF8_HANDLE, L);
    }

    static MemorySegment stubAlloc(Arena arena, Alloc alloc) {
        return Linker.nativeLinker().upcallStub(ALLOC_HANDLE.bindTo(alloc), ALLOC_DESCRIPTOR, arena);
    }

    static MemorySegment stubCFunction(Arena arena, CFunction cFunction) {
        return Linker.nativeLinker().upcallStub(C_FUNCTION_HANDLE.bindTo(cFunction), C_FUNCTION_DESCRIPTOR, arena);
    }

    static MemorySegment stubHook(Arena arena, Hook hook) {
        return Linker.nativeLinker().upcallStub(HOOK_HANDLE.bindTo(hook), HOOK_DESCRIPTOR, arena);
    }

    static MemorySegment stubKFunction(Arena arena, KFunction kFunction) {
        return Linker.nativeLinker().upcallStub(K_FUNCTION_HANDLE.bindTo(kFunction), K_FUNCTION_DESCRIPTOR, arena);
    }

    static MemorySegment stubReader(Arena arena, Reader reader) {
        return Linker.nativeLinker().upcallStub(READER_HANDLE.bindTo(reader), READER_DESCRIPTOR, arena);
    }

    static MemorySegment stubWarnFunction(Arena arena, WarnFunction warnFunction) {
        return Linker.nativeLinker().upcallStub(WARN_FUNCTION_HANDLE.bindTo(warnFunction), WARN_FUNCTION_DESCRIPTOR, arena);
    }

    static MemorySegment stubWriter(Arena arena, Writer writer) {
        return Linker.nativeLinker().upcallStub(WRITER_HANDLE.bindTo(writer), WRITER_DESCRIPTOR, arena);
    }

    @FunctionalInterface
    interface Alloc {
        MemorySegment apply(MemorySegment ud, MemorySegment ptr, long osize, long nsize);
    }

    @FunctionalInterface
    interface CFunction {
        int apply(MemorySegment L);
    }

    @FunctionalInterface
    interface Hook {
        void apply(MemorySegment L, MemorySegment ar);
    }

    @FunctionalInterface
    interface KFunction {
        int apply(MemorySegment L, int status, long ctx);
    }

    @FunctionalInterface
    interface Reader {
        MemorySegment apply(MemorySegment L, MemorySegment ud, MemorySegment sz);
    }

    @FunctionalInterface
    interface WarnFunction {
        void apply(MemorySegment ud, MemorySegment msg, int tocont);
    }

    @FunctionalInterface
    interface Writer {
        int apply(MemorySegment L, MemorySegment p, long sz, MemorySegment ud);
    }
}

package me.white.wlua;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

class LuaBindings {
    static final MemoryLayout INTEGER_LAYOUT = ValueLayout.JAVA_INT;
    static final MemoryLayout NUMBER_LAYOUT = ValueLayout.JAVA_DOUBLE;
    static final MemoryLayout UNSIGNED_LAYOUT = ValueLayout.JAVA_LONG;
    static final MemoryLayout CONTEXT_LAYOUT = ValueLayout.JAVA_LONG;
    static final MemoryLayout SIZE_LAYOUT = ValueLayout.JAVA_INT;
    private static final MethodHandle ABSINDEX_HANDLE = downcallHandle("lua_absindex", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
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
    private static final MethodHandle SETIUSERVALUE_HANDLE = downcallHandle("lua_setiuservalue", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
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
    private static final MethodHandle AUXILIARY_TYPEERROR_HANDLE = downcallHandle("luaL_typeerror", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
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
    private static final FunctionDescriptor ALLOC_DESCRIPTOR = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG);
    private static final FunctionDescriptor C_FUNCTION_DESCRIPTOR = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS);
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
    private static boolean isLoaded = false;
    static final String VERSION_MAJOR = "5";
    static final String VERSION_MINOR = "4";
    static final String VERSION_RELEASE = "7";
    static final int VERSION_NUM = 504;
    static final int VERSION_RELEASE_NUM = 50407;
    static final String VERSION = "Lua 5.4";
    static final String RELEASE = "Lua 5.4.7";
    static final String COPYRIGHT = "Lua 5.4.7  Copyright (C) 1994-2024 Lua.org, PUC-Rio";
    static final String AUTHORS = "R. Ierusalimschy, L. H. de Figueiredo, W. Celes";
    static final String SIGNATURE = "\u001bLua";
    static final int MULTRET = -1;
    static final int REGISTRYINDEX = -1001000;
    static final int OK = 0;
    static final int YIELD = 1;
    static final int ERRRUN = 2;
    static final int ERRSYNTAX = 3;
    static final int ERRMEM = 4;
    static final int ERRERR = 5;
    static final int TNONE = -1;
    static final int TNIL = 0;
    static final int TBOOLEAN = 1;
    static final int TLIGHTUSERDATA = 2;
    static final int TNUMBER = 3;
    static final int TSTRING = 4;
    static final int TTABLE = 5;
    static final int TFUNCTION = 6;
    static final int TUSERDATA = 7;
    static final int TTHREAD = 8;
    static final int NUMTYPES = 9;
    static final int MINSTACK = 20;
    static final int RIDX_MAINTHREAD = 1;
    static final int RIDX_GLOBALS = 2;
    static final int RIDX_LAST = 2;
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
    static final int GCSTOP = 0;
    static final int GCRESTART = 1;
    static final int GCCOLLECT = 2;
    static final int GCCOUNT = 3;
    static final int GCCOUNTB = 4;
    static final int GCSTEP = 5;
    static final int GCSETPAUSE = 6;
    static final int GCSETSTEPMUL = 7;
    static final int GCISRUNNING = 9;
    static final int GCGEN = 10;
    static final int GCINC = 11;
    static final int HOOKCALL = 0;
    static final int HOOKRET = 1;
    static final int HOOKLINE = 2;
    static final int HOOKCOUNT = 3;
    static final int HOOKTAILCALL = 4;
    static final int MASKCALL = 1 << HOOKCALL;
    static final int MASKRET = 1 << HOOKRET;
    static final int MASKLINE = 1 << HOOKLINE;
    static final int MASKCOUNT = 1 << HOOKCOUNT;

    private static MethodHandle downcallHandle(String name, FunctionDescriptor descriptor) {
        if (!isLoaded) {
            System.loadLibrary("lua54");
            isLoaded = true;
        }
        MemorySegment address = SymbolLookup.loaderLookup().find(name).orElseThrow(() -> new UnsatisfiedLinkError("Lua is not loaded."));
        return Linker.nativeLinker().downcallHandle(address, descriptor);
    }

    private static MethodHandle upcallHandle(Class<?> clazz, String name, FunctionDescriptor descriptor) {
        try {
            return MethodHandles.lookup().findVirtual(clazz, name, descriptor.toMethodType());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    static int absindex(MemorySegment L, int idx) {
        try {
            return (int)ABSINDEX_HANDLE.invokeExact(L, idx);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void arith(MemorySegment L, int op) {
        try {
            ARITH_HANDLE.invokeExact(L, op);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static MemorySegment atpanic(MemorySegment L, MemorySegment panicf) {
        try {
            return (MemorySegment)ATPANIC_HANDLE.invokeExact(L, panicf);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void callk(MemorySegment L, int nargs, int nresults, int ctx, MemorySegment k) {
        try {
            CALLK_HANDLE.invokeExact(L, nargs, nresults, ctx, k);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int checkstack(MemorySegment L, int n) {
        try {
            return (int)CHECKSTACK_HANDLE.invokeExact(L, n);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void close(MemorySegment L) {
        try {
            CLOSE_HANDLE.invokeExact(L);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void closeslot(MemorySegment L, int index) {
        try {
            CLOSESLOT_HANDLE.invokeExact(L, index);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int closethread(MemorySegment L, MemorySegment from) {
        try {
            return (int)CLOSETHREAD_HANDLE.invokeExact(L, from);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int compare(MemorySegment L, int index1, int index2, int op) {
        try {
            return (int)COMPARE_HANDLE.invokeExact(L, index1, index2, op);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void concat(MemorySegment L, int n) {
        try {
            CONCAT_HANDLE.invokeExact(L, n);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void copy(MemorySegment L, int fromidx, int toidx) {
        try {
            COPY_HANDLE.invokeExact(L, fromidx, toidx);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void createtable(MemorySegment L, int narr, int nrec) {
        try {
            CREATETABLE_HANDLE.invokeExact(L, narr, nrec);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int dump(MemorySegment L, MemorySegment writer, MemorySegment data, int strip) {
        try {
            return (int)DUMP_HANDLE.invokeExact(L, writer, data, strip);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int error(MemorySegment L) {
        try {
            return (int)ERROR_HANDLE.invokeExact(L);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int gc(MemorySegment L, int what) {
        try {
            return (int)GC_HANDLE.invokeExact(L, what);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int gcStep(MemorySegment L, int what, int stepsize) {
        try {
            return (int)GC_STEP_HANDLE.invokeExact(L, what, stepsize);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int gcInc(MemorySegment L, int what, int pause, int stepmul, int stepsize) {
        try {
            return (int)GC_INC_HANDLE.invokeExact(L, what, pause, stepmul, stepsize);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int gcGen(MemorySegment L, int what, int minormul, int majormul) {
        try {
            return (int)GC_GEN_HANDLE.invokeExact(L, what, minormul, majormul);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static MemorySegment getallocf(MemorySegment L, MemorySegment ud) {
        try {
            return (MemorySegment)GETALLOCF_HANDLE.invokeExact(L, ud);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int getfield(MemorySegment L, int index, MemorySegment k) {
        try {
            return (int)GETFIELD_HANDLE.invokeExact(L, index, k);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int getglobal(MemorySegment L, MemorySegment name) {
        try {
            return (int)GETGLOBAL_HANDLE.invokeExact(L, name);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static MemorySegment gethook(MemorySegment L) {
        try {
            return (MemorySegment)GETHOOK_HANDLE.invokeExact(L);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int gethookcount(MemorySegment L) {
        try {
            return (int)GETHOOKCOUNT_HANDLE.invokeExact(L);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int gethookmask(MemorySegment L) {
        try {
            return (int)GETHOOKMASK_HANDLE.invokeExact(L);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int geti(MemorySegment L, int index, int i) {
        try {
            return (int)GETI_HANDLE.invokeExact(L, index, i);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int getinfo(MemorySegment L, MemorySegment what, MemorySegment ar) {
        try {
            return (int)GETINFO_HANDLE.invokeExact(L, what, ar);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int getiuservalue(MemorySegment L, int index, int n) {
        try {
            return (int)GETIUSERVALUE_HANDLE.invokeExact(L, index, n);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static MemorySegment getlocal(MemorySegment L, MemorySegment ar, int n) {
        try {
            return (MemorySegment)GETLOCAL_HANDLE.invokeExact(L, ar, n);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int getmetatable(MemorySegment L, int index) {
        try {
            return (int)GETMETATABLE_HANDLE.invokeExact(L, index);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int getstack(MemorySegment L, int level, MemorySegment ar) {
        try {
            return (int)GETSTACK_HANDLE.invokeExact(L, level, ar);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int gettable(MemorySegment L, int index) {
        try {
            return (int)GETTABLE_HANDLE.invokeExact(L, index);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int gettop(MemorySegment L) {
        try {
            return (int)GETTOP_HANDLE.invokeExact(L);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static MemorySegment getupvalue(MemorySegment L, int funcindex, int n) {
        try {
            return (MemorySegment)GETUPVALUE_HANDLE.invokeExact(L, funcindex, n);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int iscfunction(MemorySegment L, int index) {
        try {
            return (int)ISCFUNCTION_HANDLE.invokeExact(L, index);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int isinteger(MemorySegment L, int index) {
        try {
            return (int)ISINTEGER_HANDLE.invokeExact(L, index);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int isnumber(MemorySegment L, int index) {
        try {
            return (int)ISNUMBER_HANDLE.invokeExact(L, index);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int isstring(MemorySegment L, int index) {
        try {
            return (int)ISSTRING_HANDLE.invokeExact(L, index);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int isuserdata(MemorySegment L, int index) {
        try {
            return (int)ISUSERDATA_HANDLE.invokeExact(L, index);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int isyieldable(MemorySegment L) {
        try {
            return (int)ISYIELDABLE_HANDLE.invokeExact(L);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void len(MemorySegment L, int index) {
        try {
            LEN_HANDLE.invokeExact(L, index);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int load(MemorySegment L, MemorySegment reader, MemorySegment data, MemorySegment chunkname, MemorySegment mode) {
        try {
            return (int)LOAD_HANDLE.invokeExact(L, reader, data, chunkname, mode);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static MemorySegment newstate(MemorySegment f, MemorySegment ud) {
        try {
            return (MemorySegment)NEWSTATE_HANDLE.invokeExact(f, ud);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static MemorySegment newthread(MemorySegment L) {
        try {
            return (MemorySegment)NEWTHREAD_HANDLE.invokeExact(L);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static MemorySegment newuserdatauv(MemorySegment L, int size, int nuvalue) {
        try {
            return (MemorySegment)NEWUSERDATAUV_HANDLE.invokeExact(L, size, nuvalue);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int next(MemorySegment L, int index) {
        try {
            return (int)NEXT_HANDLE.invokeExact(L, index);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int pcallk(MemorySegment L, int nargs, int nresults, int msgh, long ctx, MemorySegment k) {
        try {
            return (int)PCALLK_HANDLE.invokeExact(L, nargs, nresults, msgh, ctx, k);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void pushboolean(MemorySegment L, int b) {
        try {
            PUSHBOOLEAN_HANDLE.invokeExact(L, b);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void pushcclosure(MemorySegment L, MemorySegment fn, int n) {
        try {
            PUSHCCLOSURE_HANDLE.invokeExact(L, fn, n);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static MemorySegment pushfstring(MemorySegment L, MemorySegment fmt) {
        try {
            return (MemorySegment)PUSHFSTRING_HANDLE.invokeExact(L, fmt);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void pushinteger(MemorySegment L, int n) {
        try {
            PUSHINTEGER_HANDLE.invokeExact(L, n);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void pushlightuserdata(MemorySegment L, MemorySegment p) {
        try {
            PUSHLIGHTUSERDATA_HANDLE.invokeExact(L, p);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static MemorySegment pushlstring(MemorySegment L, MemorySegment s, int len) {
        try {
            return (MemorySegment)PUSHLSTRING_HANDLE.invokeExact(L, s, len);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void pushnil(MemorySegment L) {
        try {
            PUSHNIL_HANDLE.invokeExact(L);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void pushnumber(MemorySegment L, double n) {
        try {
            PUSHNUMBER_HANDLE.invokeExact(L, n);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static MemorySegment pushstring(MemorySegment L, MemorySegment s) {
        try {
            return (MemorySegment)PUSHSTRING_HANDLE.invokeExact(L, s);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int pushthread(MemorySegment L) {
        try {
            return (int)PUSHTHREAD_HANDLE.invokeExact(L);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void pushvalue(MemorySegment L, int index) {
        try {
            PUSHVALUE_HANDLE.invokeExact(L, index);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static MemorySegment pushvfstring(MemorySegment L, MemorySegment fmt, MemorySegment argp) {
        try {
            return (MemorySegment)PUSHVFSTRING_HANDLE.invokeExact(L, fmt, argp);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int rawequal(MemorySegment L, int index1, int index2) {
        try {
            return (int)RAWEQUAL_HANDLE.invokeExact(L, index1, index2);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int rawget(MemorySegment L, int index) {
        try {
            return (int)RAWGET_HANDLE.invokeExact(L, index);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int rawgeti(MemorySegment L, int index, int n) {
        try {
            return (int)RAWGETI_HANDLE.invokeExact(L, index, n);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int rawgetp(MemorySegment L, int index, MemorySegment p) {
        try {
            return (int)RAWGETP_HANDLE.invokeExact(L, index, p);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static long rawlen(MemorySegment L, int index) {
        try {
            return (long)RAWLEN_HANDLE.invokeExact(L, index);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void rawset(MemorySegment L, int index) {
        try {
            RAWSET_HANDLE.invokeExact(L, index);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void rawseti(MemorySegment L, int index, int i) {
        try {
            RAWSETI_HANDLE.invokeExact(L, index, i);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void rawsetp(MemorySegment L, int index, MemorySegment p) {
        try {
            RAWSETP_HANDLE.invokeExact(L, index, p);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int resetthread(MemorySegment L) {
        try {
            return (int)RESETTHREAD_HANDLE.invokeExact(L);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int resume(MemorySegment L, MemorySegment from, int nargs, MemorySegment nresults) {
        try {
            return (int)RESUME_HANDLE.invokeExact(L, from, nargs, nresults);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void rotate(MemorySegment L, int idx, int n) {
        try {
            ROTATE_HANDLE.invokeExact(L, idx, n);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void setallocf(MemorySegment L, MemorySegment f, MemorySegment ud) {
        try {
            SETALLOCF_HANDLE.invokeExact(L, f, ud);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void setfield(MemorySegment L, int index, MemorySegment k) {
        try {
            SETFIELD_HANDLE.invokeExact(L, index, k);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void setglobal(MemorySegment L, MemorySegment name) {
        try {
            SETGLOBAL_HANDLE.invokeExact(L, name);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void sethook(MemorySegment L, MemorySegment f, int mask, int count) {
        try {
            SETHOOK_HANDLE.invokeExact(L, f, mask, count);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void seti(MemorySegment L, int index, int n) {
        try {
            SETI_HANDLE.invokeExact(L, index, n);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int setiuservalue(MemorySegment L, int index, int n) {
        try {
            return (int)SETIUSERVALUE_HANDLE.invokeExact(L, index, n);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static MemorySegment setlocal(MemorySegment L, MemorySegment ar, int n) {
        try {
            return (MemorySegment)SETLOCAL_HANDLE.invokeExact(L, ar, n);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int setmetatable(MemorySegment L, int index) {
        try {
            return (int)SETMETATABLE_HANDLE.invokeExact(L, index);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void settable(MemorySegment L, int index) {
        try {
            SETTABLE_HANDLE.invokeExact(L, index);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void settop(MemorySegment L, int index) {
        try {
            SETTOP_HANDLE.invokeExact(L, index);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static MemorySegment setupvalue(MemorySegment L, int funcindex, int n) {
        try {
            return (MemorySegment)SETUPVALUE_HANDLE.invokeExact(L, funcindex, n);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void setwarnf(MemorySegment L, MemorySegment f, MemorySegment ud) {
        try {
            SETWARNF_HANDLE.invokeExact(L, f, ud);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int status(MemorySegment L) {
        try {
            return (int)STATUS_HANDLE.invokeExact(L);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int stringtonumber(MemorySegment L, MemorySegment s) {
        try {
            return (int)STRINGTONUMBER_HANDLE.invokeExact(L, s);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int toboolean(MemorySegment L, int index) {
        try {
            return (int)TOBOOLEAN_HANDLE.invokeExact(L, index);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static MemorySegment tocfunction(MemorySegment L, int index) {
        try {
            return (MemorySegment)TOCFUNCTION_HANDLE.invokeExact(L, index);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void toclose(MemorySegment L, int index) {
        try {
            TOCLOSE_HANDLE.invokeExact(L, index);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int tointegerx(MemorySegment L, int index, MemorySegment isnum) {
        try {
            return (int)TOINTEGERX_HANDLE.invokeExact(L, index, isnum);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static MemorySegment tolstring(MemorySegment L, int index, MemorySegment len) {
        try {
            return (MemorySegment)TOLSTRING_HANDLE.invokeExact(L, index, len);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static double tonumberx(MemorySegment L, int index, MemorySegment isnum) {
        try {
            return (double)TONUMBERX_HANDLE.invokeExact(L, index, isnum);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static MemorySegment topointer(MemorySegment L, int index) {
        try {
            return (MemorySegment)TOPOINTER_HANDLE.invokeExact(L, index);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static MemorySegment tothread(MemorySegment L, int index) {
        try {
            return (MemorySegment)TOTHREAD_HANDLE.invokeExact(L, index);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static MemorySegment touserdata(MemorySegment L, int index) {
        try {
            return (MemorySegment)TOUSERDATA_HANDLE.invokeExact(L, index);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int type(MemorySegment L, int index) {
        try {
            return (int)TYPE_HANDLE.invokeExact(L, index);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static MemorySegment typename(MemorySegment L, int tp) {
        try {
            return (MemorySegment)TYPENAME_HANDLE.invokeExact(L, tp);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static MemorySegment upvalueid(MemorySegment L, int funcindex, int n) {
        try {
            return (MemorySegment)UPVALUEID_HANDLE.invokeExact(L, funcindex, n);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void upvaluejoin(MemorySegment L, int funcindex1, int n1, int funcindex2, int n2) {
        try {
            UPVALUEJOIN_HANDLE.invokeExact(L, funcindex1, n1, funcindex2, n2);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static double version(MemorySegment L) {
        try {
            return (double)VERSION_HANDLE.invokeExact(L);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void warning(MemorySegment L, MemorySegment msg, int tocont) {
        try {
            WARNING_HANDLE.invokeExact(L, msg, tocont);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void xmove(MemorySegment from, MemorySegment to, int n) {
        try {
            XMOVE_HANDLE.invokeExact(from, to, n);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int yieldk(MemorySegment L, int nresults, long ctx, MemorySegment k) {
        try {
            return (int)YIELDK_HANDLE.invokeExact(L, nresults, ctx, k);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static MemorySegment auxiliaryAddgsub(MemorySegment B, MemorySegment s, MemorySegment p, MemorySegment r) {
        try {
            return (MemorySegment)AUXILIARY_ADDGSUB_HANDLE.invokeExact(B, s, p, r);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void auxiliaryAddlstring(MemorySegment B, MemorySegment s, int l) {
        try {
            AUXILIARY_ADDLSTRING_HANDLE.invokeExact(B, s, l);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void auxiliaryAddstring(MemorySegment B, MemorySegment s) {
        try {
            AUXILIARY_ADDSTRING_HANDLE.invokeExact(B, s);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void auxiliaryAddvalue(MemorySegment B) {
        try {
            AUXILIARY_ADDVALUE_HANDLE.invokeExact(B);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int auxiliaryArgerror(MemorySegment L, int arg, MemorySegment extramsg) {
        try {
            return (int)AUXILIARY_ARGERROR_HANDLE.invokeExact(L, arg, extramsg);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int auxiliaryBuffinit(MemorySegment L, MemorySegment B) {
        try {
            return (int)AUXILIARY_BUFFINIT_HANDLE.invokeExact(L, B);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static MemorySegment auxiliaryBuffinitsize(MemorySegment L, MemorySegment B, int sz) {
        try {
            return (MemorySegment)AUXILIARY_BUFFINITSIZE_HANDLE.invokeExact(L, B, sz);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int auxiliaryCallmeta(MemorySegment L, int obj, MemorySegment e) {
        try {
            return (int)AUXILIARY_CALLMETA_HANDLE.invokeExact(L, obj, e);
        } catch (Throwable ex) {
            throw new AssertionError(ex);
        }
    }

    static void auxiliaryCheckany(MemorySegment L, int arg) {
        try {
            AUXILIARY_CHECKANY_HANDLE.invokeExact(L, arg);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int auxiliaryCheckinteger(MemorySegment L, int arg) {
        try {
            return (int)AUXILIARY_CHECKINTEGER_HANDLE.invokeExact(L, arg);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static MemorySegment auxiliaryChecklstring(MemorySegment L, int arg, MemorySegment l) {
        try {
            return (MemorySegment)AUXILIARY_CHECKLSTRING_HANDLE.invokeExact(L, arg, l);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static double auxiliaryChecknumber(MemorySegment L, int arg) {
        try {
            return (double)AUXILIARY_CHECKNUMBER_HANDLE.invokeExact(L, arg);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int auxiliaryCheckoption(MemorySegment L, int arg, MemorySegment def, MemorySegment lst) {
        try {
            return (int)AUXILIARY_CHECKOPTION_HANDLE.invokeExact(L, arg, def, lst);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int auxiliaryCheckstack(MemorySegment L, int sz, MemorySegment msg) {
        try {
            return (int)AUXILIARY_CHECKSTACK_HANDLE.invokeExact(L, sz, msg);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void auxiliaryChecktype(MemorySegment L, int arg, int t) {
        try {
            AUXILIARY_CHECKTYPE_HANDLE.invokeExact(L, arg, t);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static MemorySegment auxiliaryCheckudata(MemorySegment L, int arg, MemorySegment tname) {
        try {
            return (MemorySegment)AUXILIARY_CHECKUDATA_HANDLE.invokeExact(L, arg, tname);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void auxiliaryCheckversion_(MemorySegment L) {
        try {
            AUXILIARY_CHECKVERSION__HANDLE.invokeExact(L);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int auxiliaryError(MemorySegment L, MemorySegment fmt) {
        try {
            return (int)AUXILIARY_ERROR_HANDLE.invokeExact(L, fmt);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int auxiliaryExecresult(MemorySegment L, int stat) {
        try {
            return (int)AUXILIARY_EXECRESULT_HANDLE.invokeExact(L, stat);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int auxiliaryFileresult(MemorySegment L, int stat, MemorySegment fname) {
        try {
            return (int)AUXILIARY_FILERESULT_HANDLE.invokeExact(L, stat, fname);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int auxiliaryGetmetafield(MemorySegment L, int obj, MemorySegment e) {
        try {
            return (int)AUXILIARY_GETMETAFIELD_HANDLE.invokeExact(L, obj, e);
        } catch (Throwable ex) {
            throw new AssertionError(e);
        }
    }

    static int auxiliaryGetsubtable(MemorySegment L, int idx, MemorySegment fname) {
        try {
            return (int)AUXILIARY_GETSUBTABLE_HANDLE.invokeExact(L, idx, fname);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static MemorySegment auxiliaryGsub(MemorySegment L, MemorySegment s, MemorySegment p, MemorySegment r) {
        try {
            return (MemorySegment)AUXILIARY_GSUB_HANDLE.invokeExact(L, s, p, r);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int auxiliaryLen(MemorySegment L, int index) {
        try {
            return (int)AUXILIARY_LEN_HANDLE.invokeExact(L, index);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int auxiliaryLoadbufferx(MemorySegment L, MemorySegment buff, int sz, MemorySegment name, MemorySegment mode) {
        try {
            return (int)AUXILIARY_LOADBUFFERX_HANDLE.invokeExact(L, buff, sz, name, mode);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int auxiliaryLoadfilex(MemorySegment L, MemorySegment filename, MemorySegment mode) {
        try {
            return (int)AUXILIARY_LOADFILEX_HANDLE.invokeExact(L, filename, mode);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int auxiliaryLoadstring(MemorySegment L, MemorySegment s) {
        try {
            return (int)AUXILIARY_LOADSTRING_HANDLE.invokeExact(L, s);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int auxiliaryNewmetatable(MemorySegment L, MemorySegment tname) {
        try {
            return (int)AUXILIARY_NEWMETATABLE_HANDLE.invokeExact(L, tname);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static MemorySegment auxiliaryNewstate() {
        try {
            return (MemorySegment)AUXILIARY_NEWSTATE_HANDLE.invokeExact();
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void auxiliaryOpenlibs(MemorySegment L) {
        try {
            AUXILIARY_OPENLIBS_HANDLE.invokeExact(L);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int auxiliaryOptinteger(MemorySegment L, int arg, long d) {
        try {
            return (int)AUXILIARY_OPTINTEGER_HANDLE.invokeExact(L, arg, d);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static MemorySegment auxiliaryOptlstring(MemorySegment L, int arg, MemorySegment d, MemorySegment l) {
        try {
            return (MemorySegment)AUXILIARY_OPTLSTRING_HANDLE.invokeExact(L, arg, d, l);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static MemorySegment auxiliaryPrepbuffsize(MemorySegment B, int sz) {
        try {
            return (MemorySegment)AUXILIARY_PREPBUFFSIZE_HANDLE.invokeExact(B, sz);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void auxiliaryPushresult(MemorySegment B) {
        try {
            AUXILIARY_PUSHRESULT_HANDLE.invokeExact(B);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void auxiliaryPushresultsize(MemorySegment B, int sz) {
        try {
            AUXILIARY_PUSHRESULTSIZE_HANDLE.invokeExact(B, sz);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int auxiliaryRef(MemorySegment L, int t) {
        try {
            return (int)AUXILIARY_REF_HANDLE.invokeExact(L, t);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void auxiliaryRequiref(MemorySegment L, MemorySegment modname, MemorySegment openf, int glb) {
        try {
            AUXILIARY_REQUIREF_HANDLE.invokeExact(L, modname, openf, glb);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void auxiliarySetfuncs(MemorySegment L, MemorySegment l, int nup) {
        try {
            AUXILIARY_SETFUNCS_HANDLE.invokeExact(L, l, nup);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void auxiliarySetmetatable(MemorySegment L, MemorySegment tname) {
        try {
            AUXILIARY_SETMETATABLE_HANDLE.invokeExact(L, tname);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static MemorySegment auxiliaryTestudata(MemorySegment L, int arg, MemorySegment tname) {
        try {
            return (MemorySegment)AUXILIARY_TESTUDATA_HANDLE.invokeExact(L, arg, tname);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static MemorySegment auxiliaryTolstring(MemorySegment L, int idx, MemorySegment len) {
        try {
            return (MemorySegment)AUXILIARY_TOLSTRING_HANDLE.invokeExact(L, idx, len);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void auxiliaryTraceback(MemorySegment L, MemorySegment L1, MemorySegment msg, int level) {
        try {
            AUXILIARY_TRACEBACK_HANDLE.invokeExact(L, L1, msg, level);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int auxiliaryTypeerror(MemorySegment L, int arg, MemorySegment tname) {
        try {
            return (int)AUXILIARY_TYPEERROR_HANDLE.invokeExact(L, arg, tname);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void auxiliaryUnref(MemorySegment L, int t, int ref) {
        try {
            AUXILIARY_UNREF_HANDLE.invokeExact(L, t, ref);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static void auxiliaryWhere(MemorySegment L, int lvl) {
        try {
            AUXILIARY_WHERE_HANDLE.invokeExact(L, lvl);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int openBase(MemorySegment L) {
        try {
            return (int)OPEN_BASE_HANDLE.invokeExact(L);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int openCoroutine(MemorySegment L) {
        try {
            return (int)OPEN_COROUTINE_HANDLE.invokeExact(L);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int openDebug(MemorySegment L) {
        try {
            return (int)OPEN_DEBUG_HANDLE.invokeExact(L);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int openIo(MemorySegment L) {
        try {
            return (int)OPEN_IO_HANDLE.invokeExact(L);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int openMath(MemorySegment L) {
        try {
            return (int)OPEN_MATH_HANDLE.invokeExact(L);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int openOs(MemorySegment L) {
        try {
            return (int)OPEN_OS_HANDLE.invokeExact(L);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int openPackage(MemorySegment L) {
        try {
            return (int)OPEN_PACKAGE_HANDLE.invokeExact(L);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int openString(MemorySegment L) {
        try {
            return (int)OPEN_STRING_HANDLE.invokeExact(L);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int openTable(MemorySegment L) {
        try {
            return (int)OPEN_TABLE_HANDLE.invokeExact(L);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    static int openUtf8(MemorySegment L) {
        try {
            return (int)OPEN_UTF8_HANDLE.invokeExact(L);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
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

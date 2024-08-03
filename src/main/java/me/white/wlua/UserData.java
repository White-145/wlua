package me.white.wlua;

import me.white.wlua.annotation.LuaField;
import me.white.wlua.annotation.LuaMetaMethod;
import me.white.wlua.annotation.LuaFunction;

import java.lang.reflect.*;
import java.util.*;

public abstract class UserData extends LuaValue {
    private static void addName(Set<String> definedNames, String name) {
        if (definedNames.contains(name)) {
            throw new IllegalStateException("Name '" + name + "' is already defined.");
        }
        definedNames.add(name);
    }

    @Override
    void push(LuaState state) {
        LuaNatives.newUserData(state.ptr, this);
        String metaTableName = "metatable_" + getClass().getName();
        boolean exists = LuaNatives.luaL_newmetatable(state.ptr, metaTableName) == 0;
        if (!exists) {
            Map<String, Field> fields = new HashMap<>();
            Map<String, Method> functions = new HashMap<>();
            EnumMap<MetaMethodType, Method> metaMethods = new EnumMap<>(MetaMethodType.class);
            Set<String> definedNames = new HashSet<>();

            // gather
            for (Field field : getClass().getFields()) {
                if (field.isAnnotationPresent(LuaField.class)) {
                    if (!field.canAccess(this)) {
                        throw new IllegalStateException("Field '" + field.getName() + "' is not accessible.");
                    }
                    LuaField annotation = field.getAnnotation(LuaField.class);
                    addName(definedNames, annotation.value());
                    validateField(field);
                    fields.put(annotation.value(), field);
                }
            }
            for (Method method : getClass().getMethods()) {
                if (method.isAnnotationPresent(LuaFunction.class)) {
                    if (!method.canAccess(this)) {
                        throw new IllegalStateException("Method '" + method.getName() + "' is not accessible.");
                    }
                    LuaFunction annotation = method.getAnnotation(LuaFunction.class);
                    addName(definedNames, annotation.value());
                    validateFunction(method);
                    functions.put(annotation.value(), method);
                }
                if (method.isAnnotationPresent(LuaMetaMethod.class)) {
                    if (!method.canAccess(this)) {
                        throw new IllegalStateException("Method '" + method.getName() + "' is not accessible.");
                    }
                    LuaMetaMethod annotation = method.getAnnotation(LuaMetaMethod.class);
                    validateMetaMethod(method, annotation.value());
                    metaMethods.put(annotation.value(), method);
                    // throw an error if meta method type is already defined
                }
            }

            for (Map.Entry<MetaMethodType, Method> entry : metaMethods.entrySet()) {
                Method method = entry.getValue();
                MetaMethodType type = entry.getKey();
                LuaNatives.setMetaMethod(state.ptr, this, type.name, method, -1);
            }

            UserDataIndexer indexer = new UserDataIndexer(this, fields, functions);

            // set meta methods
            // __gc to removing java reference
            // __index and __newindex for getting and setting java fields and methods
            // perhaps new (gc'd together the userdata) class that'll define `get(UserData, LuaValue)` and `set(UserData, LuaValue, LuaValue)`
            // ignore non-string values and find-retrieve-set corresponding values in some <String, Field/Method> map
            // for set ignore method values
            // perhaps, allow user to define getter and setter functions?
            // set name
            // TODO: allow specifying the __name field
            LuaNatives.lua_pushstring(state.ptr, "__name");
            LuaNatives.lua_pushstring(state.ptr, getClass().getSimpleName());
            LuaNatives.lua_settable(state.ptr, -3);
        }
        // push user data
        // set meta table
        LuaNatives.lua_setmetatable(state.ptr, -2);
    }

    private static void validateField(Field field) {
        if (!LuaValue.class.isAssignableFrom(field.getType())) {
            throw new IllegalStateException("Field '" + field.getName() + "' should be of type LuaValue.");
        }
    }

    private static void validateFunction(Method method) {
        if (!VarArg.class.isAssignableFrom(method.getReturnType())) {
            throw new IllegalStateException("Method '" + method.getName() + "' should return value of type VarArg.");
        }
        if (method.getParameterCount() != 1 || !method.getParameterTypes()[0].isAssignableFrom(VarArg.class)) {
            throw new IllegalStateException("Method '" + method.getName() + "' should take a single parameter of type VarArg.");
        }
    }

    private static void validateMetaMethod(Method method, MetaMethodType type) {
        if (type.parameters != -1) {
            if (type.returns == 0 && method.getReturnType() != void.class) {
                throw new IllegalStateException("Meta method '" + method.getName() + "' should return void.");
            }
            if (type.returns == 1 && method.getReturnType() != LuaValue.class) {
                throw new IllegalStateException("Meta method '" + method.getName() + "' should return lua value.");
            }
            if (type.parameters == 0) {
                if (method.getParameterCount() != 0) {
                    throw new IllegalStateException("Meta method '" + method.getName() + "' of type '" + type.name() + "' should take 0 parameters.");
                }
            } else {
                if (type.parameters != method.getParameterCount()) {
                    throw new IllegalStateException("Meta method '" + method.getName() + "' of type '" + type.name() + "' should take " + type.parameters + " parameters of type LuaValue.");
                }
                for (Parameter parameter : method.getParameters()) {
                    if (!parameter.getType().isAssignableFrom(LuaValue.class)) {
                        throw new IllegalStateException("Meta method '" + method.getName() + "' of type '" + type.name() + "' should take " + type.parameters + " parameters of type LuaValue.");
                    }
                }
            }
        } else {
            if (!VarArg.class.isAssignableFrom(method.getReturnType())) {
                throw new IllegalStateException("Meta method '" + method.getName() + "' of type '" + type.name() + "' should return value of type VarArg.");
            }
            if (method.getParameterCount() != 1 || !method.getParameterTypes()[0].isAssignableFrom(VarArg.class)) {
                throw new IllegalStateException("Meta method '" + method.getName() + "' of type '" + type.name() + "' should take a single parameter of type VarArg.");
            }
        }
    }

    protected static class UserDataIndexer {
        private Map<String, Member> members;
        private UserData data;

        public UserDataIndexer(UserData data, Map<String, Field> fields, Map<String, Method> functions) {
            members = new HashMap<>(fields);
            members.putAll(functions);
            this.data = data;
        }

        public boolean index(LuaState state) {
            LuaValue value = LuaValue.from(state, -1);
            state.pop(1);
            if (!(value instanceof StringValue)) {
                return false;
            }
            String name = ((StringValue)value).getString();
            if (!members.containsKey(name)) {
                return false;
            }
            Member member = members.get(name);
            if (member instanceof Field) {
                // TODO: if field has a getter, use it
                try {
                    ((LuaValue)((Field)member).get(data)).push(state);
                } catch (IllegalAccessException ignored) {
                    return false;
                }
                return true;
            }
            if (member instanceof Method) {
                LuaValue.of((lua, args) -> {
                    try {
                        return (VarArg)((Method)member).invoke(data, lua, args);
                    } catch (IllegalAccessException | InvocationTargetException ignored) {
                        // TODO: throw?
                        return new VarArg();
                    }
                }).push(state);
                return true;
            }
            return false;
        }

        public boolean newIndex(LuaState state) {
            LuaValue key = LuaValue.from(state, -2);
            LuaValue value = LuaValue.from(state, -1);
            state.pop(2);
            if (!(key instanceof StringValue)) {
                return false;
            }
            String name = ((StringValue)key).getString();
            if (!members.containsKey(name)) {
                return false;
            }
            Member member = members.get(name);
            if (member instanceof Field) {
                // TODO: if field has a setter, use it
                try {
                    ((Field)member).set(data, value);
                } catch (IllegalAccessException ignored) {
                    return false;
                }
                return true;
            }
            return false;
        }
    }
}

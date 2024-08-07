package me.white.wlua;

import java.lang.reflect.*;
import java.util.*;

public abstract class UserData extends LuaValue {
    private boolean hasCollected = false;
    Map<String, Field> fields = new HashMap<>();
    Map<String, Method> functions = new HashMap<>();
    EnumMap<MetaMethodType, Method> metaMethods = new EnumMap<>(MetaMethodType.class);
    Map<String, Method> getters = new HashMap<>();
    Map<String, Method> setters = new HashMap<>();

    private void collectMembers() {
        Set<String> definedNames = new HashSet<>();
        Set<String> accessorNames = new HashSet<>();

        for (Field field : getClass().getFields()) {
            if (field.isAnnotationPresent(LuaField.class)) {
                if (!field.canAccess(this)) {
                    throw new IllegalStateException("Field '" + field.getName() + "' is not accessible.");
                }
                LuaField annotation = field.getAnnotation(LuaField.class);
                String name = annotation.value();
                if (definedNames.contains(name)) {
                    throw new IllegalStateException("Name '" + name + "' is already defined.");
                }
                definedNames.add(name);
                validateField(field);
                fields.put(name, field);
            }
        }
        for (Method method : getClass().getMethods()) {
            if (method.isAnnotationPresent(LuaFunction.class)) {
                if (!method.canAccess(this)) {
                    throw new IllegalStateException("Method '" + method.getName() + "' is not accessible.");
                }
                LuaFunction annotation = method.getAnnotation(LuaFunction.class);
                String name = annotation.value();
                if (definedNames.contains(name)) {
                    throw new IllegalStateException("Name '" + name + "' is already defined.");
                }
                definedNames.add(name);
                validateFunction(method);
                functions.put(name, method);
            } else if (method.isAnnotationPresent(LuaMetaMethod.class)) {
                if (!method.canAccess(this)) {
                    throw new IllegalStateException("Method '" + method.getName() + "' is not accessible.");
                }
                LuaMetaMethod annotation = method.getAnnotation(LuaMetaMethod.class);
                MetaMethodType type = annotation.value();
                if (metaMethods.containsKey(type)) {
                    throw new IllegalStateException("Meta method of type '" + type.name() + "' is already defined.");
                }
                type.validateSignature(method);
                metaMethods.put(type, method);
            } else if (method.isAnnotationPresent(LuaField.class)) {
                if (!method.canAccess(this)) {
                    throw new IllegalStateException("Method '" + method.getName() + "' is not accessible.");
                }
                LuaField annotation = method.getAnnotation(LuaField.class);
                String name = annotation.value();
                if (definedNames.contains(name) && !accessorNames.contains(name)) {
                    throw new IllegalStateException("Name '" + name + "' is already defined.");
                }
                accessorNames.add(name);
                addFieldAccessor(method, name);
            }
        }
    }

    private static void validateField(Field field) {
        if (!LuaValue.class.isAssignableFrom(field.getType())) {
            throw new IllegalStateException("Field '" + field.getName() + "' should be of type LuaValue.");
        }
    }

    private static void validateFunction(Method method) {
        String what = "Method '" + method.getName() + "'";
        if (method.getParameterCount() < 1 || !method.getParameterTypes()[0].isAssignableFrom(LuaState.class)) {
            throw new IllegalStateException(what + " should take at least 1 parameter, with first parameter of type LuaState.");
        }
        if (!VarArg.class.isAssignableFrom(method.getReturnType())) {
            throw new IllegalStateException(what + " should return value of type VarArg.");
        }
        if (method.getParameterCount() != 2 || !method.getParameterTypes()[1].isAssignableFrom(VarArg.class)) {
            throw new IllegalStateException(what + " should take 1 value parameter of type VarArg.");
        }
    }

    private void addFieldAccessor(Method method, String name) {
        String what = "'" + method.getName() + "' for field '" + name + "'";
        if (method.getParameterCount() < 1 || !method.getParameterTypes()[0].isAssignableFrom(LuaState.class)) {
            throw new IllegalStateException("Accessor " + what + " should take at least 1 parameter, with first parameter of type LuaState.");
        }
        if (method.getReturnType() == void.class) {
            if (method.getParameterCount() != 2 || !method.getParameterTypes()[1].isAssignableFrom(LuaValue.class)) {
                throw new IllegalStateException("Setter " + what + " should take 1 value parameter of type LuaValue.");
            }
            setters.put(name, method);
        } else if (LuaValue.class.isAssignableFrom(method.getReturnType())) {
            if (method.getParameterCount() != 1) {
                throw new IllegalStateException("Getter " + what + " should take 0 value parameters.");
            }
            getters.put(name, method);
        } else {
            throw new IllegalStateException("Accessor " + what + " should either return value of type LuaValue or return void.");
        }
    }

    private void getMetaTable(LuaState state) {
        String metaTableName = "userdata_" + getClass().getName();
        boolean isNew = LuaNatives.newMetaTable(state.ptr, metaTableName, getName()) == 1;
        if (isNew) {
            for (MetaMethodType type : metaMethods.keySet()) {
                if (type.metaMethod != null) {
                    LuaNatives.setMetaMethod(state.ptr, type.metaMethod, type.ordinal(), -1);
                }
            }
        }
    }

    public String getName() {
        return getClass().getSimpleName().isBlank() ? "Unknown" : getClass().getSimpleName();
    }

    @Override
    void push(LuaState state) {
        if (!hasCollected) {
            collectMembers();
        }
        LuaNatives.newUserData(state.ptr, this);
        getMetaTable(state);
        LuaNatives.lua_setmetatable(state.ptr, -2);
    }
}

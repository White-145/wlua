package me.white.wlua;

import java.lang.reflect.*;
import java.util.*;

public abstract class UserData extends LuaValue {
    private boolean hasCollected = false;
    final Map<String, Field> readFields = new HashMap<>();
    final Map<String, Field> writeFields = new HashMap<>();
    final Map<String, Method> functions = new HashMap<>();
    final EnumMap<MetaMethodType, Method> metaMethods = new EnumMap<>(MetaMethodType.class);
    final Map<String, Method> getters = new HashMap<>();
    final Map<String, Method> setters = new HashMap<>();

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
                ValidatorUtil.validateField(field);
                switch (annotation.type()) {
                    case READ_ONLY -> {
                        readFields.put(name, field);
                    }
                    case WRITE_ONLY -> {
                        writeFields.put(name, field);
                    }
                    case REGULAR -> {
                        readFields.put(name, field);
                        writeFields.put(name, field);
                    }
                }
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
                ValidatorUtil.validateFunction(method);
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
                ValidatorUtil.validateMetaMethod(method, type);
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
                ValidatorUtil.validateAccessor(method, name, getters, setters);
            }
        }
        hasCollected = true;
    }

    private void getMetaTable(LuaState state) {
        String metaTableName = "userdata_" + getClass().getName();
        boolean isNew = LuaNatives.newMetaTable(state.ptr, metaTableName, getName());
        if (isNew) {
            for (MetaMethodType type : metaMethods.keySet()) {
                if (type.metaMethod != null) {
                    LuaNatives.setMetaMethod(state.ptr, type.metaMethod, type.ordinal(), type.returns);
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
        LuaNatives.setMetaTable(state.ptr);
    }
}

package me.white.wlua;

import java.lang.reflect.*;
import java.util.*;

public abstract non-sealed class UserData extends LuaValue {
    private static final Map<Class<? extends UserData>, FieldData> fieldDatas = new HashMap<>();

    public String getName() {
        String name = getClass().getSimpleName();
        return name.isBlank() ? "Unknown" : name;
    }

    static FieldData getFieldData(Class<? extends UserData> clazz) {
        if (!fieldDatas.containsKey(clazz)) {
            collect(clazz);
        }
        return fieldDatas.get(clazz);
    }

    public static void collect(Class<? extends UserData> clazz) {
        fieldDatas.put(clazz, new FieldData(clazz));
    }

    public static void clean(Class<? extends UserData> clazz) {
        fieldDatas.remove(clazz);
    }

    @Override
    public final ValueType getType() {
        return ValueType.USERDATA;
    }

    @Override
    final void push(LuaState state) {
        FieldData fieldData = getFieldData(getClass());
        LuaNatives.newUserData(state.ptr, this);
        fieldData.pushMetaTable(state, getName());
        LuaNatives.setMetaTable(state.ptr);
    }

    static class FieldData {
        private final Class<? extends UserData> clazz;
        final Map<String, Field> readFields = new HashMap<>();
        final Map<String, Field> writeFields = new HashMap<>();
        final Map<String, Method> functions = new HashMap<>();
        final EnumMap<MetaMethodType, Method> metaMethods = new EnumMap<>(MetaMethodType.class);
        final Map<String, Method> getters = new HashMap<>();
        final Map<String, Method> setters = new HashMap<>();

        FieldData(Class<? extends UserData> clazz) {
            this.clazz = clazz;
            collect();
        }

        private void collect() {
            Set<String> definedNames = new HashSet<>();
            Set<String> accessorNames = new HashSet<>();
            Set<String> getterNames = new HashSet<>();
            Set<String> setterNames = new HashSet<>();

            for (Field field : clazz.getFields()) {
                if (field.isAnnotationPresent(LuaField.class)) {
                    LuaField annotation = field.getAnnotation(LuaField.class);
                    String name = annotation.value();
                    if (definedNames.contains(name)) {
                        throw new IllegalStateException("Name '" + name + "' is already defined.");
                    }
                    definedNames.add(name);
                    ValidatorUtil.validateField(field);
                    if (annotation.type().canRead) {
                        readFields.put(name, field);
                    }
                    if (annotation.type().canWrite) {
                        writeFields.put(name, field);
                    }
                }
            }
            for (Method method : clazz.getMethods()) {
                if (method.isAnnotationPresent(LuaFunction.class)) {
                    LuaFunction annotation = method.getAnnotation(LuaFunction.class);
                    String name = annotation.value();
                    if (definedNames.contains(name)) {
                        throw new IllegalStateException("Name '" + name + "' is already defined.");
                    }
                    definedNames.add(name);
                    ValidatorUtil.validateFunction(method);
                    functions.put(name, method);
                } else if (method.isAnnotationPresent(LuaMetaMethod.class)) {
                    LuaMetaMethod annotation = method.getAnnotation(LuaMetaMethod.class);
                    MetaMethodType type = annotation.value();
                    if (metaMethods.containsKey(type)) {
                        throw new IllegalStateException("Meta method of type '" + type.name() + "' is already defined.");
                    }
                    ValidatorUtil.validateMetaMethod(method, type);
                    metaMethods.put(type, method);
                } else if (method.isAnnotationPresent(LuaAccessor.class)) {
                    LuaAccessor annotation = method.getAnnotation(LuaAccessor.class);
                    String name = annotation.value();
                    if (definedNames.contains(name) && !accessorNames.contains(name)) {
                        throw new IllegalStateException("Name '" + name + "' is already defined.");
                    }
                    AccessorType type = ValidatorUtil.validateAccessor(method, name, annotation.type());
                    if (type == AccessorType.GETTER) {
                        if (getterNames.contains(name)) {
                            throw new IllegalStateException("Getter for '" + name + "' is already defined.");
                        }
                        getterNames.add(name);
                        getters.put(name, method);
                    } else {
                        if (setterNames.contains(name)) {
                            throw new IllegalStateException("Setter for '" + name + "' is already defined.");
                        }
                        setterNames.add(name);
                        setters.put(name, method);
                    }
                    definedNames.add(name);
                    accessorNames.add(name);
                }
            }
        }

        void pushMetaTable(LuaState state, String name) {
            String metaTableName = "userdata_" + clazz.getName();
            boolean isNew = LuaNatives.newMetaTable(state.ptr, metaTableName, name);
            if (isNew) {
                for (MetaMethodType type : metaMethods.keySet()) {
                    if (type.metaMethod != null) {
                        LuaNatives.setMetaMethod(state.ptr, type.metaMethod, type.ordinal(), type.returns);
                    }
                }
            }
        }
    }
}

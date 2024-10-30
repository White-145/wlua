package me.white.wlua;

import java.lang.reflect.*;
import java.util.*;

public abstract non-sealed class UserData extends LuaValue {
    private static final Map<Class<? extends UserData>, FieldData> fieldDatas = new HashMap<>();
    private final String name;

    protected UserData(String name) {
        this.name = name;
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

    // make LuaValue methods final so that they cannot be overriden by user
    @Override
    public final boolean isNil() {
        return super.isNil();
    }

    @Override
    public final boolean isNumber() {
        return super.isNumber();
    }

    @Override
    public final boolean getBoolean() {
        return super.getBoolean();
    }

    @Override
    public final double getNumber() {
        return super.getNumber();
    }

    @Override
    public final long getInteger() {
        return super.getInteger();
    }

    @Override
    public final String getString() {
        return super.getString();
    }

    @Override
    public final ValueType getType() {
        return ValueType.USER_DATA;
    }

    @Override
    final void push(LuaState state) {
        FieldData fieldData = getFieldData(getClass());
        LuaNatives.newUserData(state.ptr, this);
        fieldData.pushMetaTable(state, name);
        LuaNatives.setMetaTable(state.ptr);
    }

    static class FieldData {
        private final Class<? extends UserData> clazz;
        final Map<String, Field> readFields = new HashMap<>();
        final Map<String, Field> writeFields = new HashMap<>();
        final Map<String, Method> functions = new HashMap<>();
        final EnumMap<MetaMethodType, Method> metaMethods = new EnumMap<>(MetaMethodType.class);
        final Map<String, Method> customMetaMethods = new HashMap<>();
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
                } else if (method.isAnnotationPresent(LuaCustomMetaMethod.class)) {
                    LuaCustomMetaMethod annotation = method.getAnnotation(LuaCustomMetaMethod.class);
                    String name = annotation.value();
                    if (customMetaMethods.containsKey(name) || metaMethods.containsKey(MetaMethodType.byName(name))) {
                        throw new IllegalStateException("Meta method with name '" + name + "' is already defined.");
                    }
                    ValidatorUtil.validateCustomMetaMethod(method, name);
                    customMetaMethods.put(name, method);
                }
            }
        }

        void pushMetaTable(LuaState state, String name) {
            String metaTableName = "userdata_" + clazz.getName();
            boolean isNew = LuaNatives.newMetaTable(state.ptr, metaTableName, name);
            if (isNew) {
                for (MetaMethodType type : metaMethods.keySet()) {
                    if (type.name != null) {
                        LuaNatives.setMetaMethod(state.ptr, type.name, type.ordinal(), type.returns);
                    }
                }
                for (Map.Entry<String, Method> metaMethod : customMetaMethods.entrySet()) {
                    LuaNatives.setCustomMetaMethod(state.ptr, metaMethod.getKey(), metaMethod.getValue());
                }
            }
        }
    }
}

package me.white.wlua;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

class ValidatorUtil {
    static void validateField(Field field) {
        if (!LuaValue.class.isAssignableFrom(field.getType())) {
            throw new IllegalStateException("Field '" + field.getName() + "' should be of type LuaValue.");
        }
    }

    static void validateFunction(Method method) {
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

    static void validateMetaMethod(Method method, MetaMethodType type) {
        String what = "Meta method '" + method.getName() + "' of type '" + type.name() + "'";
        if (method.getParameterCount() < 1 || !method.getParameterTypes()[0].isAssignableFrom(LuaState.class)) {
            throw new IllegalStateException(what + " should take at least 1 parameter, with first parameter of type LuaState.");
        }
        if (type.returns == 0 && method.getReturnType() != void.class) {
            throw new IllegalStateException(what + " should return void.");
        }
        if (type.returns == 1 && !LuaValue.class.isAssignableFrom(method.getReturnType())) {
            throw new IllegalStateException(what + " should return value of type LuaValue.");
        }
        if (type.returns == -1 && !VarArg.class.isAssignableFrom(method.getReturnType())) {
            throw new IllegalStateException(what + " should return value of type VarArg.");
        }
        if (type.parameters == 0 && method.getParameterCount() != 1) {
            throw new IllegalStateException(what + " should take 0 value parameters.");
        }
        if (type.parameters == -1) {
            if (method.getParameterCount() != 2 || !method.getParameterTypes()[1].isAssignableFrom(VarArg.class)) {
                throw new IllegalStateException(what + " should take 1 value parameter of type VarArg.");
            }
        } else {
            if (type.parameters != method.getParameterCount() - 1) {
                throw new IllegalStateException(what + " should take " + type.parameters + " value parameters of type LuaValue.");
            }
            for (int i = 1; i < method.getParameterCount(); ++i) {
                if (!method.getParameterTypes()[i].isAssignableFrom(LuaValue.class)) {
                    throw new IllegalStateException(what + " should take " + type.parameters + " value parameters of type LuaValue.");
                }
            }
        }
    }

    static AccessorType validateAccessor(Method method, String name, AccessorType type) {
        String what = "'" + method.getName() + "' for field '" + name + "'";
        if (method.getParameterCount() < 1 || !method.getParameterTypes()[0].isAssignableFrom(LuaState.class)) {
            throw new IllegalStateException("Accessor " + what + " should take at least 1 parameter, with first parameter of type LuaState.");
        }
        if (method.getReturnType() == void.class) {
            if (type == AccessorType.GETTER) {
                throw new IllegalStateException("Getter " + what + " should return value of type LuaValue.");
            }
            if (method.getParameterCount() != 2 || !method.getParameterTypes()[1].isAssignableFrom(LuaValue.class)) {
                throw new IllegalStateException("Setter " + what + " should take 1 value parameter of type LuaValue.");
            }
            return AccessorType.SETTER;
        } else if (LuaValue.class.isAssignableFrom(method.getReturnType())) {
            if (type == AccessorType.SETTER) {
                throw new IllegalStateException("Setter " + what + " should return void.");
            }
            if (method.getParameterCount() != 1) {
                throw new IllegalStateException("Getter " + what + " should take 0 value parameters.");
            }
            return AccessorType.GETTER;
        } else {
            if (type == AccessorType.GETTER) {
                throw new IllegalStateException("Getter " + what + " should return value of type LuaValue.");
            }
            if (type == AccessorType.SETTER) {
                throw new IllegalStateException("Setter " + what + " should return void.");
            }
            throw new IllegalStateException("Accessor " + what + " should either return value of type LuaValue or return void.");
        }
    }
}

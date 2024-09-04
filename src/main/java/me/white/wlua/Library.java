package me.white.wlua;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Library {
    private final Map<String, Method> functions = new HashMap<>();
    private boolean hasCollected = false;

    private void collectMembers() {
        Set<String> definedNames = new HashSet<>();
        for (Method method : getClass().getMethods()) {
            if (method.isAnnotationPresent(LuaFunction.class)) {
                if (!Modifier.isStatic(method.getModifiers())) {
                    throw new IllegalStateException("Method '" + method.getName() + "' is not static.");
                }
                if (!method.canAccess(null)) {
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
            }
        }
        hasCollected = true;
    }

    void open(LuaState state) {
        if (!hasCollected) {
            collectMembers();
        }
        for (Map.Entry<String, Method> entry : functions.entrySet()) {
            String name = entry.getKey();
            Method method = entry.getValue();
            LuaNatives.pushMethod(state.ptr, method);
            LuaNatives.setGlobal(state.ptr, name);
        }
    }
}

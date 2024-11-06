package me.white.wlua.annotation;

import me.white.wlua.AccessorType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LuaAccessor {
    String value();

    AccessorType type() default AccessorType.AUTO;
}

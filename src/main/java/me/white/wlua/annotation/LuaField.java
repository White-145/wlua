package me.white.wlua.annotation;

import me.white.wlua.FieldType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface LuaField {
    String value();

    FieldType type() default FieldType.REGULAR;
}

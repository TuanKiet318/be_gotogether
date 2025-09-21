package com.vn.gotogether.security.annotation;

import com.vn.gotogether.enums.CollaboratorRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireItineraryRole {
    CollaboratorRole[] value() default {CollaboratorRole.OWNER, CollaboratorRole.EDITOR};
}
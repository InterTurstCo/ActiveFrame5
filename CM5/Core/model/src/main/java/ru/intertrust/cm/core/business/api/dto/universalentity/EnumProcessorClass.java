package ru.intertrust.cm.core.business.api.dto.universalentity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumProcessorClass {

    Class<? extends EnumProcessor<?>> value ();

}
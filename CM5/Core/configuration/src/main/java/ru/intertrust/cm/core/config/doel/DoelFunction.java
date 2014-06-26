package ru.intertrust.cm.core.config.doel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ru.intertrust.cm.core.business.api.dto.FieldType;

/**
 * Аннтотация, описывающая класс, реализующий подключаемую функцию DOEL.
 *
 * @author apirozhkov
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DoelFunction {

    /**
     * Имя функции, используемое в DOEL. Должно быть уникальным
     */
    String name();

    /**
     * Количество обязательных параметров.
     * По умолчанию - 0.
     */
    int requiredParams() default 0;

    /**
     * Максимальное количество необязательных параметров.
     * По умолчанию - 0.
     */
    int optionalParams() default 0;

    /**
     * Принимаемые функцией типы контекстов.
     * Пустой список означает, что функция принимает любой контекст (значение по умолчанию).
     */
    FieldType[] contextTypes() default { };

    /**
     * Признак того, что функция изменяет тип значения (возвращаемый тип отличается от типа контекста).
     * Если атрибут устанавливается в true, необходимо указать возвращаемый тип значения ({@link #resultType()}).
     * Значение по умолчанию - false (не изменяет).
     */
    boolean changesType() default false;

    /**
     * Возвращаемый функцией тип значений.
     * Этот атрибут имеет смысл только тогда, когда атрибут {@link #changesType()} равен true.
     * Значение по умолчанию - {@link FieldType#REFERENCE}, однако полагаться на это не рекомендуется.
     * Оно задано, в основном, для того, чтобы аннотация не требовала указывать его в том случае,
     * когда функция не изменяет тип значения.
     */
    FieldType resultType() default FieldType.REFERENCE;

    /**
     * Признак того, принимает ли функция множественные значения.
     * По умолчанию - true (принимает).
     */
    boolean contextMultiple() default true;

    /**
     * Признак того, может ли возвращать функция множественные значения.
     * По умолчанию - true (может).
     */
    boolean resultMultiple() default true;
}

package ru.intertrust.cm.core.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация, позволяющая исключить класс, метод из компиляции GWT. Нужно помнить, что данные артефакты нельзя будет
 * использовать внутри GWT-кода, использующегося на клиенте. Хотя можно использовать "родную" аннотацию GWT c аналогичным
 * названием, но это внесло бы зависимость в модель от GWT-библиотек.
 * @author Denis Mitavskiy
 *         Date: 23.04.14
 *         Time: 18:58
 */
@Retention(RetentionPolicy.CLASS)
@Target({
        ElementType.TYPE, ElementType.METHOD,
        ElementType.CONSTRUCTOR, ElementType.FIELD })
public @interface GwtIncompatible {
    /**
     * Данный атрибут может быть использован для объяснения причин несовместимости артефакта с GWT
     */
    String value() default "";
}

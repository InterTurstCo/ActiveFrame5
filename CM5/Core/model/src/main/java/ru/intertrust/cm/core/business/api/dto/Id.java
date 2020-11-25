package ru.intertrust.cm.core.business.api.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Уникальный идентификатор доменного объекта
 * Author: Denis Mitavskiy
 * Date: 19.05.13
 * Time: 16:43
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
public interface Id extends Dto {
    /**
     * Создаёт идентификатор доменного объекта на основе его строкового представления. Строковое представление
     * идентификатора генерируется методом {@link ru.intertrust.cm.core.business.api.dto.impl.RdbmsId#toString()}
     * @param stringRep строковое представление индентификатора
     * @throws NullPointerException если строковое представление есть null
     * @throws IllegalArgumentException если строковое представление не может быть декодировано
     */
    public void setFromStringRepresentation(String stringRep);

    /**
     * Возвращает строковое представление идентификатора доменного объекта
     * @return строковое представление идентификатора доменного объекта
     */
    public String toStringRepresentation();

    /**
     * Возвращает идентификатор переданного объекта (см. {@link IdentifiableObject#getId()}).
     * @param identifiableObject - объект, не может быть {@code null};
     * @return идентификатор объекта
     */
    static Id fromObject (IdentifiableObject identifiableObject) {
        return identifiableObject.getId();
    }

}

package ru.intertrust.cm.core.business.api.dto;

import java.io.Serializable;

/**
 * Уникальный идентификатор доменного объекта
 * Author: Denis Mitavskiy
 * Date: 19.05.13
 * Time: 16:43
 */
public interface Id extends Serializable {
    /**
     * Создаёт идентификатор доменного объекта на основе его строкового представления. Строковое представление
     * идентификатора генерируется методом {@link ru.intertrust.cm.core.business.api.dto.RdbmsId#toString()}
     * @param stringRep строковое представление индентификатора
     * @throws NullPointerException если строковое представление есть null
     * @throws IllegalArgumentException если строковое представление не может быть декодировано
     */
    public void setFromStringRepresentation(String stringRep);

    /**
     * Возвращает строковое представление идентификатора доменного объекта
     * @return строковое представление идентификатора доменного объекта
     */
    String toStringRepresentation();
}

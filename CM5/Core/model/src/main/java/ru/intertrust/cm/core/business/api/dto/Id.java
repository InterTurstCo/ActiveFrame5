package ru.intertrust.cm.core.business.api.dto;

import java.io.Serializable;

/**
 * Уникальный идентификатор бизнес-объекта
 * Author: Denis Mitavskiy
 * Date: 19.05.13
 * Time: 16:43
 */
public interface Id extends Serializable {
    /**
     * Создаёт идентификатор бизнес-объекта на основе его строкового представления. Строковое представление
     * идентификатора генерируется методом {@link ru.intertrust.cm.core.business.api.dto.RdbmsId#toString()}
     * @param stringRep строковое представление индентификатора
     * @throws NullPointerException если строковое представление есть null
     * @throws IllegalArgumentException если строковое представление не может быть декодировано
     */
    public void setFromStringRepresentation(String stringRep);

    /**
     * Возвращает строковое представление идентификатора бизнес-объекта
     * @return строковое представление идентификатора бизнес-объекта
     */
    String toStringRepresentation();
}

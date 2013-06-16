package ru.intertrust.cm.core.business.api.dto;

/**
 * Уникальный идентификатор доменного объекта, состоящий из целочисленного ключа и названия доменного объекта.<br/>
 * Подобные ключи используются для идентификации объектов в реляционных СУБД.
 *
 * Author: Denis Mitavskiy
 * Date: 19.05.13
 * Time: 16:06
 */
public class RdbmsId implements Id {
    private String typeName;
    private long id;

    /**
     * Конструктор по умолчанию. Обычно не используется. Требуется для сериализации
     */
    public RdbmsId() {
    }

    /**
     * Создаёт идентификатор доменного объекта на основе его строкового представления. Строковое представление
     * идентификатора генерируется методом {@link ru.intertrust.cm.core.business.api.dto.RdbmsId#toString()}
     * @param stringRep строковое представление индентификатора
     * @throws NullPointerException если строковое представление есть null
     * @throws IllegalArgumentException если строковое представление не может быть декодировано
     */
    public RdbmsId(String stringRep) {
        setFromStringRepresentation(stringRep);
    }

    /**
     * Создаёт идентификатор доменного объекта на основе его строкового представления. Строковое представление
     * идентификатора генерируется методом {@link ru.intertrust.cm.core.business.api.dto.RdbmsId#toString()}
     * @param stringRep строковое представление индентификатора
     * @throws NullPointerException если строковое представление есть null
     * @throws IllegalArgumentException если строковое представление не может быть декодировано
     */
    public void setFromStringRepresentation(String stringRep) {
        int index = stringRep.lastIndexOf('|');
        if (index == -1) {
            throw new IllegalArgumentException(stringRep + " can't be parsed");
        }
        String typeName = stringRep.substring(0, index);
        if (typeName.trim().isEmpty()) {
            throw new IllegalArgumentException(stringRep + " can't be parsed");
        }
        this.typeName = typeName;
        try {
            this.id = Long.parseLong(stringRep.substring(index));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(stringRep + " can't be parsed");
        }
    }

    /**
     * Создаёт идентификатор доменного объекта
     * @param typeName название типа доменного объекта
     * @param id целочисленный идентификатор
     */
    public RdbmsId(String typeName, long id) {
        this.typeName = typeName;
        this.id = id;
    }

    /**
     * Возвращает название типа доменного объекта
     * @return название доменного объекта
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * Возвращает целочисленный идентификатор доменного объекта
     * @return целочисленный идентификатор доменного объекта
     */
    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RdbmsId rdbmsId = (RdbmsId) o;
        if (id != rdbmsId.id) {
            return false;
        }
        if (!typeName.equals(rdbmsId.typeName)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toStringRepresentation() {
        return typeName + "|" + id;
    }

    @Override
    public String toString() {
        return "RdbmsId{" +
                "name='" + typeName + '\'' +
                ", id=" + id +
                '}';
    }
}

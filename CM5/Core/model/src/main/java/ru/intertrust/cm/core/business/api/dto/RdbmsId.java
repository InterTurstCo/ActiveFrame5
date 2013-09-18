package ru.intertrust.cm.core.business.api.dto;

import ru.intertrust.cm.core.model.FatalException;

/**
 * Уникальный идентификатор доменного объекта, состоящий из целочисленного ключа и названия доменного объекта.<br/>
 * Подобные ключи используются для идентификации объектов в реляционных СУБД.
 *
 * Author: Denis Mitavskiy
 * Date: 19.05.13
 * Time: 16:06
 */
public class RdbmsId implements Id {

    public static final int MAX_DO_TYPE_ID_LENGTH = 4;
    public static final int MAX_DO_ID_LENGTH = 12;
    public static final int MAX_ID_LENGTH = MAX_DO_TYPE_ID_LENGTH + MAX_DO_ID_LENGTH;

    private int typeId;
    private long id;
    private String stringRepresentation;

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
        if (stringRep.length() != MAX_ID_LENGTH) {
            throw new IllegalArgumentException("Invalid id string representation '" + stringRep + "'. Must be " +
                    "exactly " + MAX_ID_LENGTH + " characters long");
        }

        this.stringRepresentation = stringRep;

        try {
            this.typeId = Integer.parseInt(stringRep.substring(0, MAX_DO_TYPE_ID_LENGTH));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(stringRep + " can't be parsed");
        }

        try {
            this.id = Long.parseLong(stringRep.substring(MAX_DO_TYPE_ID_LENGTH));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(stringRep + " can't be parsed");
        }
    }

    /**
     * Создаёт идентификатор доменного объекта
     * @param typeId идентификатор типа доменного объекта
     * @param id целочисленный идентификатор
     */
    public RdbmsId(int typeId, long id) {
        this.typeId = typeId;
        this.id = id;
    }

    /**
     * Возвращает идентификатор типа доменного объекта
     * @return идентификатор доменного объекта
     */
    public int getTypeId() {
        return typeId;
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
        if (typeId != rdbmsId.typeId) {
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
        if (stringRepresentation == null) {
            stringRepresentation = generateStringRepresentation();
        }

        return stringRepresentation;
    }

    @Override
    public String toString() {
        return "RdbmsId{" +
                "typeId='" + typeId + '\'' +
                ", id=" + id +
                '}';
    }

    protected String generateStringRepresentation() {
        String typeIdString = String.valueOf(typeId);
        int typeIdLength = typeIdString.length();
        if (typeIdLength > MAX_DO_TYPE_ID_LENGTH) {
            throw new FatalException("Domain Object type id '" + typeIdString  +"' exceeds " + MAX_DO_TYPE_ID_LENGTH +
                    " digits length.");
        }

        String idString = String.valueOf(id);
        int idLength = idString.length();
        if (idLength > MAX_DO_ID_LENGTH) {
            throw new FatalException("Domain Object id '" + idString  +"' exceeds " + MAX_DO_ID_LENGTH +
                    " digits length.");
        }

        String fixedLengthTypeId = generateFixedLengthString(typeIdString, MAX_DO_TYPE_ID_LENGTH);
        String fixedLengthId = generateFixedLengthString(idString, MAX_DO_ID_LENGTH);
        return fixedLengthTypeId + fixedLengthId;
    }

    private String generateFixedLengthString(String string, int maxLength) {
        int stringLength = string.length();
        int missedLength = maxLength - stringLength;

        StringBuilder fixedLengthString = new StringBuilder();
        for(int i = 0; i < missedLength; i ++) {
            fixedLengthString.append(String.valueOf(0));
        }

        fixedLengthString.append(string);
        return fixedLengthString.toString();
    }
}

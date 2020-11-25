package ru.intertrust.cm.core.business.api.dto;

public abstract class ObjectId {

    /**
     * Возвращает идентификатор переданного объекта (см. {@link IdentifiableObject#getId()}).
     * @param identifiableObject - объект, не может быть {@code null};
     * @return идентификатор объекта
     */
    public static Id get (final IdentifiableObject identifiableObject) {
        return identifiableObject.getId();
    }

    private ObjectId () {
    }

}
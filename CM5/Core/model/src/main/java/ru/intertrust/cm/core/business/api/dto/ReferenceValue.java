package ru.intertrust.cm.core.business.api.dto;

import java.io.Serializable;

/**
 * Хранение ссылки на другой объект.
 * 
 * @author apirozhkov
 */
public class ReferenceValue extends Value implements Serializable {

    private Id reference;

    /**
     * Создаёт пустое значение-ссылку.
     */
    public ReferenceValue() {
    }

    /**
     * Создаёт значение - ссылку на доменный объект.
     * 
     * @param reference ссылка на доменный объект
     */
    public ReferenceValue(Id reference) {
        this.reference = reference;
    }

    @Override
    public Object get() {
        return reference;
    }

}

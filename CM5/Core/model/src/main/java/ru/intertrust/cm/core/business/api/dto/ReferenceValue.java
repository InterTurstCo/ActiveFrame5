package ru.intertrust.cm.core.business.api.dto;

/**
 * Хранение ссылки на другой объект.
 * 
 * @author apirozhkov
 */
public class ReferenceValue extends Value<ReferenceValue> {

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
    public Id get() {
        return reference;
    }

    @Override
    public int compareTo(ReferenceValue o) {
        if (o == null || o.isEmpty()) {
            return this.isEmpty() ? 0 : 1;
        } else {
            return this.isEmpty() ? -1 : reference.toStringRepresentation().compareTo(o.reference.toStringRepresentation());
        }
    }
}

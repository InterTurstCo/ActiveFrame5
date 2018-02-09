package ru.intertrust.cm.core.business.api.dto;

import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;

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
    public boolean isImmutable() {
        return true;
    }

    @Override
    public int compareTo(ReferenceValue o) {
        if (o == null || o.isEmpty()) {
            return this.isEmpty() ? 0 : 1;
        } else if (this.isEmpty()) {
            return -1;
        } else {
            if (((RdbmsId) reference).getTypeId() > ((RdbmsId) o.reference).getTypeId())
                return 1;
            else if (((RdbmsId) reference).getTypeId() < ((RdbmsId) o.reference).getTypeId())
                return -1;
            else {
                if (((RdbmsId) reference).getId() > ((RdbmsId) o.reference).getId())
                    return 1;
                else if (((RdbmsId) reference).getId() < ((RdbmsId) o.reference).getId())
                    return -1;
                else
                    return 0;
            }
        }
    }

    @Override
    public final ReferenceValue getPlatformClone() {
        final Id id = get();
        if (this.getClass() != ReferenceValue.class) {
            return id == null ? new ReferenceValue() : new ReferenceValue(new RdbmsId(id));
        } else if (id != null && id.getClass() != RdbmsId.class) {
            return new ReferenceValue(new RdbmsId(id));
        } else {
            return this;
        }
    }
}

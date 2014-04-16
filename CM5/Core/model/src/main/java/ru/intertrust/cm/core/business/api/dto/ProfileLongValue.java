package ru.intertrust.cm.core.business.api.dto;

/**
 * Числовое значение поля объекта профиля
 */
public class ProfileLongValue extends LongValue implements ProfileValue{

    private boolean readOnly;

    public ProfileLongValue() {
        super();
    }

    public ProfileLongValue(Integer value) {
        super(value);
    }

    public ProfileLongValue(Long value) {
        super(value);
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
}
